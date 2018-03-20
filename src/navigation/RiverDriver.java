package navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.robotics.SampleProvider;
import main.Params;
import odometer.Odometer;
import odometer.OdometerExceptions;
import main.Params;
import navigation.Navigate;

public class RiverDriver {
	/**
	 * Forward speed of the left and right motors (deg/s)
	 */
	private final int FORWARD_SPEED = 250;

	/**
	 * Amount the motor rotates to enable/disable the rear ball bearing wheel (deg)
	 */
	private final int ROTATION_AMOUNT = 90;
	
	/**
	 * Speed of rotation of the medium motor used for rear wheel (deg/s)
	 */
	private final int ROTATE_SPEED_BEARING = 30;
	
	/**
	 * Acceleration of medium motor
	 */
	private final int ACCELERATION = 10;
	
	/**
	 * motor on the bottom of the robot, used for more precise navigation
	 */
	private EV3MediumRegulatedMotor bearingMotor;
	
	/**
	 * distance recorded by the ultrasonic sensor
	 */
	private SampleProvider dist;
	
	/**
	 * Robots odometer
	 */
	private Odometer odo;
	
	/**
	 * Tunnel lower left point x
	 */
	private int tnLLx;
	
	/**
	 * Tunnel lower left y
	 */
	private int tnLLy;
	
	/**
	 * Tunnel upper right x
	 */
	private int tnURx;
	
	/**
	 * Tunnel upper right y
	 */
	private int tnURy;
	
	/**
	 * Bridge upper right x
	 */
	private int brURx;
	
	/**
	 * Bridge upper right y
	 */
	private int brURy;
	
	/**
	 * Bridge lower left x
	 */
	private int brLLx;
	
	/**
	 * Bridge lower left y
	 */
	private int brLLy;
	
	private Navigate nav;
	
	/**
	 * Constructor for Tunnel Driver class 
	 * used for navigating the tunnel
	 * @param bearingMotor for deploying the back wheel
	 * @param distance ultrasonic sensor data 
	 * @param naver navigate input
	 */
	public RiverDriver(EV3MediumRegulatedMotor bearingMotor, SampleProvider distance, Navigate naver) throws OdometerExceptions {
		
		this.bearingMotor = bearingMotor;
		
		this.dist = distance;
		
		this.odo = Odometer.getOdometer();
		
		this.nav = naver;
		
		//TODO set tunnel coordinates
		
	}
	/**
	 * Rotates the rear motor by the desired angle
	 * @param control positive rotates positive, negative, negative
	 */
	private void rotateRearMotor(int control) {
		this.bearingMotor.setSpeed(ROTATE_SPEED_BEARING);
		this.bearingMotor.setAcceleration(ACCELERATION);
		if (control < 0) {
			this.bearingMotor.rotate(-ROTATION_AMOUNT,false);
		} else {
			this.bearingMotor.rotate(ROTATION_AMOUNT,false);
		}
	}
	/**
	 * The method used the drive through the tunnel
	 */
	private void runTunnel() {
		final int control = 1;
		double centerX = this.tnLLx + 0.5;
		//Disables the rear ball bearing wheel
		this.rotateRearMotor(control);
		
		nav.travelTo(centerX,(double)this.tnLLy,false); //travels from tunnel lower left, to the center of the tunnel
		nav.travelTo(centerX, this.tnURy, false); //travels through the tunnel
		nav.squareUp(); //drives to first black line after it crosses the bridge
		
		
		//enables the rear ball bearing wheel
		this.rotateRearMotor(-control);
	}
	
	private void runBridge() {
		final int control = 1;
		double centerX = this.brURx + 0.5;
		//Disables the rear ball bearing wheel
		this.rotateRearMotor(control);
		
		nav.travelTo(centerX,(double)this.brURy,false); //travels from tunnel lower left, to the center of the tunnel
		nav.travelTo(centerX, this.brLLy, false); //travels through the tunnel
		nav.squareUp(); //drives to first black line after it crosses the bridge
		
		
		//enables the rear ball bearing wheel
		this.rotateRearMotor(-control);
	}
	
	
	
}
