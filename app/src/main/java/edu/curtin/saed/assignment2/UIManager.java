package edu.curtin.saed.assignment2;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.scene.control.*;
import javafx.stage.Stage;

public class UIManager {

    private Stage stage;
    private Button abilityBtn;
    private Label statusText;
    private TextArea inventoryTextArea;
    private Button applyBtn;
    private Label dateLabel;

    // Just so UI can print inv items easier
    // Stores strings in format 'item._____'
    private List<String> inventory; 

    @SuppressWarnings("PMD.SingularField") // To get rid of can use as a local variable warning, but need it.
    private Locale currentLocale;
    private ResourceBundle currentBundle;
    private LocalDate gameDate;
    private DateTimeFormatter dateFormatter;

    private Locale baseLocale;
    private DateTimeFormatter baseDateFormatter;

    /*
     * Handle UI refreshes
     * Handle Date refreshes, including changing date
     * Handle locale changes
     * Handle notifications to user on events
     */
    public UIManager(
            Button abilityBtn, Label statusText,
            TextArea inventoryTextArea, 
            Button applyBtn, Stage stage, Label dateLabel) {
        this.stage = stage;
        this.abilityBtn = abilityBtn;
        this.statusText = statusText;
        this.inventoryTextArea = inventoryTextArea;
        this.applyBtn = applyBtn;
        this.dateLabel = dateLabel;

        this.inventory = new ArrayList<>();

        this.baseLocale = Locale.getDefault();
        this.baseDateFormatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(baseLocale);

        this.currentLocale = baseLocale;
        this.currentBundle = ResourceBundle.getBundle("bundle", baseLocale);
        this.dateFormatter = baseDateFormatter;
        this.gameDate = LocalDate.now();
    }

    // Refresh date label. 
    // 'Date: ' part, and date on locale change or day increment
    public void refreshDateLabel() {
        String prefix = currentBundle.getString("ui.date_label");

        String formatted = dateFormatter.format(gameDate);
        dateLabel.setText(prefix + ": " + formatted);
    }

    // Increment day, refresh date
    public void nextDay(int dayNum) {
        gameDate = gameDate.plusDays(1);
        refreshDateLabel();
    }

    // Change locale, only if it is a valid ietf tag
    // May only change date format, not translation
    public boolean applyLocaleFromTag(String ietfTag) {
        String tag = ietfTag == null ? "" : ietfTag.trim();
        Locale newLocale = Locale.forLanguageTag(tag);

        boolean representsLocale =
                !(isEmpty(newLocale.getLanguage()) &&
                  isEmpty(newLocale.getScript()) &&
                  isEmpty(newLocale.getCountry()));

        if (!representsLocale) {
            currentLocale = baseLocale;
            currentBundle = ResourceBundle.getBundle("bundle", baseLocale);
            dateFormatter = baseDateFormatter;
            refreshDateLabel();
            return false;
        }

        currentLocale = newLocale;

        currentBundle = ResourceBundle.getBundle("bundle", currentLocale);

        dateFormatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(currentLocale);

        refreshUi();

        return true;
    }

    // Refresh whole of UI
    public void refreshUi() {
        refreshDateLabel();
        refreshInvDisp();
        statusText.setText("");
        stage.setTitle(currentBundle.getString("ui.title"));

        abilityBtn.setText(currentBundle.getString("ui.ability_button"));
        applyBtn.setText(currentBundle.getString("ui.apply_button"));
    }

    // Refresh inventory display, when player acquires an item
    public void refreshInvDisp() {
        inventoryTextArea.setText("");
        inventoryTextArea.appendText(currentBundle.getString("ui.inventory_title") + "\n");
        for (String itemName : inventory) { // item name in form "item._____"
            inventoryTextArea.appendText(currentBundle.getString(itemName) + "\n");
        }
    }

    // To help reduce clog in applyLocaleFromTag()
    private static boolean isEmpty(String s) { return s == null || s.isEmpty(); }

    // Notify user of win
    public void notifyWin(int daysPassed) {
        statusText.setText(currentBundle.getString("ui.win_message")
                            + " "
                            + currentBundle.getString("ui.days_passed")
                            + ": "
                            + Integer.toString(daysPassed) + "!");
    }

    // Notify user they have tried to move out of bounds
    public void notifyOOB() { 
        statusText.setText(currentBundle.getString("ui.out_of_bounds"));
    }

    // Notify user they cannot traverse an obstacle, also display required items
    public void youShallNotPass(List<String> requiredItems) {
        String text = currentBundle.getString("ui.obstacle_prefix") + " ";

        List<String> localisedItems = new ArrayList<>();
        for (String itemName : requiredItems) {
            localisedItems.add(currentBundle.getString(itemName));
        }

        String itemList;
        int size = localisedItems.size();
        if (size == 1) {
            itemList = localisedItems.get(0);
        } else if (size == 2) {
            itemList = localisedItems.get(0) + " " 
                    + currentBundle.getString("ui.and") + " " 
                    + localisedItems.get(1);
        } else {
            itemList = String.join(", ", localisedItems.subList(0, size - 1))
                    + " " + currentBundle.getString("ui.and") + " "
                    + localisedItems.get(size - 1);
        }

        text += itemList + " " + currentBundle.getString("ui.obstacle_suffix");
        statusText.setText(text);
    }

    // Clear status text, so notifications don't linger after player moves
    public void clearStatusText() {
        statusText.setText("");
    }

    // Add item to UIManager's invenory list
    // Also notify user that item has been acquired
    // Displays differently if it is a reward item
    // If reward item, and other item acquired at same time, display both at same time
    public void addItem(String item, String itemMessage, boolean isRewardItem) {
        inventory.add(item);
        String text;
        if (isRewardItem) {
            text = currentBundle.getString("ui.reward_acquired")
                    + " " + currentBundle.getString(item)
                    + " " + currentBundle.getString("ui.acquired")
                    + " " + itemMessage;
        }
        else {
            text = currentBundle.getString(item)
                    + " " + currentBundle.getString("ui.acquired")
                    + " " + itemMessage;
        }

        if (statusText.getText() != "") {
            text = statusText.getText() + "\n" + text;
        }

        statusText.setText(text);

        refreshInvDisp();
    }




}
