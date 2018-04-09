package tasks;
import navigation.Navigate;
import odometer.*;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;
import main.Params;
import ca.mcgill.ecse211.detectColor.*;
import fsm.Task;

public class Search implements Task {
	
	private static double llx;
	private static double lly;
	private static double urx;
	private static double ury;
	
	private static int cornerNum;
	
	public boolean outOfTime; 
	
	private static float[] blocks = new float[8]; //can hold 4 position values	
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	
	private static Navigate nav;
	SampleProvider ultraSonic;
	
	private double xDiff;
	private double yDiff;
	
	private static Odometer odo;
	
	int colour;
	
	//dumb implementation variables
	int scanDistance;
	double usDepth;
	int direction;
	EV3ColorSensor colSensor;
	private boolean _stop;

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
	
	public Search(SampleProvider ultraSonic, Navigate nav, double llx, double lly, double urx, double ury,
			EV3ColorSensor col, int targetColour, int cornerNum) {
		
		
		this.llx = llx;
		this.lly = lly;
		this.urx = urx;
		this.ury = ury;
		
		this.cornerNum = cornerNum;
		
		this.nav = nav;
		
		double xDiff = urx - llx;
		double yDiff = ury - lly;
		
		try {
			this.odo = Odometer.getOdometer();
		} catch (OdometerExceptions e) {
			
		}
		this.ultraSonic = ultraSonic;
		
		this.colSensor = col;
		
		
		colour = 0;
		
		outOfTime = false;
		
		//sorting out target colour
		//implements mapping red: 1->5 ; blue: 2->2 ; yellow: 3->4 ; white: 4->6
		this.targetColour = targetColour==1 ? 5: targetColour==2 ? 2 : targetColour==3 ? 4 : 6;
		
		//defining variables for dumb search
		scanDistance = (int) (((urx-llx) > (ury-lly)) ? (urx-llx) : (ury - lly));
		
		usDepth = ((urx-llx) > (ury-lly)) ? (ury-lly) : (urx - llx);
		
		direction = (int) (((urx-llx) > (ury-lly)) ? 0 : 1);
		
		this._stop = false;
				
			
	}
	
	
	/**
	 *Enables the robot to travel to each corner of the search area,
	 *and then probe for blocks afterwards */
	public boolean start(boolean prevTaskSuccess) {
		
		//sample array
		float[] usSample = new float[this.ultraSonic.sampleSize()];
		
		
		//for every block on that side
		for(int i = 0; i < scanDistance && !_stop; i++) {
			
			//move forward one tile
			nav.travelForward(Params.TILE_LENGTH, direction);
			
			//look with ultrasonic sensor
			this.ultraSonic.fetchSample(usSample,0);
			
			//if there is a block there
			if(usSample[0] < usDepth*Params.TILE_LENGTH) {
				if(probe(this.targetColour)) {//beep three times
					Sound.beep();
					Sound.beep();
					Sound.beep();
					return true;
				}
					
			}
		}
		
		//beep SIX TIMES cause you didn't find it
		Sound.twoBeeps();
		Sound.twoBeeps();
		Sound.twoBeeps();
		
		
		return false;
	}
	
	
	/**
	 * Drives to each object saved in the 2-d map, in order to identify colour
	 * @param targetColour - an int corresponding to a blue, red, yellow or white block
	 * */
	public boolean probe(int targetColour) { //have color detection running in the background
		
		//0-7 : NONE,BLACK,BLUE,GREEN,YELLOW,RED,WHITE,BROWN
		int output;
		boolean success = false;
		
		//turn left
		nav.turnTo(odo.getXYT()[2] - 90);
		
		//sample collector
		float[] colSamples = new float[1];
		
		//turn on floodlight
		colSensor.setFloodlight(true);
		
		//set to ColorID mode
		SensorMode colScanner = colSensor.getColorIDMode();
		
		int i = 0;
		
		for(i = 0; i < 2*usDepth; i++) {
			
			nav.goForward(100, Params.TILE_LENGTH/2);
			
			output = colSensor.getColorID();
			
			//if white red yellow or blue
			if(output == 2 || output == 4 || output == 5 || output == 6) {
				//if the block is the target colour
				if(output == targetColour) {
					success = true;
				}
				break;
			}
			
		}
		
		//turn around
		nav.turnTo(odo.getXYT()[2] + 180);
		
		//go back
		nav.goForward(100, i*Params.TILE_LENGTH/2);
		
		//turn to face
		nav.turnTo(odo.getXYT()[2] - 90);
		
		return success;
	}
	
	public void stop() {
		_stop = true;
	}
	
}
