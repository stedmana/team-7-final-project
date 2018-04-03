package tasks;
import navigation.Navigate;
import odometer.*;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
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
	
	private static Odometer odo;
	
	DetectColor col;
	
	int colour;

	int targetColour;
	
	boolean taskSuccess;
	
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
	
	public Search(SampleProvider ultraSonic, Odometer odo, Navigate nav, double llx, double lly, double urx, double ury,
			EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, DetectColor col, int targetColour) {
		
		
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
			
	}
	
	
	/**
	 *Enables the robot to travel to each corner of the search area,
	 *and then probe for blocks afterwards */
	public boolean start(boolean prevTaskSuccess) {
		
		taskSuccess = false;
		
	float data[] = new float[ultraSonic.sampleSize()];
	int i = 0;
		
		nav.travelTo(urx, lly, 0, true);
		while(leftMotor.isMoving() && rightMotor.isMoving()) {
			ultraSonic.fetchSample(data, 0);
			if(data[0] <= 80) {
				blocks[i] = (float)odo.getXYT()[0];
				blocks[i+1] = data[0] + (float)odo.getXYT()[1];
				i += 2;
				//so a block will take up two spaces in the array - the first space for the x-Position, second 
				//for the y-Position
			}
		}
		
		nav.travelTo(urx, ury, 0, true);
		while(leftMotor.isMoving() && rightMotor.isMoving()) {
			ultraSonic.fetchSample(data, 0);
			if(data[0] <= 80) { //maybe lower the max distance before disregarding distance
				blocks[i] = data[0] + (float)odo.getXYT()[0];
				blocks[i+1] = (float)odo.getXYT()[1];
				i += 2;
			}
		}
		
		nav.travelTo(llx, ury, 0, true);
		while(leftMotor.isMoving() && rightMotor.isMoving()) {
			ultraSonic.fetchSample(data, 0);
			if(data[0] <= 80) { //maybe lower the max distance before disregarding distance
				blocks[i] = (float)odo.getXYT()[0];
				blocks[i+1] = data[0] + (float)odo.getXYT()[1];
				i += 2;
			}
		}
		
		nav.travelTo(llx, lly, 0, true);
		while(leftMotor.isMoving() && rightMotor.isMoving()) {
			ultraSonic.fetchSample(data, 0);
			if(data[0] <= 80) { //maybe lower the max distance before disregarding distance
				blocks[i] = data[0] + (float)odo.getXYT()[0];
				blocks[i+1] = (float)odo.getXYT()[1];
				i += 2;
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
			}
			nav.travelTo((double)blocks[i], (double)blocks[i+1], 0, false);
			//insert colour detection here!!!
			while(leftMotor.isMoving() && rightMotor.isMoving()) {
				ultraSonic.fetchSample(data, 0);
				if(data[0] <= 15) { //do we have an ultrasonic sensor to detect the block?
					colour = col.detectC();
					try {
						Thread.sleep(500); //to allow the colour detection to be accurate
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				colour = col.detectC();
				break;
				}
			} if(colour == targetColour) {
				Sound.beep();
				Sound.beep();
				Sound.beep();
				val = 1;
			}
		}
	return val;
	}
	
	public void stop() {
		//force the program to exit - perhaps use EXIT;
	}
	
}
