package tasks;
import navigation.Navigate;
import odometer.*;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import main.Params;
import ca.mcgill.ecse211.detectColor.*;
import fsm.Task;

public class Search implements Task {
	
	private static double llx;
	private static double lly;
	private static double urx;
	private static double ury;
	
	public boolean outOfTime; 
	
	private static float[] blocks = new float[8]; //can hold 4 position values	
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	
	private static Navigate nav;
	SampleProvider ultraSonic;
	
	private double xDiff;
	private double yDiff;
	
	private double senseDiff;
	
	private static Odometer odo;
	
	DetectColor col;
	
	int colour;

	int targetColour;
	
	boolean taskSuccess;
	
	SampleProvider left;
	SampleProvider right;
	
	/**
	 * Creates the search class, which enables the robot to createa 2-d map of the search area,
	 * with all blocks included within it
	 * @param ultraSonic - the ultrasonic sensor
	 * 
	 * @param odo - the odometer object
	 * 
	 * @param nav - the navigation object
	 * 
	 * @param llx - x position on the grid of the lower left corner of the search area
	 * @param lly - y position on the grid of the lower left corner of the search area
	 * 
	 * @param urx - x position on the grid of the upper right corner of the search area
	 * @param ury - y position on the grid of the upper right corner of the search area
	 * 
	 * @param leftMotor - the left EV3 motor
	 * @param rightMotor - the right EV3 motor
	 * 
	 * @param col - a colour detection object from the detectColour package
	 * @param targetColour - a number between 1 and 4, representing the
	 *  colour of the object (blue = 1, red = 2, yellow = 3, white = 4)
	 * */
	
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
			
	}
	
	
	/**
	 *Enables the robot to travel to each corner of the search area,
	 *and then probe for blocks afterwards */
	public boolean start(boolean prevTaskSuccess) {
		
		taskSuccess = false;
		double prevOdoValx;
		double prevOdoValy;
		
	float data[] = new float[ultraSonic.sampleSize()];
	int i = 0;
		
		//might be completed by previous task
		nav.travelTo(llx, lly, 90, true);
	
		//travel along bottom of search area
		
		while(odo.getXYT()[0] != urx) {
			nav.travelTo(urx, lly, 0, true);
			prevOdoValx = odo.getXYT()[0];
			double odoDiffx = odo.getXYT()[0] - prevOdoValx;
			
			ultraSonic.fetchSample(data, 0);
			if((data[0] <= Params.SEARCH_THRESHOLD) && (odoDiffx >= 7)) {
				Sound.beep();
				blocks[i] = (float)odo.getXYT()[0]; //stores x coordinate of block
				blocks[i+1] = data[0] + (float)odo.getXYT()[1]; // stores approximate y coordinate of block
				i += 2;
				//hopefully will pause the ultrasonic controller while keeping the motors rolling
				//so a block will take up two spaces in the array - the first space for the x-Position, second 
				//for the y-Position
			}
		}
		
		
		//travel up from bottom right corner
		while(odo.getXYT()[1] != ury) {
			prevOdoValy = odo.getXYT()[1];
			double odoDiffy = odo.getXYT()[1] - prevOdoValy;
			
			nav.travelTo(urx, ury, 270, true);
			ultraSonic.fetchSample(data, 0);
			if((data[0] <= senseDiff) && (odoDiffy >= 7)) {
				Sound.beep();
				blocks[i] = data[0] + (float)odo.getXYT()[0];
				blocks[i+1] = (float)odo.getXYT()[1];
				i += 2;
			}
		}
		while(leftMotor.isMoving() && rightMotor.isMoving()) {
			
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
	 * Drives to each object saved in the 2-d map, in order to identify colour
	 * @param targetColour - an int corresponding to a blue, red, yellow or white block
	 * */
	public int probe(int targetColour) { //have color detection running in the background
		
		int val = 0;
		//0 is false, 1 is true
		
		float data[] = new float[ultraSonic.sampleSize()];
		
		for(int i = 0; i < 8; i += 2) {
			if(blocks[i] == 0 && blocks[i+1] == 0) { //the only way both values will be 0 is if there are no more blocks recorded
				break; //or return..??
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
	
	public void stop() {
		//force the program to exit - perhaps use EXIT;
	}
	
}
