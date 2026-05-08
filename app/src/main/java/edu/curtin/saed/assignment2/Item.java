package edu.curtin.saed.assignment2;

public class Item implements GameObject {
    
    private String name;
    private String message;
    private GridAreaIcon icon;
    private String type;

    public Item(String name, String message, GridAreaIcon icon){ 
        this.name = name;
        this.message = message;
        this.icon = icon;
        this.type = "item";
    }

    public String getName() {
        return this.name;
    }

    public String getMessage() {
        return this.message;
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

