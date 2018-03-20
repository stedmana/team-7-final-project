package navigation;
import odometer.Odometer;
import odometer.OdometerExceptions;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import main.Params;

public class Navigate {
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private double radius;
	private double track;
	
	private Odometer odo;

	private SampleProvider leftLightVal;
	private SampleProvider rightLightVal;
	
	/**
	 * Navigate class used for navigation
	 * @param leftMotor
	 * @param rightMotor
	 * @param leftLightVal
	 * @param rightLightVal
	 * @throws OdometerExceptions
	 */
	public Navigate(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			SampleProvider leftLightVal, SampleProvider rightLightVal){
		
		
		this.track = Params.TRACK;
		
		this.radius = Params.WHEEL_RAD;
		
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		
		this.leftLightVal = leftLightVal;
		this.rightLightVal = rightLightVal;
		try {
		    odo = Odometer.getOdometer();
		}catch (Exception e){}
		
	}
	
	/**
	 * Travels to point given in arguments by referencing odometer value
	 * Correction occurs along the way using two front mounted light sensors
	 * Assuming: on an intersection of two grid lines
	 * @param x - in cartesian coordinates
	 * @param y - in cartesian coordinates
	 * @param pointCorrect - does odometry correction about end point if true
	 * @return
	 */
	public void travelTo(double x, double y, boolean pointCorrect) {
		

		//get current position from odometer
		double[] pos = odo.getXYT();
		
		//
		x = x*Params.TILE_LENGTH;
		y = y*Params.TILE_LENGTH;
		
		
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
	public void squareUp()
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
	 * Turns the robot the minimum distance to face the desired theta heading
	 * @param theta - in degrees
	 */
	public void turnTo(double theta) {

		double dTheta = theta - odo.getXYT()[2];
		   
		   //Insures rotation magnitude will be less than 180
		   //by normalizing angle about +-180
		   if(dTheta > 180) {
		       dTheta -= 360;
		   }
		   else if(dTheta < -180) {
		       dTheta += 360;
		   }
		   
		   //Rotates robot
		   double rotationAngle = dTheta * (track/(2*radius));
		   leftMotor.rotate((int) rotationAngle, true);
		   rightMotor.rotate((int) -rotationAngle, false);
	}
	
	public boolean leftMotorSpinning()
	{
	    return leftMotor.isMoving();
	}
	
	public boolean rightMotorSpinning()
    {
        return rightMotor.isMoving();
    }
	
	/**
	 *  Turns the robot 360 degrees. Useful for localization etc.
	 *  @param speed - in degrees/sec
	 */
	public void spin(int speed)
	{
        double rotation = (Params.TRACK * 180/ Params.WHEEL_RAD);
        leftMotor.setSpeed(speed);
        rightMotor.setSpeed(speed);
        leftMotor.rotate((int)rotation, true);
        rightMotor.rotate((int)-rotation, true);
	}
	
	/**
     *  Raw part of travel to, robot will travel forward by the given amount.
     *  @param speed - in degrees/sec
     *  @param distance - in cm
     */
	public void goForward(int speed, double distance)
	{
	    int wheelRotations = (int) ((distance*180)/(Math.PI*Params.WHEEL_RAD));
	    leftMotor.setSpeed(speed);
	    rightMotor.setSpeed(speed);
	    leftMotor.rotateTo(wheelRotations, true);
	    rightMotor.rotateTo(wheelRotations, false);
	}
}
