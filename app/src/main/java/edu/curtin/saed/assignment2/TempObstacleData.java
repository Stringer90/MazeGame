package edu.curtin.saed.assignment2;

import java.util.List;

public class TempObstacleData {
    public List<List<Integer>> positions;
    public List<String> requires;

    public TempObstacleData(List<List<Integer>> positions, List<String> requires) {
        this.positions = positions;
        this.requires = requires;
    }
}
