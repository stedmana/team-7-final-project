package main;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is to store all the parameters for the robot. Only store things here
 * that will not change. i.e descriptions about our hardware, regular constants like speed
 * etc. Put units beside each value.
 * 
 * Constants stored include: SERVER_IP, TRACK, WHEEL_RAD, SENSOR_DIST, SPEED, TURN_SPEED,
 * ACCEL, SEARCH_THRESHOLD, TILE_LENGTH, DIFF_THRESHOLD, TEAM_ID
 *
 */
public class Params {
    /*
     * This class is to store all the parameters for the robot. Only store things here
     * that will not change. i.e descriptions about our hardware, regular constants like speed
     * etc. Put units beside each value.
     */
    final public static String SERVER_IP = "192.168.2.13";
    final public static double TRACK = 10.7; // cm
    final public static double WHEEL_RAD = 1.6; // cm
    final public static double SENSOR_DIST_L = 4.5; // cm
    final public static double SENSOR_DIST_R = SENSOR_DIST_L + 0.4; // cm
    final public static double SENSOR_DIST = (SENSOR_DIST_L + SENSOR_DIST_R) / 2;
    
    final public static int SPEED = 150; // deg/sec
    final public static int TURN_SPEED = 100; // deg/sec
    final public static int ACCEL = 800; // deg/sec/sec
    final public static int SEARCH_THRESHOLD = 80;
    final public static double TILE_LENGTH = 30.48; // cm
    final public static double DIFF_THRESHOLD = -0.2; //change in units per sample
    final public static int TEAM_ID = 7;
    // Corner location  parameters 
    public final static double cornerParams[][] = {
        {    TILE_LENGTH,     TILE_LENGTH,  90},
        {7 * TILE_LENGTH,     TILE_LENGTH,  0},
        {7 * TILE_LENGTH, 7 * TILE_LENGTH, 270},
        {    TILE_LENGTH, 7 * TILE_LENGTH, 180}
    };
    
    final public static int BOARD_SIZE = 8;
    
    // Sample set of competition params so we don't have to connect to the server to test all
    // the time.
    final public static Map<String, Long> debugCompetitionParams;
    
    static
    {
        debugCompetitionParams = new HashMap<String, Long>();
        debugCompetitionParams.put("GreenTeam", (long) TEAM_ID);
        debugCompetitionParams.put("GreenCorner", (long) 1);
        debugCompetitionParams.put("TN_LL_x", (long) 7);
        debugCompetitionParams.put("TN_LL_y", (long) 7);
        debugCompetitionParams.put("TN_UR_x", (long) 9);
        debugCompetitionParams.put("TN_UR_y", (long) 9);
        debugCompetitionParams.put("BR_LL_x", (long) 5);
        debugCompetitionParams.put("BR_LL_y", (long) 3);
        debugCompetitionParams.put("BR_UR_x", (long) 6);
        debugCompetitionParams.put("BR_UR_y", (long) 5);
        
        debugCompetitionParams.put("SR_LL_x", (long) 1);
        debugCompetitionParams.put("SR_LL_y", (long) 4);
        debugCompetitionParams.put("SR_UR_x", (long) 2);
        debugCompetitionParams.put("SR_UR_y", (long) 6);
        
        debugCompetitionParams.put("SG_LL_x", (long) 4);
        debugCompetitionParams.put("SG_LL_y", (long) 1);
        debugCompetitionParams.put("SG_UR_x", (long) 6);
        debugCompetitionParams.put("SG_UR_y", (long) 2);
    }
}
