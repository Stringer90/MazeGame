package edu.curtin.saed.assignment2;

public class Trophy implements GameObject {
    
    private GridAreaIcon icon;
    private String type;

    public Trophy(GridAreaIcon icon){ 
        this.icon = icon;
        this.type = "trophy";
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
