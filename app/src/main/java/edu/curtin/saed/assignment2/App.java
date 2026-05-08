package edu.curtin.saed.assignment2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * This is demonstration code intended for you to modify. Currently, it sets up a rudimentary
 * JavaFX GUI with the basic elements required for the assignment.
 *
 * (There is an equivalent Swing version of this, which you can use if you have trouble getting
 * JavaFX as a whole to work.)
 *
 * You will need to use the GridArea object, and create various GridAreaIcon objects, to represent
 * the on-screen map.
 *
 * Use the startBtn, endBtn, statusText and textArea objects for the other input/output required by
 * the assignment specification.
 *
 * Break this up into multiple methods and/or classes if it seems appropriate. Promote some of the
 * local variables to fields if needed.
 */
public class App extends Application
{
    private List<Integer> startPos;
    private List<Integer> trophyPos;

    // Data transfer from input file to app
    private List<TempItemData> tempItemData;
    private List<TempObstacleData> tempObstacleData;

    private List<List<GameObject>> objectMap;
    private List<GameObject> itemList; // Holds items and trophy only, for 'reveal' plugin
    private List<List<GridAreaIcon>> fogMap;

    private GridAreaIcon playerIcon;

    private GridArea area;
    private Button abilityBtn;
    private TextField localeInputField;

    private Image imgPerson;
    private Image imgTrophy;
    private Image imgItem;
    private Image imgObstacle;
    private Image imgFog;

    // Move every 500ms at the fastest
    private long lastMoveTime = 0L;
    private long moveCooldown = 200L;

    private static String fileName; // Input file
    
    public static void main(String[] args)
    {
         if (args.length == 0) {
            System.out.println("Usage: ./gradlew run --args=\"<filename>\"");
            return;
        }
        fileName = args[0];
        launch(args);
    }

    @Override
    public void start(Stage stage)
    {
        // #region LOCAL_VARIABLES

        Label statusText;
        TextArea inventoryTextArea;
        Button applyBtn;
        Label dateLabel;

        List<Integer> gameDims;
        List<String> plugins;
        List<String> scripts;

        ClassLoader cl = App.class.getClassLoader();

        imgPerson = new Image(cl.getResourceAsStream("person.png"), 64, 64, true, true);
        imgTrophy = new Image(cl.getResourceAsStream("trophy.png"), 64, 64, true, true);
        imgItem = new Image(cl.getResourceAsStream("item.png"), 64, 64, true, true);
        imgObstacle = new Image(cl.getResourceAsStream("obstacle.png"), 64, 64, true, true);
        imgFog = new Image(cl.getResourceAsStream("fog.png"), 64, 64, true, true);

        // #endregion

        // #region DATA_INPUT

        String encoding = getFileEncoding(fileName);
        if (encoding == null) {
            System.out.println("Invalid file name. Please use input.utf8.map, input.utf16.map, or input.utf32.map");
            return;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }

        try (InputStream in = new FileInputStream(file)) {

            MyParser parser = MyParser.parse(in, encoding);

            gameDims = parser.getSize();
            startPos = parser.getStartPosition();
            trophyPos = parser.getGoal();
            tempItemData = parser.getItems();
            tempObstacleData = parser.getObstacles();
            plugins = parser.getPlugins();
            scripts = parser.getScripts();

        } catch (IOException | ParseException e) {
            System.out.println("Error parsing input: " + e);
            return;
        }
        // #endregion
        
        // #region UI_ELEMENTS

        area = new GridArea(gameDims.get(0), gameDims.get(1));
        area.setStyle("-fx-background-color: #4D4D4D;");

        abilityBtn = new Button("Special Ability"); // For button-related plugin 'teleport'

        statusText = new Label(""); // Info, like picking up items, obstacle traversal, winning
        inventoryTextArea = new TextArea();
        inventoryTextArea.appendText("Inventory:\n");
        
        localeInputField = new TextField();
        localeInputField.setPromptText("fr-FR/de-DE/en-AU/en-US");
        localeInputField.setPrefColumnCount(14);
        applyBtn = new Button("Apply"); // Apply locale
        dateLabel = new Label();

        statusText.setMaxWidth(Double.MAX_VALUE);
        statusText.setWrapText(true);
        HBox.setHgrow(statusText, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // #endregion

        // #region BUILD_GAME

        // Build UI Manager (stores UI and locale stuff)
        UIManager uiManager = new UIManager(abilityBtn, 
                                            statusText, inventoryTextArea, 
                                            applyBtn, 
                                            stage, dateLabel);
        uiManager.refreshDateLabel(); // Initialise date based on default locale (initially)

        int rows = gameDims.get(0);
        int cols = gameDims.get(1);

        // Initialise object map (easy access w/ coords)
        objectMap = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<GameObject> row = new ArrayList<>(cols);
            for (int j = 0; j < cols; j++) {
                row.add(null);
            }
            objectMap.add(row);
        }

        // Add Player, trophy, items and obstacles to map and GridArea.
        buildMap();

        // Initialise fog map (easy access w/ coords)
        fogMap = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<GridAreaIcon> row = new ArrayList<>(cols);
            for (int j = 0; j < cols; j++) {
                row.add(null);
            }
            fogMap.add(row);
        }

        // Add fog to GridArea.
        buildFog();

        // Build Game Manager (API implementation), stores UI Manager, inventory, etc
        GameManager gameManager = new GameManager(uiManager, area, 
                                                playerIcon, objectMap, 
                                                itemList, fogMap, imgObstacle);

        // Initialise plugin/s
        loadPlugins(plugins, gameManager);

        // Initialise script/s
        ScriptHandler handler = new ScriptHandler();
        
        for (String script : scripts) {
            handler.runScript(gameManager, script);
        }

        // #endregion

        // #region BUTTONS

        abilityBtn.setOnAction((event) ->
        {
            gameManager.notifyButtonListeners();
            abilityBtn.setDisable(true);
            System.out.println("Ability button pressed");
        });
        applyBtn.setOnAction((event) ->
        {
            uiManager.applyLocaleFromTag(localeInputField.getText());
            System.out.println("Apply button pressed");
        });
        stage.setOnCloseRequest((event) ->
        {
            System.out.println("Close button pressed");
        });
        
        // #endregion
        
        // #region PLUMBING and PLAYER_MOVEMENT

        // Below is basically just the GUI "plumbing" (connecting things together).

        var toolbar = new ToolBar();
        toolbar.getItems().addAll(
            abilityBtn, 
            new Separator(), 
            statusText, spacer, 
            dateLabel,
            new Separator(), 
            localeInputField, 
            applyBtn
        );

        var splitPane = new SplitPane();
        splitPane.getItems().addAll(area, inventoryTextArea);
        splitPane.setDividerPositions(0.75);

        stage.setTitle("Maze Game");
        var contentPane = new BorderPane();
        contentPane.setTop(toolbar);
        contentPane.setCenter(splitPane);

        var scene = new Scene(contentPane, 1200, 800);

        // Player movement using arrow keys
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMoveTime < moveCooldown) {
                e.consume();
                return;
            }
            boolean moved = false;
            switch (e.getCode()) {
                case UP:
                    gameManager.makeMove(0, -1);
                    moved = true;
                    break;
                case DOWN:
                    gameManager.makeMove(0, 1);
                    moved = true;
                    break;
                case LEFT:
                    gameManager.makeMove(-1, 0);
                    moved = true;
                    break;
                case RIGHT:
                    gameManager.makeMove(1, 0);
                    moved = true;
                    break;
                default:
                    break;
            }
            if (moved) {
                lastMoveTime = currentTime;
                e.consume();
            }
        });
        stage.setScene(scene);
        stage.show();
        // #endregion
    }

    // Get encoding type from input file name
    public String getFileEncoding(String fileName) {
        String encoding = null;

        if (fileName.contains("utf8")) {
            encoding = "UTF-8";
        } 
        else if (fileName.contains("utf16")) {
            encoding = "UTF-16";
        } 
        else if (fileName.contains("utf32")) {
            encoding = "UTF-32";
        }

        return encoding;
    }

    // Load any plugin/s defined in the input file, assuming they work
    public void loadPlugins(List<String> pluginNames, GameManager gameManager) {
        for (String pluginName : pluginNames) {
            try {
                Class<?> cls = Class.forName(pluginName);

                Constructor<?> constructor = cls.getConstructor(ApiInterface.class);

                @SuppressWarnings("unused") // No real functional effect, just to get rid of the warning.
                PluginInterface pluginInstance = (PluginInterface) constructor.newInstance(gameManager);
            } catch (ClassNotFoundException | NoSuchMethodException
                    | InstantiationException | IllegalAccessException
                    | InvocationTargetException e) {
                System.out.println("Failed to load plugin: " + pluginName + " : " + e);
            }
        }
    }

    /* 
     * BUILD_MAP
     * 
     * - Creates trophy and items, adding them to the GridArea, object map and item list
     * - Creates obstacles, adding them to the GridArea and obstacle map
     * - Creates player icon and adds it to the GridArea
     * 
     */
    public void buildMap() {
        itemList = new ArrayList<>();

        // Add trophy
        GridAreaIcon trophyIcon = new GridAreaIcon(
            trophyPos.get(0), 
            trophyPos.get(1), 
            0, 1, 
            imgTrophy, 
            ""
        );
        area.getIcons().add(trophyIcon);
        Trophy trophy = new Trophy(trophyIcon);
        objectMap.get(trophyPos.get(0)).set(trophyPos.get(1), trophy);
        itemList.add(trophy);

        /*
         * ITEMS
         * 
         * - Add items to GridArea, object map and item list
         * - Checks if position is already filled in the object map before proceeding
         * - Makes a new icon for each entry in the positions list and add to the GridArea.
         */
        for (TempItemData tempData : tempItemData) { 
            String name = tempData.name;
            List<List<Integer>> positions = tempData.positions;
            String message = tempData.message;

            for (List<Integer> pos : positions) {
                try {
                    // if object/player has already taken that spot, throw exception if out of bounds
                    if (objectMap.get(pos.get(0)).get(pos.get(1)) != null) {
                        continue;
                    }
                    if (pos.get(0).equals(startPos.get(0)) && pos.get(1).equals(startPos.get(1))) {
                        continue;
                    }

                    // create icon
                    GridAreaIcon icon = new GridAreaIcon(
                        pos.get(0), 
                        pos.get(1), 
                        0, 1, 
                        imgItem, 
                        ""
                    );

                    // add icon to grid
                    area.getIcons().add(icon);
                    // create item
                    Item item = new Item(name, message, icon);
                    // add to object map
                    objectMap.get(pos.get(0)).set(pos.get(1), item);
                    // add to item list
                    itemList.add(item);
                } catch (IndexOutOfBoundsException e) { // if coords out of bounds, skip
                    System.out.println("Tried to add item to out-of-bounds area. " + e);
                }
            }
        }

        /*
         * OBSTACLES
         * 
         * - Add items to GridArea, object map and item list
         * - Checks if position is already filled in the object map before proceeding
         * - Makes a new icon for each entry in the positions list and add to the GridArea.
         */
        for (TempObstacleData tempData : tempObstacleData) { 
            List<String> requires = tempData.requires;
            List<List<Integer>> positions = tempData.positions;

            for (List<Integer> pos : positions) {
                try {
                    // if object/player has already taken that spot, throw exception if out of bounds
                    if (objectMap.get(pos.get(0)).get(pos.get(1)) != null) {
                        continue;
                    }
                    if (pos.get(0).equals(startPos.get(0)) && pos.get(1).equals(startPos.get(1))) {
                        continue;
                    }

                    // create icon
                    GridAreaIcon icon = new GridAreaIcon(
                        pos.get(0), 
                        pos.get(1), 
                        0, 1, 
                        imgObstacle, 
                        ""
                    );

                    // add icon to grid
                    area.getIcons().add(icon);
                    // create obstacle
                    Obstacle obstacle = new Obstacle(requires, icon);
                    // add to object map
                    objectMap.get(pos.get(0)).set(pos.get(1), obstacle);
                } catch (IndexOutOfBoundsException e) { // if coords out of bounds, skip
                    System.out.println("Tried to add obstacle to out-of-bounds area. " + e);
                }
            }
        }

        // add player
        // adding last because needs to appear over obstacles when bypassing
        playerIcon = new GridAreaIcon(
            startPos.get(0), 
            startPos.get(1), 
            0, 1, 
            imgPerson, 
            ""
        );
        area.getIcons().add(playerIcon);
    }

    // Build fog on GridArea and fog map, not on player or orthogonal to player start pos
    public void buildFog() {
        // just fill fog map with fog icons, adding to area
        int startX = startPos.get(0);
        int startY = startPos.get(1);

        // remove fog icons on and around player
        for (int i = 0; i < fogMap.size(); i++) {
            for (int j = 0; j < fogMap.get(i).size(); j++) {

                GridAreaIcon icon = new GridAreaIcon(
                    i, j,
                    0, 1.0,
                    imgFog,
                    ""
                );

                // no fog on or orthogonal to player
                int dx = Math.abs(i - startX);
                int dy = Math.abs(j - startY);
                if (dx + dy <= 1) {
                    icon.setShown(false);
                }

                fogMap.get(i).set(j, icon);

                area.getIcons().add(icon);
            }
        }
    }

}
