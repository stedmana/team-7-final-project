package navigation;
import odometer.Odometer;
import odometer.OdometerExceptions;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class Navigate {
	
	private double xPos;
	private double yPos;
	
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	private static final double TILE_SIZE = 30.48;
	
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	private static double radius;
	private static double track;
	private static int corner;
	
	private static Odometer odo;

	private static SampleProvider colorL;
	private static SampleProvider colorR;

	public Navigate(int corner, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			double radius, double track, SampleProvider colorL, SampleProvider colorR) throws OdometerExceptions {
		this.colorL = colorL;
		this.colorR = colorR;
		
		this.track = track;
		
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		
		odo = Odometer.getOdometer();
		
		xPos = odo.getXYT()[0];
		yPos = odo.getXYT()[1];
		
		this.corner = corner;
	}
	
		
	public void run() {
		double x, y;
		//assuming 
	if(corner == 1) { //starting in green zone; bridge first
		odo.setXYT(10.501968, 0.817585, 0);
		 x = 4.5;
		 y = 5;
	} else { //starting in red zone; tunnel first
		odo.setXYT(1.501968, 10.817585, 180);
		 x = 7.5;
		 y = 7;
	} 
	}
	
	
	public static int travelTo(double x, double y) {

		double distance = 0;
		int val = 0;

		double[] pos = odo.getXYT();
		distance = Math.sqrt(Math.pow(x * TILE_SIZE - pos[0], 2) + Math.pow(y * TILE_SIZE - pos[1], 2));

		double angle = Math.atan2(x * TILE_SIZE - pos[0], y * TILE_SIZE - pos[1]) * 180 / Math.PI;
		angle = (angle < 0) ? 360 + angle : angle;

		if ((angle - pos[2] < 0 ? 360 + angle - pos[2] : angle - pos[2]) > 5) {
			turnTo(angle);
		}


		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.rotate(convertDistance(radius, distance), true);
		rightMotor.rotate(convertDistance(radius, distance), false);
		
		if((odo.getXYT()[0] >= (4.5 - 0.5)) && (odo.getXYT()[0]) <= (4.5 + 0.5)) { //bridge - return 1 for transit (bridge)
			val = 1;
		}
		
		if((odo.getXYT()[0] >= (7.5 - 0.5)) && (odo.getXYT()[0]) <= (7.5 + 0.5)) { //tunnel - return 2 for transit (tunnel)
			val = 2;
		}
		return val;
		
	}
	
	public static void turnTo(double theta) {

		double rotation = theta - odo.getXYT()[2];
		rotation = (rotation < 0) ? 360 + rotation : rotation;

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		if (rotation < 180) {
			leftMotor.rotate(convertAngle(radius, track, rotation), true);
			rightMotor.rotate(-convertAngle(radius, track, rotation), false);
		} else {
			leftMotor.rotate(-convertAngle(radius, track, 360 - rotation), true);
			rightMotor.rotate(convertAngle(radius, track, 360 - rotation), false);
		}

	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
}
