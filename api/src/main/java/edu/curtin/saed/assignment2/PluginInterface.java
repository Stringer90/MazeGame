package edu.curtin.saed.assignment2;

public interface PluginInterface {
    default void onMove() {}
    default void onAcquire() {}
    default void onButton() {}
    default void onObstacleTraversal() {}
}

