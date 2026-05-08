package edu.curtin.saed.assignment2;

public interface GameObject {

    String getType();
    double getX();
    double getY();
    // Marker to identify game objects, i.e. items, obstacles, trophy.
    // So that they can be stored in same 2D array.
}
