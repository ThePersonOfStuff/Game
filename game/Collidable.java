package game;

public interface Collidable {
    public double getX();
    public double getY();
    public double getWidth();
    public double getHeight();
    public void setX(double x);
    public void setY(double y);
    public int getLevelID();

    public default boolean collidesWith(Collidable other) {
        if(other.getLevelID() != getLevelID()) {
            return false;
        }
        double minXDist = (getWidth() + other.getWidth())/2; 
        double minYDist = (getHeight() + other.getHeight())/2;
        return Math.abs(getX() - other.getX()) < minXDist && Math.abs(getY() - other.getY()) < minYDist;
    }
}
