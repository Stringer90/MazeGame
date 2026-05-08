package edu.curtin.saed.assignment2;

import java.util.List;

public interface ApiInterface {

    void removeFogAround(int x, int y);

    void refreshGridLayout();

    GridAreaIcon getPlayerIcon();

    List<Integer> getGridSize();

    List<String> getInventory();

    void addToInventory(String item);

    String getLastItem();

    List<GameObject> getItemList();

    GameObject getGridSquareContent(int x, int y);

    boolean isGridSquareEmpty(int x, int y);

    void setGridSquareContent(int x, int y, GameObject content);

    boolean getGridSquareVisibility(int x, int y);

    void setGridSquareVisibility(int x, int y, boolean visible);

    void registerMoveListener(PluginInterface plugin);

    void registerAcquireListener(PluginInterface plugin);

    void registerButtonListener(PluginInterface plugin);

    void registerObstacleTraversalListener(PluginInterface plugin);

    void addRewardItemToInventory(String itemName, String itemMessage);

    void addPenaltyObstacleAtPosition(int x, int y, List<String> requires);

    void setPlayerPosition(int x, int y);
}
