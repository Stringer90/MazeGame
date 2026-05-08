package edu.curtin.saed.assignment2;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;

public class GameManager implements ApiInterface{
    
    private UIManager uiManager;
    private GridArea area;
    private GridAreaIcon playerIcon;
    
    // Holds items and trophy only, for when map acquired
    private List<GameObject> itemList; // List of items remaining on map
    private List<List<GameObject>> objectMap;
    private List<List<GridAreaIcon>> fogMap;
    private Image imgObstacle;

    private List<String> inventory;
    private String recentItem;
    private int movesMade;
    private boolean won;

    private List<Integer> gridSize;

    private List<PluginInterface> moveListeners;
    private List<PluginInterface> acquireListeners;
    private List<PluginInterface> buttonListeners;
    private List<PluginInterface> obstacleTraversalListeners;

    public GameManager(UIManager uiManager, GridArea area, 
    GridAreaIcon playerIcon, List<List<GameObject>> objectMap, 
    List<GameObject> itemList, List<List<GridAreaIcon>> fogMap,
    Image imgObstacle) {
        this.uiManager = uiManager;
        this.area = area;
        this.playerIcon = playerIcon;
        
        this.itemList = itemList;
        this.objectMap = objectMap;
        this.fogMap = fogMap;
        this.imgObstacle = imgObstacle;

        this.inventory = new ArrayList<>();
        this.recentItem = null;
        this.movesMade = 0;
        this.won = false;

        int rows = objectMap.size();
        int cols = rows > 0 ? fogMap.get(0).size() : 0;
        this.gridSize = new ArrayList<>(List.of(rows, cols));

        this.moveListeners = new ArrayList<>();
        this.acquireListeners = new ArrayList<>();
        this.buttonListeners = new ArrayList<>();
        this.obstacleTraversalListeners = new ArrayList<>();
    }

    public void makeMove(int xDiff, int yDiff) {

        if (won) { return; }
        uiManager.clearStatusText();

        boolean move = false;

        int x = (int) playerIcon.getX();
        int y = (int) playerIcon.getY();
        int newX = x + xDiff; // proposed new x
        int newY = y + yDiff; // proposed new y

        try {
            GameObject newPosObj = objectMap.get(newX).get(newY);
            if (newPosObj == null) { // empty, not OOB
                move = true;
            } 
            else if ("trophy".equals(newPosObj.getType())) {
                // notify win, also not allow further interaction/moving
                uiManager.notifyWin(movesMade + 1);
                move = true;
                won = true;
            }
            else if ("item".equals(newPosObj.getType())) {
                pickUpItem(newX, newY);
                notifyAcquireListeners();
                move = true;
            }
            else if ("obstacle".equals(newPosObj.getType())) {
                move = tryPassObstacle(newX, newY);
                if (move) { notifyObstacleTraversalListeners(); }
            }
        } catch(IndexOutOfBoundsException e) { // Out of bounds
            uiManager.notifyOOB();
        }

        if (move) {
            playerIcon.setPosition(newX, newY);
            area.requestLayout();
            movesMade++;
            uiManager.nextDay(movesMade);
            removeFogAround(newX, newY);
            if (won == false) { notifyMoveListeners(); }
        }
    }

    // Re-render GridArea on player move/obstacle traversal/item acquirement
    @Override
    public void refreshGridLayout() {
        area.requestLayout();
    }

    // #region GENERAL

    // Pick up item at a position
    public void pickUpItem(int x, int y) {
        // Add item to inventory
        // Invoke UI Manager method
        // remove from GridArea and object map
        Item item = (Item) objectMap.get(x).get(y);
        String itemName = item.getName(); // in format 'item.___'
        String itemMessage = item.getMessage();
        GridAreaIcon itemIcon = item.getIcon();

        inventory.add(itemName);
        recentItem = itemName;

        // add to UIManager internal inventory, display item acquired,
        // refresh inventory display
        uiManager.addItem(itemName, itemMessage, false);

        objectMap.get(x).set(y, null); // remove from objectMap
        itemIcon.setShown(false); // just in case
        area.getIcons().remove(itemIcon); // remove from screen
    }

    // Try to pass/traverse an obstacle
    public boolean tryPassObstacle(int x, int y) {
        Obstacle obstacle = (Obstacle) objectMap.get(x).get(y);
        List<String> requiredItems = obstacle.getRequires();
        GridAreaIcon obstacleIcon = obstacle.getIcon();

        boolean itemMissing = false;

        // Determine if player has required items
        for (String requiredItem : requiredItems) {
            boolean found = false;
            for (String item : inventory) {
                if (Normalizer.normalize(item, Normalizer.Form.NFKC)
                        .equals(Normalizer.normalize(requiredItem, Normalizer.Form.NFKC))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                itemMissing = true;
                break;
            }
        }

        if (itemMissing) { // can't pass obstacle
            uiManager.youShallNotPass(requiredItems);
        }
        else {
            obstacleIcon.setShown(false);
            objectMap.get(x).set(y, null); // remove from objectMap
            area.getIcons().remove(obstacleIcon); // remove from GRidArea
        }

        return !itemMissing;
    }

    // remove fog around a position, includes position and orthogonal
    @Override
    @SuppressWarnings("PMD.EmptyCatchBlock")  // Out of game area - do nothing
    public void removeFogAround(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                try {
                    int newX = x + dx;
                    int newY = y + dy;

                    if (Math.abs(dx) + Math.abs(dy) > 1) { continue; }
                    //if (dx == 0 && dy == 0) { continue; }

                    GridAreaIcon fogIcon = fogMap.get(newX).get(newY);
                    if (fogIcon != null) {
                        fogIcon.setShown(false);
                        area.getIcons().remove(fogIcon);
                        fogMap.get(newX).set(newY, null);
                    }
                } catch (IndexOutOfBoundsException e) { 
                    // Can't remove fog outside of game area
                }
            }
        }
    }

    @Override
    public GridAreaIcon getPlayerIcon() {
        return this.playerIcon;
    }

    @Override
    public List<Integer> getGridSize() {
        return this.gridSize;
    }

    @Override
    public List<String> getInventory() {
        return this.inventory;
    }

    @Override
    public void addToInventory(String item) {
        this.inventory.add(item); // item in format 'item._____'
    }

    @Override
    public String getLastItem() {
        return this.recentItem; // Last acquired
    }

    @Override
    public List<GameObject> getItemList() {
        return this.itemList; // List of items remaining on map
    }

    @Override
    @SuppressWarnings("PMD.EmptyCatchBlock")  // Out of game area - do nothing
    public GameObject getGridSquareContent(int x, int y) {
        try {
            return this.objectMap.get(x).get(y);
        } catch (IndexOutOfBoundsException e) { 
            // Can't get content if out of game area
        }
        return null;
    }

    @Override
    @SuppressWarnings("PMD.EmptyCatchBlock")  // Out of game area - do nothing
    public boolean isGridSquareEmpty(int x, int y) {
        try {
            if(this.objectMap.get(x).get(y) == null) {
                return true;
            }
        } catch (IndexOutOfBoundsException e) { 
            // Out of game area
        }
        return false;
    }

    @Override
    @SuppressWarnings("PMD.EmptyCatchBlock")  // Out of game area - do nothing
    public void setGridSquareContent(int x, int y, GameObject content) {
        try {
            this.objectMap.get(x).set(y, content);
        } catch (IndexOutOfBoundsException e) { 
            // Out of game area
        }
    }

    @Override
    @SuppressWarnings("PMD.EmptyCatchBlock")  // Out of game area - do nothing
    public boolean getGridSquareVisibility(int x, int y) {
        try {
            GridAreaIcon fogIcon = this.fogMap.get(x).get(y);
            if (fogIcon == null) {
                return true;
            }
            return fogIcon.isShown();
        } catch (IndexOutOfBoundsException e) { 
            // Out of game area
        }
        return true;
    }

    @Override
    @SuppressWarnings("PMD.EmptyCatchBlock") // Out of game area - do nothing
    public void setGridSquareVisibility(int x, int y, boolean visible) {
        try {
            GridAreaIcon fogIcon = this.fogMap.get(x).get(y);
            if (fogIcon != null) {
                fogIcon.setShown(visible);
                if (visible == false) {
                    area.getIcons().remove(fogIcon);
                    fogMap.get(x).set(y, null);
                }
            }
        } catch (IndexOutOfBoundsException e) { 
            // Out of game area
        }
    }

    @Override
    public void addRewardItemToInventory(String itemName, String itemMessage) {
        this.inventory.add(itemName);
        uiManager.addItem(itemName, itemMessage, true);
    }

    // 'requires' items must be in format 'item.____'
    @Override
    public void addPenaltyObstacleAtPosition(int x, int y, List<String> requires) {
        if (objectMap.get(x).get(y) == null && !(playerIcon.getX() == x && playerIcon.getY() == y)) {
            GridAreaIcon icon = new GridAreaIcon(
                x, 
                y, 
                0, 1, 
                imgObstacle, 
                ""
            );
            area.getIcons().add(icon);
            Obstacle obstacle = new Obstacle(requires, icon);
            objectMap.get(x).set(y, obstacle);
        }
    }

    @Override
    public void setPlayerPosition(int x, int y) { 
        if (objectMap.get(x).get(y) == null) {
            playerIcon.setPosition(x, y);
        }
    }

    // #endregion

    // #region PLUGIN STUFF

    @Override
    public void registerMoveListener(PluginInterface plugin) {
        moveListeners.add(plugin);
    }

    @Override
    public void registerAcquireListener(PluginInterface plugin) {
        acquireListeners.add(plugin);
    }

    @Override
    public void registerButtonListener(PluginInterface plugin) {
        buttonListeners.add(plugin);
    }

    @Override
    public void registerObstacleTraversalListener(PluginInterface plugin) {
        obstacleTraversalListeners.add(plugin);
    }

    public void notifyMoveListeners() {
        for (PluginInterface plugin : moveListeners) {
            plugin.onMove();
        }
    }

    public void notifyAcquireListeners() {
        for (PluginInterface plugin : acquireListeners) {
            plugin.onAcquire();
        }
    }

    public void notifyButtonListeners() {
        for (PluginInterface plugin : buttonListeners) {
            plugin.onButton();
        }
    }

    public void notifyObstacleTraversalListeners() {
        for (PluginInterface plugin : obstacleTraversalListeners) {
            plugin.onObstacleTraversal();
        }
    }

    // #endregion

    
}

