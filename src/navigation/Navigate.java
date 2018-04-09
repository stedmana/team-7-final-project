package navigation;
import odometer.Odometer;

import odometer.OdometerExceptions;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import main.Params;

/**
 * Navigate class is used to move the robot across the playing field
 * using the cartesian coordinate system defined. It includes methods for 
 * navigation to new coordinates, odometry correction, and motor data.
 * 
 */
public class Navigate {
    
	
	//TODO: is this going to be deleted?
    public class PIController implements Runnable{
        final static int I_MAX = 5;
        final int defaultSpeed;
        double current_error;
        double K_p;
        double K_i;
        
        double integrator;
        
        double lastTime = 0;
        
        boolean _stop = true;
        public PIController(double K_p, double K_i, int defaultSpeed) {
            this.current_error = 0;
            this.integrator = 0;
            this.defaultSpeed = defaultSpeed;
        }
        
        @Override
        public void run() {
            _stop = false;
            this.lastTime = System.currentTimeMillis();
            while(!_stop) {
                if(current_error > 0) {
                    leftMotor.setSpeed(defaultSpeed+50);
                    rightMotor.setSpeed(defaultSpeed);
                } else if (current_error < 0) {
                    leftMotor.setSpeed(defaultSpeed);
                    rightMotor.setSpeed(defaultSpeed+50);
                } else {
                    leftMotor.setSpeed(defaultSpeed);
                    leftMotor.setSpeed(defaultSpeed);
                }
                if(!(leftMotor.isMoving() && rightMotor.isMoving()))
                    _stop = true;
            }
        }
        
        public void stop() {
            _stop = true;
        }
        
        public void set_error(double error) {
            this.current_error = error;
            update_integrator(error, (System.currentTimeMillis() - lastTime)/1000);
            this.lastTime = System.currentTimeMillis();
        }

        private void update_integrator(double error, double dt) {
            if(this.integrator <= I_MAX)
                this.integrator += dt * error;
        }

        public boolean isStopped() {
            return _stop;
        }
    }
  
	private static final int DIR_X = 0;
    private static final int DIR_Y = 1;
    
    public EV3LargeRegulatedMotor leftMotor;
	public EV3LargeRegulatedMotor rightMotor;
	private double radius;
	private double track;
	
	private Odometer odo;

	private SampleProvider leftLightVal;
	private SampleProvider rightLightVal;
	
	/**
	 * Navigate object constructor
	 * 
	 * @param leftMotor	left motor of robot
	 * @param rightMotor	right motor of robot
	 * @param leftLightVal light value from left light sensor
	 * @param rightLightVal light value from right light sensor
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
	 * Method used to travel to a defined cartesian coordinate in the 
	 * playing field, and finish there facing a defined heading. Odometry
	 * correction is built in, and line sensing can be done with either a
	 * differential or hard-coded method. 
	 * 
	 * @param x - cartesian x coordinate of destination
	 * @param y - cartesian y coordinate of destination
	 * @param theta - desired final theta heading
	 * @param diff - if true robot uses differential line sensing, false is hard-coded
	 */
	public void travelTo(double x, double y, double theta, boolean diff) {
		
		//allows choice between differential or hard-coded line sensing
		if(!diff)
			navigateTo(x,y,theta);
		else
			diffTravelTo(x,y,theta);
	}
	
	public void travel(double x, double y, double theta, boolean diff) {
		navigate(x, y, theta);
	}
	
	/**
	 * Navigate to a given coordinate using the hard-coded line sensing method. 
	 * 
	 * @param x - Navigation coordinate X in cartesian
	 * @param y - Navigation coordinate Y in cartesian
	 * @param theta - Final angle in degrees
	 */
	public void navigateTo(double x, double y, double theta) {
	    // Set our speeds
	    leftMotor.setSpeed(Params.SPEED);
	    rightMotor.setSpeed(Params.SPEED);
	    
	    //change to cm
	    x = x*Params.TILE_LENGTH;
	    y = y*Params.TILE_LENGTH;
	    
	    double[] pos = odo.getXYT();
	    travelForward(y-pos[1], DIR_Y);
	    travelForward(x-pos[0], DIR_X);
	    turnTo(theta);
	}
	
	public void navigate(double x, double y, double theta) {
		// Set our speeds
	    leftMotor.setSpeed(Params.SPEED);
	    rightMotor.setSpeed(Params.SPEED);
	    
	    //change to cm
	    x = x*Params.TILE_LENGTH;
	    y = y*Params.TILE_LENGTH;
	    
	    double[] pos = odo.getXYT();
	    travelF(y-pos[1], DIR_Y);
	    travelF(x-pos[0], DIR_X);
	    //turnTo(theta);
	}
	
	/**
	 * Travels forward a distance in x or y direction correcting the robot with the lines.
	 * Used by the navigateTo hard-coded line sensing method.
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
              
              //start robot moving again
              goForward(100, Params.SENSOR_DIST_L, Params.SENSOR_DIST_R);
              
              odo.setXYT(pos[0], pos[1], pos[2]);

          }
      }
  	  

  	  double remainingDist = (goal - odo.getXYT()[direction]); // dead reckon the remaining amount
  	  if((remainingDist > 0 && distance > 0) ||
  	     (remainingDist < 0 && distance < 0)) 
  		  remainingDist = (Params.TILE_LENGTH)/2 - Params.SENSOR_DIST; 
  	  else
  		  remainingDist = 0;
  	  goForward(Params.SPEED, remainingDist);
	}
	
	public void travelF(double distance, int direction) {
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
	              
	              //start robot moving again
	              goForward(100, Params.SENSOR_DIST_L, Params.SENSOR_DIST_R);
	              
	              odo.setXYT(pos[0], pos[1], pos[2]);
//	              rightMotor.forward();
//	              leftMotor.forward();
	          }
	      }
	  	  
//	  	  leftMotor.stop(true);
//	  	  rightMotor.stop(true);
	  	  double remainingDist = (goal - odo.getXYT()[direction]); // dead reckon the remaining amount
	  	  if((remainingDist > 0 && distance > 0) ||
	  	     (remainingDist < 0 && distance < 0)) 
	  		  remainingDist = (Params.TILE_LENGTH)/2 - Params.SENSOR_DIST; 
	  	  else
	  		  remainingDist = 0;
	  	  goForward(Params.SPEED, remainingDist);
	}
	
	/**
	 * Travels to point given in arguments by referencing odometer value
	 * Correction occurs along the way using two front mounted light sensors
	 * Finishes with heading facing corresponding Y direction traversed
	 * (eg. up = 0 degrees, down = 180 degrees)
	 * Assuming: on an intersection of two grid lines
	 * 
	 * @param x - in cartesian coordinates
	 * @param y - in cartesian coordinates
	 * @param theta - does odometry correction about end point if true, orients facing 0 degrees
	 * 
	 */
	public void diffTravelTo(double x, double y, double theta) {
		
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
            
            //record value for future reference (increased window size)
            lastValLeft = tempValLeft;
            lastValRight = tempValRight;
            tempValLeft = sampleLeft[0];
            tempValRight = sampleRight[0];
            
            //TODO: remove print line after testing is complete
            
            System.out.println(sampleLeft[0] + "\t" + sampleRight[0]);
            
            //dynamic theta correction takes place with left and right wheel line detection
            //left motor
            if(slopeLeft < Params.DIFF_THRESHOLD && leftMotor.isMoving())
            {
              leftMotor.stop(true);
              //System.out.println("LEFT STOP");
              //System.out.println("Raw Left: "+sampleLeft[0]+"\t Slope Left: "+slopeLeft+"\t Raw Right: "+sampleRight[0]+"\t Slope Right: "+slopeRight);
            }
            //right motor
            if(slopeRight < Params.DIFF_THRESHOLD && rightMotor.isMoving())
            {
            	
	          rightMotor.stop(true);
	          //System.out.println("RIGHT STOP");
	          //System.out.println("Raw Left: "+sampleLeft[0]+"\t Slope Left: "+slopeLeft+"\t Raw Right: "+sampleRight[0]+"\t Slope Right: "+slopeRight);
	          
            }
            //resets once line is hit
            if(!leftMotor.isMoving() && !rightMotor.isMoving()) {
            	
            	//dynamic theta correction
            	if(odo.getXYT()[2] > 90 && odo.getXYT()[2] < 270) {
            		odo.setTheta(180);
            		odo.setY(Math.floor((odo.getXYT()[1] + 2*Params.SENSOR_DIST) / Params.TILE_LENGTH)*Params.TILE_LENGTH + Params.SENSOR_DIST);
            	}
            	else {
            		odo.setTheta(0);
            		odo.setY(Math.floor((odo.getXYT()[1] + 2*Params.SENSOR_DIST) / Params.TILE_LENGTH)*Params.TILE_LENGTH - Params.SENSOR_DIST);
            	}
            	
            	
            	
            	//start robot moving again
            	rightMotor.forward();
            	leftMotor.forward();
            	
            }
			
		}
		
		//y coordinate has been reached
		leftMotor.stop(true);
		rightMotor.stop(false);
		
		System.out.println("Turning to x coordinates");
		
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
            	if(odo.getXYT()[2] > 0 && odo.getXYT()[2] < 180) {
            		odo.setTheta(90);
            		odo.setX(Math.floor((odo.getXYT()[0] + 2*Params.SENSOR_DIST) / Params.TILE_LENGTH) * Params.TILE_LENGTH - Params.SENSOR_DIST);
            	}
            	else {
            		odo.setTheta(270);
            		odo.setX(Math.floor((odo.getXYT()[0] + 2*Params.SENSOR_DIST) / Params.TILE_LENGTH) * Params.TILE_LENGTH + Params.SENSOR_DIST);
            	}
            	
            	
            	//restarting motors
            	rightMotor.forward();
            	leftMotor.forward();
            	
            }
			
		}
		
		//the x coordinate has been reached
		rightMotor.stop(true);
		leftMotor.stop(false);
		
		//rotate to desired final theta heading
		turnTo(theta);
		
			
	}
	
	/**
	 * Used so a squareUp call with no arguments is defaulted to an
	 * argument of True, which corresponds to a forward square up.
	 */
	public void squareUp() {
		squareUp(true);
	}
	
	/**
	 * Allows robot to correct theta using the nearest black line. The robot drives
	 * forward until a sensor detects a line, and then stops the sensor's corresponding motor.
	 * 
	 * @param fwd - false = robot drives backward, true = robot drives forward
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
	 * Turns the robot the minimum distance to face the desired theta heading.
	 * 
	 * @param theta - target angle in degrees
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
	
	/**
	 * Used by localization to check if robot is spinning
	 * 
	 * @return true if left motors is spinning
	 */
	public boolean leftMotorSpinning()
	{
	    return leftMotor.isMoving();
	}
	
	/**
	 * Used by localization to check if robot is spinning
	 * 
	 * @return true if right motors is spinning
	 */
	public boolean rightMotorSpinning()
    {
        return rightMotor.isMoving();
    }
	
	/**
	 *  Makes the robot rotate 360 degrees clockwise.
	 *  Used in localization procedure.
	 *  
	 *  @param speed - in degrees/second
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
	 *  Makes the robot rotate 360 degrees counter-clockwise.
	 *  Used in localization procedure.
	 *  
	 *  @param speed - in degrees/second
	 */
	public void spinLeft(int speed) {
		double rotation = (Params.TRACK * 180/ Params.WHEEL_RAD);
        leftMotor.setSpeed(speed);
        rightMotor.setSpeed(speed);
        leftMotor.rotate((int)-rotation, true);
        rightMotor.rotate((int)rotation, true);
	}
	
	/**
     *  Robot will travel forward by the given amount.
     *  
     *  @param speed - in degrees/second
     *  @param distance - in centimeters
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
	
	public void goForward(int speed, double distanceLeft, double distanceRight)
    {
        double wheelRotationsCoeff = (180/(Math.PI*Params.WHEEL_RAD));
        leftMotor.setSpeed(speed);
        rightMotor.setSpeed(speed);
        leftMotor.rotate((int) (wheelRotationsCoeff*distanceLeft), true);
        rightMotor.rotate((int) (wheelRotationsCoeff*distanceRight), false);
        leftMotor.setSpeed(Params.SPEED);
        rightMotor.setSpeed(Params.SPEED);
    }
	
	/**
	 * Travel with angle correction.
	 * 
	 * @param speed - Initial speed to go to the forward motors.
	 * @param d - The distance to go.
	 * @param e - The amount of distance to go before starting the controller
	 * @return PIController - a controller object to send the distance parameters to.
	 */
	public PIController PITraveller(float speed, double d, double e) {
	    
	    int wheelRotations = (int) ((e*180)/(Math.PI*Params.WHEEL_RAD));
	    leftMotor.rotate(wheelRotations, true);
	    rightMotor.rotate(wheelRotations, false);
	    
	    wheelRotations = (int) ((d*180)/(Math.PI*Params.WHEEL_RAD));
	    leftMotor.rotate(wheelRotations, true);
	    leftMotor.rotate(wheelRotations, true);
	    PIController controller = new PIController(0.6, 3, (int) speed);
	    Thread controlThread = new Thread(controller);
	    controlThread.start();
	    return controller;
	}
	
	/**
	 * Get left motor object
	 * 
	 * @return left motor object
	 */
	public EV3LargeRegulatedMotor getLeftMotor() {
		return this.leftMotor;
	}
	
	/**
	 * Get right motor object
	 * 
	 * @return right motor object
	 */
	public EV3LargeRegulatedMotor getRightMotor() {
		return this.rightMotor;
	}
	
	/**
	 * Get left sample provider for left light sensor.
	 * 
	 * @return left light sensor sample provider
	 */
	public SampleProvider getSampleLeft() {
		return leftLightVal;
	}
	
	/**
	 * Get right sample provider for left light sensor.
	 * 
	 * @return right light sensor sample provider
	 */
	public SampleProvider getSampleRight() {
		return rightLightVal;
	}
	
	/**
	 * Get odometer object being used by navigation class
	 * 
	 * @return odometer object
	 */
	public Odometer getOdo() {
		return this.odo;
	}
	
	/**
	 * Stop both motors rotation.
	 */
	public void stop() {
		leftMotor.stop(true);
		rightMotor.stop(false);
	}
}
