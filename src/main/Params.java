package main;

public class Params {
    /*
     * This class is to store all the parameters for the robot. Only store things here
     * that will not change. i.e descriptions about our hardware, regular constants like speed
     * etc. Put units beside each value.
     */
    final public static double TRACK = 10.7; // cm
    final public static double WHEEL_RAD = 1.6; // cm
    final public static double SENSOR_DIST = 4.5; // cm
    final public static int SPEED = 200; // deg/sec
    final public static int TURN_SPEED = 100; // deg/sec
    final public static int ACCEL = 800; // deg/sec/sec
    final public static double TILE_LENGTH = 30.48; // cm
    final public static double DIFF_THRESHOLD = -0.08; //change in units per sample
    final public static int TEAM_ID = 7;
    // Corner location  parameters 
    public final static double cornerParams[][] = {
        {    TILE_LENGTH,     TILE_LENGTH,  0},
        {7 * TILE_LENGTH,     TILE_LENGTH,  0},
        {7 * TILE_LENGTH, 7 * TILE_LENGTH, 180},
        {    TILE_LENGTH, 7 * TILE_LENGTH, 180}
    };
    
    final public static int BOARD_SIZE = 8;
}
