package edu.curtin.gameplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import edu.curtin.saed.assignment2.ApiInterface;
import edu.curtin.saed.assignment2.GridAreaIcon;
import edu.curtin.saed.assignment2.PluginInterface;

public class Teleport implements PluginInterface {
    
    private ApiInterface api;

    @SuppressWarnings("this-escape") // No real functional effect, just to get rid of the warning.
    public Teleport(ApiInterface api) {
        this.api = api;
        this.api.registerButtonListener(this);
    }

    /*
     * Get grid size and player position
     * Get map of empty positions (x/y coords)
     * Randomly select an empty position to teleport player to
     */
    @Override
    public void onButton() {
        List<Integer> size = api.getGridSize();
        int sizeX = size.get(0);
        int sizeY = size.get(1);
        GridAreaIcon playerIcon = api.getPlayerIcon();
        int playerX = (int) playerIcon.getX();
        int playerY = (int) playerIcon.getY();

        // Get empty positions
        List<List<Integer>> emptyPositions = new ArrayList<>();
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if ((x == playerX && y == playerY)) {
                    continue; // skip player position
                }
                if (api.isGridSquareEmpty(x, y)) {
                    emptyPositions.add(List.of(x, y));
                }
            }
        }

        if (emptyPositions.isEmpty()) { // Don't teleport of all positions non-empty
            return;
        }

        // Get random position to teleport to
        int pick = ThreadLocalRandom.current().nextInt(emptyPositions.size());
        List<Integer> target = emptyPositions.get(pick);
        int targetX = target.get(0);
        int targetY = target.get(1);

        // Set player position, remove fog, refresh GridArea
        // Not a normal move, tf no days passed
        playerIcon.setPosition(targetX, targetY);
        api.removeFogAround(targetX, targetY);
        api.refreshGridLayout();
    }
}
