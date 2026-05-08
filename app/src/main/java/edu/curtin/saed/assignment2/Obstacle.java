package edu.curtin.saed.assignment2;

import java.util.List;

public class Obstacle implements GameObject {
    
    private GridAreaIcon icon;
    private String type;
    private List<String> requires;

    public Obstacle(List<String> requires, GridAreaIcon icon){ 
        this.icon = icon;
        this.requires = requires;
        this.type = "obstacle";
    }

    public List<String> getRequires() {
        return this.requires;
    }

    public GridAreaIcon getIcon() {
        return this.icon;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public double getX() {
        return this.icon.getX();
    }

    @Override
    public double getY() {
        return this.icon.getY();
    }

    public void setPos(double newX, double newY) {
        this.icon.setPosition(newX, newY);
    }

    public void setShown(boolean shown)
    {
        this.icon.setShown(shown);
    }
}
