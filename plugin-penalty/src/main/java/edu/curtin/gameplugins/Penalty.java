package edu.curtin.gameplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import edu.curtin.saed.assignment2.ApiInterface;
import edu.curtin.saed.assignment2.GridAreaIcon;
import edu.curtin.saed.assignment2.PluginInterface;

public class Penalty implements PluginInterface {

    private ApiInterface api;
    private long previousMoveTime;

    @SuppressWarnings("this-escape") // No real functional effect, just to get rid of the warning.
    public Penalty(ApiInterface api) {
        this.api = api;
        this.api.registerMoveListener(this);

        this.previousMoveTime = 0L; // Give player chance to move at least once
    }

    // If player not moved for 5 seconds, create penalty obstacle
    @Override
    public void onMove() {
        long curMoveTime = System.currentTimeMillis();
        if ((Math.abs(curMoveTime - previousMoveTime) > 5000) && (this.previousMoveTime != 0L)) {
            createPenaltyObstacle();
        }
        previousMoveTime = curMoveTime;
    }

    public void createPenaltyObstacle() {
        /*
         * get player pos
         * search around player for available space (null)
         * on random one, place a penalty obstacle
         */
        GridAreaIcon playerIcon = api.getPlayerIcon();
        int playerPosX = (int) playerIcon.getX();
        int playerPosY = (int) playerIcon.getY();

        List<int[]> emptyAdj = new ArrayList<>(4);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int newX = playerPosX + dx;
                int newY = playerPosY + dy;

                if (Math.abs(dx) + Math.abs(dy) > 1) { continue; } // skip diagonals
                if (dx == 0 && dy == 0) { continue; }              // skip player's own tile

                if (api.isGridSquareEmpty(newX, newY)) {
                    emptyAdj.add(new int[] { newX, newY });
                }
            }
        }

        if (emptyAdj.isEmpty()) {
            return; // no emptyadjacent squares
        }

        int pick = ThreadLocalRandom.current().nextInt(emptyAdj.size());
        int[] chosen = emptyAdj.get(pick);
        int targetX = chosen[0];
        int targetY = chosen[1];
        List<String> requires = new ArrayList<>(
            List.of(
                "item.pickaxe"
            )
        );

        api.addPenaltyObstacleAtPosition(targetX, targetY, requires);
    }
}
