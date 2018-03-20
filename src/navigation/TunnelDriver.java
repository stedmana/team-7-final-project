package navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.robotics.SampleProvider;
import main.Params;
import odometer.Odometer;
import odometer.OdometerExceptions;
import main.Params;

public class TunnelDriver {
	/**
	 * Forward speed of the left and right motors (deg/s)
	 */
	private static final int FORWARD_SPEED = 250;

	/**
	 * Amount the motor rotates to enable/disable the rear ball bearing wheel (deg)
	 */
	private static final int ROTATION_AMOUNT = 90;
	
	/**
	 * Speed of rotation of the medium motor used for rear wheel deg/s
	 */
	private static final int ROTATE_SPEED_BEARING = 30;
	
	/**
	 * Acceleration of medium motor
	 */
	private static final int ACCELERATION = 10;
	
	/**
	 * motor on the bottom of the robot, used for more precise navigation
	 */
	private static EV3MediumRegulatedMotor bearingMotor;
	
	/**
	 * distance recorded by the ultrasonic sensor
	 */
	private static SampleProvider dist;
	
	/**
	 * Robots odometer
	 */
	private static Odometer odo;

	
	/**
	 * Constructor for Tunnel Driver class 
	 * used for navigating the tunnel
	 * @param leftMotor
	 * @param rightMotor
	 * @param bearingMotor for deploying the back wheel
	 * @param leftLightVal
	 * @param rightLightVal
	 * @param distance ultrasonic sensor data 
	 * @throws OdometerExceptions
	 */
	public TunnelDriver(EV3MediumRegulatedMotor bearingMotor, SampleProvider distance) throws OdometerExceptions {
		
		TunnelDriver.bearingMotor = bearingMotor;
		
		TunnelDriver.dist = distance;
		
		TunnelDriver.odo = Odometer.getOdometer();
		
	}
	/**
	 * Rotates the rear motor by the desired angle
	 * @param control positive rotates positive, negative, negative
	 */
	private static void rotateRearMotor(int control) {
		TunnelDriver.bearingMotor.setSpeed(ROTATE_SPEED_BEARING);
		TunnelDriver.bearingMotor.setAcceleration(ACCELERATION);
		if (control < 0) {
			TunnelDriver.bearingMotor.rotate(-ROTATION_AMOUNT,false);
		} else {
			TunnelDriver.bearingMotor.rotate(ROTATION_AMOUNT,false);
		}
	}
	/**
	 * The method used the drive through the tunnel
	 */
	private static void run() {
		final int control = 1;
		
		//Disables the rear ball bearing wheel
		TunnelDriver.rotateRearMotor(control);
		
		//Code the travel across the bridge
		Navigate.squareUp();
		
		//enables the rear ball bearing wheel
		TunnelDriver.rotateRearMotor(-control);
	}
	
}
