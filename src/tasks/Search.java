package tasks;
import navigation.Navigate;
import odometer.*;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import main.Params;
import ca.mcgill.ecse211.detectColor.*;
import fsm.Task;
import lejos.hardware.lcd.TextLCD;

/**
 * The search class is used to search for coloured blocks within the defined search area. 
 * The search algorithm first traverses the outside of the search area while scanning inwards
 * with the ultrasonic sensor to create a 2D map of the blocks in space. After, the robot navigates
 * to the block positions indicated in the 2D map, and scanning each block with a colour sensor. This task
 * is time-restricted, and will abort if the search takes too long.
 *
 */
public class Search extends Thread implements Task {
	
	private static double llx;
	private static double lly;
	private static double urx;
	private static double ury;
	
	boolean stop;
	
	public boolean outOfTime; 
	
	private static double[] blocks = new double[8]; //can hold 4 position values	
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	
	private static Navigate nav;
	SampleProvider ultraSonic;
	
	private double xDiff;
	private double yDiff;
	
	private int distance;
	
	private double senseDiff;
	
	private static Odometer odo;
	
	DetectColor col;
	
	int colour;

	int targetColour;
	
	float data[];
	
	boolean taskSuccess;
	 private static final TextLCD lcd = LocalEV3.get().getTextLCD();

	
	SampleProvider left;
	SampleProvider right;
		

	/**
	 * Constructor for the search class
	 * 
	 * @param ultraSonic the ultrasonic sensor
	 * @param odo the odometer object
	 * @param nav the navigation object
	 * @param leftMotor the left motor
	 * @param rightMotor the right motor
	 * @param left
	 * @param right
	 * @param col colour detection object from detectColour package
	 * @param targetColor a number between 1 and 4, representing the colour of the object (blue = 1, red = 2, yellow = 3, white = 4)
	 * @param llx x position on the grid of the lower left corner of the search area
	 * @param lly y position on the grid of the lower left corner of the search area
	 * @param urx x position on the grid of the upper right corner of the search area
	 * @param ury y position on the grid of the upper right corner of the search area
	 */
	public Search(SampleProvider ultraSonic, Odometer odo, Navigate nav, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, 
			SampleProvider left, SampleProvider right, DetectColor col, int targetColor, double llx, double lly, double urx, double ury) {
		
		
		this.llx = llx;
		this.lly = lly;
		this.urx = urx;
		this.ury = ury;
		
		this.nav = nav;
		
		double xDiff = urx - llx;
		double yDiff = ury - lly;
		
		this.odo = odo;
		this.ultraSonic = ultraSonic;
		
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		
		this.col = col;
		
		colour = 0;
		
		outOfTime = false;
		
		this.targetColour = targetColour;
		
		this.left = left;
		this.right = right;
		
		this.senseDiff = (urx - llx)*Params.TILE_LENGTH;
		
		data = new float[ultraSonic.sampleSize()];
		
		this.stop = false;
			
	}
	
	
	/**
	 *Enables the robot to travel to each corner of the search area,
	 *and then probe for blocks afterwards 
	 *
	 *@param prevTaskSuccess
	 *@return boolean if task is successful
	 */
	public boolean start(boolean prevTaskSuccess) {
		
		lcd.clear();
		
		taskSuccess = false;
		double prevOdoValx;
		double prevOdoValy;
		
	int i = 0;
		
		//might be completed by previous task
		nav.travelTo(llx, lly, 90, false);
	
		//travel along bottom of search area
		
		while(odo.getXYT()[0] <= urx*Params.TILE_LENGTH) {
			nav.travelTo(urx, lly, 0, false);
//			prevOdoValx = odo.getXYT()[0];
//			double odoDiffx = odo.getXYT()[0] - prevOdoValx;
			lcd.clear();
			ultraSonic.fetchSample(data, 0);
			if((data[0] <= Params.SEARCH_THRESHOLD) /*&& (odoDiffx >= 7)*/) {
				lcd.drawString("" + data[0], 2, 2);
				
				Sound.beep();
				blocks[i] = odo.getXYT()[0]; //stores x coordinate of block
				blocks[i+1] = (double)data[0] + odo.getXYT()[1]; // stores approximate y coordinate of block
				i += 2;
				nav.travelTo(blocks[i], blocks[i+1], 0, false);
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
				//hopefully will pause the ultrasonic controller while keeping the motors rolling
				//so a block will take up two spaces in the array - the first space for the x-Position, second 
				//for the y-Position
			}
		}
		
		
		//travel up from bottom right corner
		while(odo.getXYT()[1] <= ury*Params.TILE_LENGTH) {
//			prevOdoValy = odo.getXYT()[1];
//			double odoDiffy = odo.getXYT()[1] - prevOdoValy;
			
			nav.travelTo(urx, ury, 90, false);
			ultraSonic.fetchSample(data, 0);
			if((data[0] <= senseDiff) /*&& (odoDiffy >= 7)*/) {
				Sound.beep();
				blocks[i] = (double)data[0] + odo.getXYT()[0];
				blocks[i+1] = odo.getXYT()[1];
				i += 2;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
		int val = probe(targetColour);
		if(val == 1) {
			taskSuccess = true;
		} else {
			taskSuccess = false;
		}
		return taskSuccess;
	}
	
	
	/**
	 * Drives to each object saved in the 2d map, in order to identify colour.
	 * 
	 * @param targetColour an int corresponding to a blue, red, yellow or white black
	 * @return int indicating if the target colour was found: 0 = false, 1 = true.
	 */
	public int probe(int targetColour) { //have color detection running in the background
		
		int val = 0;
		//0 is false, 1 is true
		
		float data[] = new float[ultraSonic.sampleSize()];
		
		for(int i = 0; i < 8; i += 2) {
			if(blocks[i] == 0 && blocks[i+1] == 0) { //the only way both values will be 0 is if there are no more blocks recorded
				val = 0;
				return val; 
			} else if(blocks[i] == 0.0f) { //assuming they're in a row, so x is 0; use blocks[i-1] and blocks[i-2]
				int j = i;
				int temp = 1; //start at one so no issue with incrementing it after loop
				while(blocks[j+2] == 0) {
					temp++;
				}
				//ends when it finds blocks[i-2] != 0 which is what we want
				nav.travelTo(blocks[i+(temp*2)], blocks[i+1], 0, false); //should take care of the columns case
			} else if(blocks[i+1] == 0.0f) { //assuming they're in a column, so y is 0; 
				int j = i;
				int temp = 1; //start at one so no issue with incrementing it after loop
				while(blocks[(j+1)-2] == 0) {
					temp++;
				}
				nav.travelTo(blocks[(i+1)-(temp*2)], blocks[i+1], 0, false); //should take care of the columns case
			} else { //tbh the above conditions should never happen...
				nav.travelTo((double)blocks[i], (double)blocks[i+1], 0, false); //TODO: include offset so robot does not drive into block

			}
				
			colour = col.detectC();
			try {
				Thread.sleep(500); //to allow the colour detection to be accurate
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			if(colour == targetColour) {
				Sound.beep();
				Sound.beep();
				Sound.beep();
				val = 1; //TODO: change to bool
			}
		}
	return val;
	}
	
//	@Override
//	public void stop() {
//		//force the program to exit - perhaps use EXIT;
//		stop = true;
//	}
	
	public SampleProvider getSampleProvider() {
		return this.ultraSonic;
	}
	
	public float[] getData() {
		return this.data;
	}
	
//	@Override
//	public void processUSData(int distance) {
//		this.distance = distance;
//
//	}
//
//	@Override
//	public int readUSDistance() {
//		return this.distance;
//	}
	
}
