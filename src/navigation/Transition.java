package navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import ca.mcgill.ecse211.odometer.OdometryCorrection;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.odometer.Odometer;

public class Transition extends OdometryCorrection{
	
	public static EV3LargeRegulatedMotor leftMotor;
	public static EV3LargeRegulatedMotor rightMotor;
	public static double radius;
	public static double track;
	public static int state; 
	private static Odometer odo;
	
	public static double BRIDGE_VAL = 58.9; //in cm
	
	public Transition(Odometer odo, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			double radius, double track, int state) throws OdometerExceptions{
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.radius = radius;
		this.track = track;
		this.state = state;
		this.odo = odo;
	}
	
	public void run() {
		if(state == 1) { //bridge crossing
			
			
			
		} else if(state == 2){ //tunnel crossing
			
		}
	}
	
}
