package tasks;
import navigation.Navigate;
import odometer.*;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.robotics.SampleProvider;
import main.Params;
import ca.mcgill.ecse211.detectColor.*;
import fsm.Task;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.TextLCD;
import localization.*;


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
	
	private static double[] blocks = new double[10]; //can hold 5 position values	
	
	private static Navigate nav;
	SampleProvider ultraSonic;
	
	private static Odometer odo;
	
	DetectColor col;
	
	int colour;

	int targetColour;
	
	float data[];
	int dist;
	
	boolean taskSuccess;
	private static TextLCD lcd = LocalEV3.get().getTextLCD(Font.getFont(0, 0, Font.SIZE_SMALL));

	
	Localization loc;
		

	
	 /**
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
	public Search(SampleProvider ultraSonic, Odometer odo, Navigate nav, Localization loc,  DetectColor col, 
				  int targetColor, double llx, double lly, double urx, double ury) {
		
		
		this.llx = llx - 0.5;
		this.lly = lly;
		this.urx = urx;
		this.ury = ury;
		
		this.nav = nav;

		
		this.odo = odo;
		this.ultraSonic = ultraSonic;
		this.loc = loc;
		
		this.col = col;
		
		colour = 0;
		
		outOfTime = false;
		
		this.targetColour = targetColor;
		
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
		int i = 0;
		
		//might be completed by previous task
		//nav.travelTo(llx, lly, 90, false);
		nav.diagNav(llx, lly);
	
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//travel along bottom of search area
		
		Thread nav1 = new Thread() {
			public void run() {
				while(!Thread.currentThread().isInterrupted()) {
					try{
						Thread.sleep(300);
						//nav.travel(urx, lly, 0, false);
						nav.diagNav(urx, lly);
						odo.run();
					} catch(InterruptedException e) {
						
					}
				}
			}
		}; 
				odo.setX(llx*Params.TILE_LENGTH);
				nav1.start();
				while(odo.getXYT()[0] <= urx*Params.TILE_LENGTH) { //use 'drive THIS distance'
			 //nav will run as a thread...
			//while(true) {
				//while(nav.leftMotor.isMoving() && nav.rightMotor.isMoving()) {
					loc.us.fetchSample(data, 0); //should I make this another thread?
					dist = (int)(data[0]*100.0);
					if((dist <= 30)) {
						Sound.beep();
						blocks[i] = odo.getXYT()[0]; //stores x coordinate of block
						blocks[i+1] = dist + odo.getXYT()[1]; // stores approximate y coordinate of block
						i += 2;
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//hopefully will pause the ultrasonic controller while keeping the motors rolling
						//so a block will take up two spaces in the array - the first space for the x-Position, second 
						//for the y-Position
					}
				//}
			//}
			
		}
		nav1.interrupt();
		
		odo.setY(lly*Params.TILE_LENGTH);
		Thread nav2 = new Thread() {
			public void run() {
				while(!Thread.currentThread().isInterrupted()) {
					try{
						Thread.sleep(300);
						//nav.travel(urx, ury, 0, false);
						nav.diagNav(urx, ury);
					} catch(InterruptedException e) {
						
					}
				}
			}
		}; 
		nav2.start();
		while(odo.getXYT()[1] <= ury*Params.TILE_LENGTH) {
			
			//while(true) {
				//while(nav.leftMotor.isMoving() && nav.rightMotor.isMoving()) {
					loc.us.fetchSample(data, 0);
					dist = (int)(data[0]*100.0);
					if((dist <= 30)) {
						Sound.beep();
						blocks[i] = dist + odo.getXYT()[0];
						blocks[i+1] = odo.getXYT()[1];
						i += 2;
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				//}
			//}
		}
		nav2.interrupt();
		
		
//		int val = probe(targetColour);
//		if(val != 0) {
//			taskSuccess = true;
//		} else {
//			taskSuccess = false;
//			Sound.beep();
//			Sound.beep();
//			Sound.beep();
//			Sound.beep();
//			Sound.beep();
//			Sound.beep();
//
//		}
//		return taskSuccess;
		
		
	}
	
	
	/**
	 * Drives to each object saved in the 2d map, in order to identify colour.
	 * 
	 * @param targetColour an int corresponding to a blue, red, yellow or white black
	 * @return int indicating if the target colour was found: 0 = false, 1 = true.
	 */
	@SuppressWarnings("static-access")
	public int probe(int targetColour) { //have color detection running in the background
		
		int val = 0;
		//0 is false, 1 is true
		for(int i = 0; i < blocks.length; i += 2) {
			if(blocks[i] == 0 && blocks[i+1] == 0) {
				return val;
			} else {
				nav.diagNav(blocks[i], blocks[i+1]);
			}

			Thread c1 = new Thread() {
				public void run() {
					try {
						while(!Thread.currentThread().isInterrupted()) {
							colour = col.detectC();
							Thread.sleep(300);
						}
					} catch(InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			};
			c1.start();
			try {
				c1.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(colour == targetColour) {
				Sound.beep();
				Sound.beep();
				Sound.beep();
				val = colour;
				c1.interrupt();
				return val;
			} else if(colour == 0) { //end the thread...
				c1.interrupt();
			}
		}
	return val;
	}


	public float[] getData() {
		return this.data;
	}

	
}
