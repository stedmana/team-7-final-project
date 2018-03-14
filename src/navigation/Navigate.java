package navigation;
import odometer.Odometer;
import odometer.OdometerExceptions;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class Navigate {
	
	
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	private static final double TILE_SIZE = 30.48;
	
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	private static double radius;
	private static double track;
	private static int corner;
	
	private static Odometer odo;

	private static SampleProvider leftLightVal;
	private static SampleProvider rightLightVal;
	/**
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @param radius
	 * @param track
	 * @param colorL
	 * @param colorR
	 * @throws OdometerExceptions
	 */
	public Navigate(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			double radius, double track, SampleProvider leftLightVal, SampleProvider rightLightVal) throws OdometerExceptions {
		
		
		this.track = track;
		
		this.radius = radius;
		
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		
		this.leftLightVal = leftLightVal;
		this.rightLightVal = rightLightVal;
		
		odo = Odometer.getOdometer();
		
	}
	
	/**
	 * Travels to point given in arguments by referencing odometer value
	 * Correction occurs along the way using two front mounted light sensors
	 * Assuming: on an intersection of two grid lines
	 * @param x
	 * @param y
	 * @param pointCorrect - does odometry correction about end point if true
	 * @return
	 */
	public static void travelTo(double x, double y, boolean pointCorrect) {
		
		//get current position from odometer
		double[] pos = odo.getXYT();
		
		//
		x = x*Navigate.TILE_SIZE;
		y = y*Navigate.TILE_SIZE;
		
		
		//get sample arrays for front facing light sensors
		float[] sampleLeft = new float[leftLightVal.sampleSize()];
        float [] sampleRight = new float[rightLightVal.sampleSize()];
        
        //booleans to be used by 
        boolean leftDetect = false;
        boolean rightDetect = false;
		
		//rotate to begin y rotation
		if(y - pos[1] > 0)
			turnTo(0);
		else
			turnTo(180);
		
		//travel y distance
		while(Math.abs(y - pos[1]) > 1){ //while distance difference is greater than 1cm
			
			rightLightVal.fetchSample(sampleRight, 0);
            leftLightVal.fetchSample(sampleLeft, 0);
            
            //TODO: make black line detection more rigorous using differential filtering
            
            //dynamic theta correction takes place with left and right wheel line detection
            if(sampleLeft[0] < 0.4)
            {
              leftMotor.stop(true);
              Sound.beep();
              leftDetect = true;
              
            }
            if(sampleRight[0] < 0.4)
            {
	          rightMotor.stop(true);
	          Sound.beep();
	          rightDetect = true;
            }
            //resets once line is hit
            if(leftDetect && rightDetect) {
            	leftDetect = rightDetect = false;
            	rightMotor.forward();
            	leftMotor.forward();
            }
			
		}
		
		//y coordinate has been reached
		leftMotor.stop();
		rightMotor.stop();
		
		//turn towards x coordinate
		if(x - pos[0] > 0)
			turnTo(90);
		else
			turnTo(270);
		
		//execute similar loop traveling to x coordinate
		while(Math.abs(x - pos[0]) > 1){ //while distance difference is greater than 1cm
			
			rightLightVal.fetchSample(sampleRight, 0);
            leftLightVal.fetchSample(sampleLeft, 0);
            
            //TODO: make black line detection more rigorous using differential filtering
            
            //dynamic theta correction takes place with left and right wheel line detection
            if(sampleLeft[0] < 0.4)
            {
              leftMotor.stop(true);
              Sound.beep();
              leftDetect = true;
              
            }
            if(sampleRight[0] < 0.4)
            {
	          rightMotor.stop(true);
	          Sound.beep();
	          rightDetect = true;
            }
            //resets once line is hit
            if(leftDetect && rightDetect) {
            	leftDetect = rightDetect = false;
            	rightMotor.forward();
            	leftMotor.forward();
            }
			
		}
		
		//the x coordinate has been reached
		rightMotor.stop();
		leftMotor.stop();
		
		
		//TODO: add logic to correct odometer about finishing point

		
		
	}
	
	/**
	 * Allows robot to correct theta by driving forward to nearest black line
	 * and using it to square the theta to that line
	 */
	public static void squareUp()
	{
		//get samples
		float[] sampleLeft = new float[leftLightVal.sampleSize()];
        float [] sampleRight = new float[rightLightVal.sampleSize()];
        
        
        //set speed
        leftMotor.setSpeed(100);
        rightMotor.setSpeed(100);
        
        boolean leftDetect = true;
        boolean rightDetect = true;
        
        //drive forward
        leftMotor.forward();
        rightMotor.forward();
        
        //drive to next line
        do {    
            rightLightVal.fetchSample(sampleRight, 0);
            leftLightVal.fetchSample(sampleLeft, 0);
            
            //TODO: make black line detection more rigorous using differential filtering
            
            if(sampleLeft[0] < 0.4)
            {
              leftMotor.stop(true);
              Sound.beep();
              leftDetect = false;
              
            }
            if(sampleRight[0] < 0.4)
            {
	          rightMotor.stop(true);
	          rightDetect = false;
            }
        }while(leftDetect || rightDetect);
	}
	
	/**
	 * 
	 * @param theta
	 */
	public static void turnTo(double theta) {

		double dTheta = theta - odo.getXYT()[2];
		   
		   //Insures rotation magnitude will be less than 180
		   //by normalizing angle about +-180
		   if(dTheta > 180) {
		     dTheta -= 360;
		   }
		   else if(dTheta < -180)
		   {
		     dTheta += 360;
		   }
		   
		   //Rotates robot
		   double rotationAngle = dTheta * (track/(2*radius));
		   leftMotor.rotate((int) rotationAngle, true);
		   rightMotor.rotate((int) -rotationAngle, false);
	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
}
