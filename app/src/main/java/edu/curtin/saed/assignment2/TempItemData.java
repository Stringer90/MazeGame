package edu.curtin.saed.assignment2;

import java.util.List;

public class TempItemData {
    public String name;
    public List<List<Integer>> positions;
    public String message;

    public TempItemData(String name, List<List<Integer>> positions, String message) {
        this.name = name;
        this.positions = positions;
        this.message = message;
    }
}
