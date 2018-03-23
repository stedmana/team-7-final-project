package navigation;
import odometer.Odometer;

import odometer.OdometerExceptions;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import main.Params;

public class Navigate {
  
	private static final int DIR_X = 0;
    private static final int DIR_Y = 1;
    
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
		    odo = Odometer.getOdometer(leftMotor, rightMotor, track, radius);
		}catch (Exception e){}
		new Thread(odo).start();
	}
	
	/**
	 * Navigate to a given coordinate.
	 * 
	 * @param x - Navigation coordinate X in cm
	 * @param y - Navigation coordinate Y in cm
	 * @param theta - Final angle in cm
	 */
	public void navigateTo(double x, double y, double theta) {
	    // Set our speeds
	    leftMotor.setSpeed(Params.SPEED);
	    rightMotor.setSpeed(Params.SPEED);
	    
	    double[] pos = odo.getXYT();
	    travelForward(y-pos[1], DIR_Y);
	    travelForward(x-pos[0], DIR_X);
	    turnTo(theta);
	}
	
	/**
	 * Travels forward a distance in x or y direction correcting the robot with the lines.
	 * 
	 * @param distance distance to travel
	 * @param direction DIR_X or DIR_Y
	 */
	private void travelForward(double distance, int direction)
	{
  	  double angle = 0;
  	  // Turn to the appropriate direction.
  	  int sideOffset = distance < 0 ? 180 : 0;
  	  if (direction == DIR_X){
  	      angle = 90 + sideOffset;
  	  } else if (direction == DIR_Y) {
  	      angle = sideOffset;
  	  } else {
  	      return;
  	  }
  	  turnTo(angle);
  	  
  	  double[] pos = odo.getXYT();
  	  
  	  float[] sampleRight = new float[rightLightVal.sampleSize()];
  	  float[] sampleLeft = new float[leftLightVal.sampleSize()];
  	  
  	  double goal = pos[direction] + distance;
  	  
  	  int closestLineToGoal = (int) (distance < 0 ? Math.ceil((goal - 5)/ Params.TILE_LENGTH) :
  	                                         Math.floor((goal + 5)/ Params.TILE_LENGTH));
  	  
  	  int closestLineToUs = (int) (distance < 0 ? Math.ceil((pos[direction] - 5)/ Params.TILE_LENGTH) :
                                             Math.floor((pos[direction] + 5)/ Params.TILE_LENGTH));
  	  int numberOfLines = Math.abs(closestLineToGoal - closestLineToUs);
  	  int currentLine = 0;
  	      
  	  leftMotor.forward();
  	  rightMotor.forward();
  	  while(currentLine < numberOfLines) { //while distance difference is greater than 2cm
          
          rightLightVal.fetchSample(sampleRight, 0);
          leftLightVal.fetchSample(sampleLeft, 0);
          
          //dynamic theta correction takes place with left and right wheel line detection
          //left motor
          if(sampleLeft[0] < 0.4 && leftMotor.isMoving()) {
            leftMotor.stop(true);
          }
          //right motor
          if(sampleRight[0] < 0.4 && rightMotor.isMoving()) {
            rightMotor.stop(true);
          }
          //resets once line is hit
          if(!leftMotor.isMoving() && !rightMotor.isMoving()) {
              currentLine++;
              
              //dynamic theta correction
              odo.setTheta(angle);
              
              // correct the position
              pos = odo.getXYT();
              if(distance >= 0)
                  pos[direction] = ((int)(pos[direction] + 5) / Params.TILE_LENGTH)*Params.TILE_LENGTH;
              else
                  pos[direction] = ((int)(pos[direction] - 5) / Params.TILE_LENGTH)*Params.TILE_LENGTH;
              odo.setXYT(pos[0], pos[1], pos[2]);
              
              //start robot moving again
              goForward(100, Params.SENSOR_DIST);
              rightMotor.forward();
              leftMotor.forward();
          }
      }
  	  
  	  leftMotor.stop(true);
  	  rightMotor.stop(false);
  	  double remainingDist = Math.abs((goal - odo.getXYT()[direction])); // dead reckon the remaining amount
  	  if(remainingDist > 0) remainingDist = (Params.TILE_LENGTH-Params.SENSOR_DIST)/2; 
  	  goForward(Params.SPEED, remainingDist);
	}
	
	/**
	 * Travels to point given in arguments by referencing odometer value
	 * Correction occurs along the way using two front mounted light sensors
	 * Finishes with heading facing corresponding Y direction traversed
	 * (eg. up = 0 degrees, down = 180 degrees)
	 * Assuming: on an intersection of two grid lines
	 * @param x - in cartesian coordinates
	 * @param y - in cartesian coordinates
	 * @param pointCorrect - does odometry correction about end point if true, orients facing 0 degrees
	 * @return
	 */
	public void travelTo(double x, double y, boolean pointCorrect) {
		
		//set speed
		leftMotor.setSpeed(Params.SPEED);
		rightMotor.setSpeed(Params.SPEED);
		
		//set acceleration
		leftMotor.setAcceleration(Params.ACCEL);
		rightMotor.setAcceleration(Params.ACCEL);
		
		//get current position from odometer
		double[] pos = odo.getXYT();
		
		//convert to cm
		x = x*Params.TILE_LENGTH;
		y = y*Params.TILE_LENGTH;
		
		//temporary value used for differential calculation
		double lastValLeft = 0;
		double lastValRight = 0;
		double tempValLeft = 0;
		double tempValRight = 0;
		
		//value containing the slope
		double slopeLeft = 0;
		double slopeRight = 0;
		
		//get sample arrays for front facing light sensors
		float[] sampleLeft = new float[leftLightVal.sampleSize()];
        float [] sampleRight = new float[rightLightVal.sampleSize()];
        
		//rotate to begin y rotation
		if(y - pos[1] > 0)
			turnTo(0);
		else
			turnTo(180);
		
		//travel y distance
		while(Math.abs(y - odo.getXYT()[1]) > 2){ //while distance difference is greater than 2cm
			
			rightLightVal.fetchSample(sampleRight, 0);
            leftLightVal.fetchSample(sampleLeft, 0);
            
            //calculate difference between current and last light sensor value
            slopeLeft = sampleLeft[0] - lastValLeft;
            slopeRight = sampleRight[0] - lastValRight;
            
            //record value for future reference
            lastValLeft = tempValLeft;
            lastValRight = tempValRight;
            tempValLeft = sampleLeft[0];
            tempValRight = sampleRight[0];
            
            //shift temp value to last
            
            //dynamic theta correction takes place with left and right wheel line detection
            //left motor
            if(slopeLeft < Params.DIFF_THRESHOLD && leftMotor.isMoving())
            {
              leftMotor.stop(true);
            }
            //right motor
            if(slopeRight < Params.DIFF_THRESHOLD && rightMotor.isMoving())
            {
	          rightMotor.stop(true);
	          
            }
            //resets once line is hit
            if(!leftMotor.isMoving() && !rightMotor.isMoving()) {
            	
            	//dynamic theta correction
            	if(odo.getXYT()[2] > 90 && odo.getXYT()[2] < 270)
            		odo.setTheta(180);
            	else
            		odo.setTheta(0);
            	
            	//correcting odometer y value
            	odo.setY(((int)(odo.getXYT()[1] + 5) / Params.TILE_LENGTH)*Params.TILE_LENGTH);
            	
            	//start robot moving again
            	rightMotor.forward();
            	leftMotor.forward();
            	
            }
			
		}
		
		//y coordinate has been reached
		leftMotor.stop(true);
		rightMotor.stop(false);
		
		//turn towards x coordinate
		if(x - pos[0] > 0)
			turnTo(90);
		else
			turnTo(270);
		
		//execute similar loop traveling to x coordinate
		while(Math.abs(x - odo.getXYT()[0]) > 2){ //while distance difference is greater than 2cm
			
			rightLightVal.fetchSample(sampleRight, 0);
            leftLightVal.fetchSample(sampleLeft, 0);
            
          //calculate difference between current and last light sensor value
            slopeLeft = sampleLeft[0] - lastValLeft;
            slopeRight = sampleRight[0] - lastValRight;
            
            //record value for future reference
            lastValLeft = sampleLeft[0];
            lastValRight = sampleRight[0];
            
            //TODO: remove print line after testing is complete
            //System.out.println("Raw Left: "+sampleLeft[0]+" Slope Left: "+slopeLeft);
            
          //dynamic theta correction takes place with left and right wheel line detection
            //left motor
            if(slopeLeft < Params.DIFF_THRESHOLD && leftMotor.isMoving())
            {
              leftMotor.stop(true);
            }
            //right motor
            if(slopeRight < Params.DIFF_THRESHOLD && rightMotor.isMoving())
            {
	          rightMotor.stop(true);
	          
            }
            //resets once line is hit
            if(!leftMotor.isMoving() && !rightMotor.isMoving()){
            	
            	//correcting odometer theta
            	if(odo.getXYT()[2] > 0 && odo.getXYT()[2] < 180)
            		odo.setTheta(90);
            	else
            		odo.setTheta(270);
            	
            	//correcting odometer x value
            	odo.setX(((int)(odo.getXYT()[0] + 5) / Params.TILE_LENGTH)*Params.TILE_LENGTH);
            	
            	//restarting motors
            	rightMotor.forward();
            	leftMotor.forward();
            	
            }
			
		}
		
		//the x coordinate has been reached
		rightMotor.stop(true);
		leftMotor.stop(false);
		
		
		//TODO: add logic to correct odometer about finishing point
		
		//call pointCorrect method if requested
		if(pointCorrect)
			pointCorrect(x,y);
			
	}
	
	/**
	 * Starting with the assumption that the robot is near a cartesian coordinate
	 * facing along the x direction
	 * @param x - x double value of coordinate being corrected about
	 * @param y - y double value of coordinate being corrected about
	 */
	public void pointCorrect(double x, double y) {
		
		//correct the y precision
		squareUp(false);
		leftMotor.rotate((int)Math.toDegrees(Params.SENSOR_DIST/Params.WHEEL_RAD),true);
		rightMotor.rotate((int)Math.toDegrees(Params.SENSOR_DIST/Params.WHEEL_RAD),false);
		
		//turn to face 0 degrees
		turnTo(0);
		
		//correct the x precision and theta precision
		squareUp(false);
		leftMotor.rotate((int)Math.toDegrees(Params.SENSOR_DIST/Params.WHEEL_RAD),true);
		rightMotor.rotate((int)Math.toDegrees(Params.SENSOR_DIST/Params.WHEEL_RAD),false);
		odo.setXYT(x, y, 0);
		
	}
	/**
	 * Used to get around boolean argument of squareUp(boolean fwd)
	 * Has no parameters
	 */
	public void squareUp() {
		squareUp(true);
	}
	
	/**
	 * Allows robot to correct theta by driving forward to nearest black line
	 * and using it to square the theta to that line
	 * @param fwd - false = robot drives backward
	 */
	public void squareUp(boolean fwd)
	{
		//get samples
		float[] sampleLeft = new float[leftLightVal.sampleSize()];
        float [] sampleRight = new float[rightLightVal.sampleSize()];
        
        
        //set speed
        	leftMotor.setSpeed(100);
        	rightMotor.setSpeed(100);
        
        boolean leftDetect = false;
        boolean rightDetect = false;
        
        //drive
        if(fwd) {
            leftMotor.forward();
            rightMotor.forward();
        } else {
          leftMotor.backward();
          rightMotor.backward();
        }
        
        
        //drive to next line
        do {    
            rightLightVal.fetchSample(sampleRight, 0);
            leftLightVal.fetchSample(sampleLeft, 0);
            
            //TODO: make black line detection more rigorous using differential filtering
            
            if(sampleLeft[0] < 0.4)
            {
              leftMotor.stop(true);
              leftDetect = true;
              
            }
            if(sampleRight[0] < 0.4)
            {
	          rightMotor.stop(true);
	          rightDetect = true;
            }
        }while(!leftDetect || !rightDetect);
	}
	
	/**
	 * Turns the robot the minimum distance to face the desired theta heading
	 * @param theta - in degrees
	 */
	public void turnTo(double theta) {

	   leftMotor.setAcceleration(2* Params.TURN_SPEED);
	   rightMotor.setAcceleration(2* Params.TURN_SPEED);
	   leftMotor.setSpeed(Params.TURN_SPEED);
	   rightMotor.setSpeed(Params.TURN_SPEED);
	   
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
	   
	   // reset speed params
	   leftMotor.setAcceleration(Params.ACCEL);
       rightMotor.setAcceleration(Params.ACCEL);
       leftMotor.setSpeed(Params.SPEED);
       rightMotor.setSpeed(Params.SPEED);
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
	
	public void spinLeft(int speed) {
		double rotation = (Params.TRACK * 180/ Params.WHEEL_RAD);
        leftMotor.setSpeed(speed);
        rightMotor.setSpeed(speed);
        leftMotor.rotate((int)-rotation, true);
        rightMotor.rotate((int)rotation, true);
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
	    leftMotor.rotate(wheelRotations, true);
	    rightMotor.rotate(wheelRotations, false);
	    leftMotor.setSpeed(Params.SPEED);
        rightMotor.setSpeed(Params.SPEED);
	}
	
	/**
	 * Stop both motors rotation.
	 */
	public void stop() {
		leftMotor.stop(true);
		rightMotor.stop(false);
	}
}
