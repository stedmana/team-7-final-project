package tasks;

/**
 *Used to store robot position. 
 *
 */
public class Position {
	
	public double x;
	public double y;

	/**
	 * Constructor for position class
	 * 
	 * @param x - cartesian
	 * @param y - cartesian
	 */
	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * get x value
	 * 
	 * @return x value
	 */
	public double getX() {
		return x;
	}
	/**
	 * get y value
	 * 
	 * @return y value
	 */
	public double getY() {
		return y;
	}
	
}
