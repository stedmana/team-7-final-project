package tasks;
import tasks.Navigate;
import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import java.util.ArrayList;
import ca.mcgill.ecse211.detectColor.*;
import lejos.utility.Timer;


public class Search {
	
	private static double llx;
	private static double lly;
	private static double urx;
	private static double ury;
	
	public boolean outOfTime; 
	
	private static float[] blocks = new float[8]; //can hold 30 position values
	ArrayList<Float> block = new ArrayList<Float>();
	
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	
	private static Navigate nav;
	SampleProvider ultraSonic;
	
	private double xDiff;
	private double yDiff;
	
	private static Odometer odo;
	
	DetectColor col;
	
	int colour;

	
	public Search(SampleProvider ultraSonic, Odometer odo, Navigate nav, double llx, double lly, double urx, double ury,
			EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, DetectColor col) {
		
		
		this.llx = llx;
		this.lly = lly;
		this.urx = urx;
		this.ury = ury;
		
		this.nav = nav;
		
		double xDiff = urx - llx;
		double yDiff = ury - lly;
		
		this.odo = odo;
		this.ultraSonic = ultraSonic;
		
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		
		this.col = col;
		
		colour = 0;
		
		outOfTime = false;
			
	}
	
	public void run() {
	float data[] = new float[ultraSonic.sampleSize()];
	int i = 0;
		
//		int numTurns;
//		
//		if(yDiff == 1) {}
//		else {
//			for(int i = 1; i < yDiff; i++) {
//				if(yDiff % i == 0) {
//					numTurns = i;
//				}
//			}
//		}
		
		nav.travelToBlock(urx, odo.getXYT()[1]);
		while(leftMotor.isMoving() && rightMotor.isMoving()) {
			ultraSonic.fetchSample(data, 0);
			if(data[0] <= 80) {
				blocks[i] = (float)odo.getXYT()[0];
				blocks[i+1] = data[0] + (float)odo.getXYT()[1];
				i++;
				//so a block will take up two spaces in the array - the first space for the x-Position, second 
				//for the y-Position
			}
		}
		
		nav.travelToBlock(odo.getXYT()[0], ury);
		while(leftMotor.isMoving() && rightMotor.isMoving()) {
			ultraSonic.fetchSample(data, 0);
			if(data[0] <= 80) { //maybe lower the max distance before disregarding distance
				blocks[i] = data[0] + (float)odo.getXYT()[0];
				blocks[i+1] = (float)odo.getXYT()[1];
				i++;
			}
		}
		
		nav.travelToBlock(llx, odo.getXYT()[2]);
		while(leftMotor.isMoving() && rightMotor.isMoving()) {
			ultraSonic.fetchSample(data, 0);
			if(data[0] <= 80) { //maybe lower the max distance before disregarding distance
				blocks[i] = (float)odo.getXYT()[0];
				blocks[i+1] = data[0] + (float)odo.getXYT()[1];
				i++;
			}
		}
		
		nav.travelToBlock(odo.getXYT()[0], lly);
		while(leftMotor.isMoving() && rightMotor.isMoving()) {
			ultraSonic.fetchSample(data, 0);
			if(data[0] <= 80) { //maybe lower the max distance before disregarding distance
				blocks[i] = data[0] + (float)odo.getXYT()[0];
				blocks[i+1] = (float)odo.getXYT()[1];
				i += 2;
			}
		}
		
		
		probe();
	}
	
	public void probe(/*double depthMax*/) { //have color detection running in the background
		
		float data[] = new float[ultraSonic.sampleSize()];
		
		for(int i = 0; i < 8; i += 2) {
			if(blocks[i] == 0 && blocks[i+1] == 0) { //the only way both values will be 0 is if there are no more blocks recorded
				break; //or return..??
			}
			nav.travelToBlock((double)blocks[i], (double)blocks[i+1]);
			//insert colour detection here!!!
			while(leftMotor.isMoving() && rightMotor.isMoving()) {
				ultraSonic.fetchSample(data, 0);
				if(data[0] <= 15) {
					colour = col.detectC();
				}
			}
		}
		
//	double depth = 0;
//	
//	
////	double xDist = 0.5 * x; //should be xDiff
////	double yDist = 0.5 * y; //should be yDiff
//		
//	double xPos = odo.getXYT()[0];	
//	double yPos = odo.getXYT()[1];
//	
//	double start = xPos;
//	
//	nav.travelTo(xPos + depthMax, yPos); //assume this works fam
//	
//	
//	
//	while(Math.abs(depth) < depthMax) {
//		//
//	}
//		
//	
//	nav.travelReverse(xPos, yPos);
	
	}
	
}
