package edu.curtin.gameplugins;

import java.util.List;

import edu.curtin.saed.assignment2.ApiInterface;
import edu.curtin.saed.assignment2.GameObject;
import edu.curtin.saed.assignment2.PluginInterface;

public class Reveal implements PluginInterface {

    private ApiInterface api;

    @SuppressWarnings("this-escape") // No real functional effect, just to get rid of the warning.
    public Reveal(ApiInterface api) {
        this.api = api;
        this.api.registerAcquireListener(this);
    }

    // If latest item acquired includes 'map',
    // Use list of items to get their positions, then remove fog on those positions
    @Override
    public void onAcquire() {
        String latestItem = api.getLastItem();
        if (latestItem.toLowerCase().contains("map")) {
            List<GameObject> gameObjects = api.getItemList();
            for(GameObject object : gameObjects) {
                int x = (int) object.getX();
                int y = (int) object.getY();
                api.setGridSquareVisibility(x, y, false);
            }
        }
    }
}
