package navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.robotics.SampleProvider;
import main.Params;
import odometer.Odometer;
import odometer.OdometerExceptions;

public class TunnelDriver {
	/**
	 * Forward speed of the left and right motors
	 */
	private static final int FORWARD_SPEED = 250;
	
	/**
	 * Rotation speed for the left and right motors when turning
	 */
	private static final int ROTATE_SPEED = 150;
	
	/**
	 * Size of tiles made by black lines
	 */
	private static final double TILE_SIZE = 30.48;
	
	/**
	 * Amount the motor rotates to enable/disable the rear ball bearing wheel
	 */
	private static final int ROTATION_AMOUNT = 90;
	
	/**
	 * Speed of rotation of the medium motor used for rear wheel
	 */
	private static final int ROTATE_SPEED_BEARING = 30;
	
	/**
	 * Acceleration of medium motor
	 */
	private static final int ACCELERATION = 50;
	
	/**
	 * Left motor on the Robot
	 */
	private static EV3LargeRegulatedMotor leftMotor;
	
	/**
	 * Right motor on the robot
	 */
	private static EV3LargeRegulatedMotor rightMotor;
	
	/**
	 * motor on the bottom of the robot, used for more precise navigation
	 */
	private static EV3MediumRegulatedMotor bearingMotor;
	
	/**
	 * radius of the wheels on the robot
	 */
	private static double radius;
	
	/**
	 * track width of the robot (distance between wheels)
	 */
	private static double track;
	
	/**
	 * primary odometer
	 */
	private static Odometer odo;

	/**
	 * left light sensor
	 */
	private static SampleProvider leftLightVal;
	
	/**
	 * right light sensor
	 */
	private static SampleProvider rightLightVal;

	/**
	 * ultrasonic sensor
	 */
	private static SampleProvider dist;

	
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
	public TunnelDriver(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3MediumRegulatedMotor bearingMotor,
			SampleProvider leftLightVal, SampleProvider rightLightVal, SampleProvider distance) throws OdometerExceptions {
		
		
		TunnelDriver.track = Params.TRACK;
		
		TunnelDriver.radius = Params.WHEEL_RAD;
		
		TunnelDriver.leftMotor = leftMotor;
		TunnelDriver.rightMotor = rightMotor;
		TunnelDriver.bearingMotor = bearingMotor;
		
		
		
		TunnelDriver.leftLightVal = leftLightVal;
		TunnelDriver.rightLightVal = rightLightVal;
		
		TunnelDriver.dist = distance;
		
		odo = Odometer.getOdometer();
		
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
