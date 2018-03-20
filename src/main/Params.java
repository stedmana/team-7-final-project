package main;

public class Params {
<<<<<<< HEAD
	/*
	 * This class is to store all the parameters for the robot. Only store things here
	 * that will not change. i.e descriptions about our hardware, regular constants like speed
	 * etc. Put units beside each value.
	 */
	public static double TRACK = 10.9; // cm
	public static double WHEEL_RAD = 1.6; // cm
	public static double SENSOR_DIST = 0; // cm
	public static int SPEED = 0; // deg/sec
	public static int ACCEL = 0; // deg/sec/sec
	public static double TILE_LENGTH = 30.48; // cm
	public static int TEAM_ID = 7;
	// Corner location  parameters
	public final static double cornerParams[][] = {
			{    TILE_LENGTH,     TILE_LENGTH,  90},
			{7 * TILE_LENGTH,     TILE_LENGTH,   0},
			{7 * TILE_LENGTH, 7 * TILE_LENGTH, 270},
			{    TILE_LENGTH, 7 * TILE_LENGTH, 180}
	};
=======
    /*
     * This class is to store all the parameters for the robot. Only store things here
     * that will not change. i.e descriptions about our hardware, regular constants like speed
     * etc. Put units beside each value.
     */
    public static double TRACK = 10.9; // cm
    public static double WHEEL_RAD = 1.6; // cm
    public static double SENSOR_DIST = 0; // cm
    public static int SPEED = 0; // deg/sec
    public static int ACCEL = 0; // deg/sec/sec
    public static double TILE_LENGTH = 30.48; // cm
    public static int TEAM_ID = 7;
    // Corner location  parameters
    public final static double cornerParams[][] = {
        {    TILE_LENGTH,     TILE_LENGTH,  90},
        {7 * TILE_LENGTH,     TILE_LENGTH,   0},
        {7 * TILE_LENGTH, 7 * TILE_LENGTH, 270},
        {    TILE_LENGTH, 7 * TILE_LENGTH, 180}
};
>>>>>>> ADD DEBUG MENU
}
