package localization;

import odometer.OdometerExceptions;
import fsm.Task;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.robotics.SampleProvider;
import main.Params;
import navigation.Navigate;
import odometer.Odometer;

public class Localization implements Task{
    
  
    final private SampleProvider us;
    private Odometer odometer;
    private Navigate navigate;
    private int corner;
    private volatile boolean _stop = false;
    
    private static double D;
    private static double K;
    

    /**
     * Creates a localization object
     * @param sensor sensor to record distance with
     * @param n reference to navigate object
     * @param corner corner which the robot is in
     * @throws OdometerExceptions the exception thrown by odometer issues
     */
    public Localization(final SampleProvider sensor,
                        Navigate n, int corner) throws OdometerExceptions
    {
        this.us = sensor;
        this.corner = corner;
        this.navigate = n;
        Odometer tmpOdom = null;
        tmpOdom = Odometer.getOdometer();
        this.odometer = tmpOdom;
        
    }
    
    @Override
    public boolean start(boolean prevTaskSuccess) {
      	usLocalize();
      	navigate.squareUp();
      	odometer.setTheta(0);
      	Sound.beep();
      	navigate.goForward(100, Params.SENSOR_DIST);
      	navigate.turnTo(90);
      	
      	navigate.squareUp();
      	Sound.beep();
      	navigate.goForward(100, Params.SENSOR_DIST);
      	odometer.setXYT(Params.cornerParams[corner][0], 
      	                Params.cornerParams[corner][1], 
      	                Params.cornerParams[corner][2]);
      	System.out.println(odometer.getXYT());
      	return true;
    }
        
    private double nearestMultiple(double base, double num) {
    	return Math.round(num / base) * base;
    }
    
    private void findD() {

		navigate.spin(150);

		float minDist = Float.MAX_VALUE;

		while (navigate.leftMotorSpinning() && navigate.rightMotorSpinning()) {
			float[] data = new float[us.sampleSize()];

			us.fetchSample(data, 0);

			if (data[0] < minDist) {
				minDist = data[0];
			}

			try {
				Thread.sleep(70);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		D = minDist * 100 + 9;
		K = D / 3.5;
		
		odometer.setX(minDist * 100 + 5);
		odometer.setY(minDist * 100 + 5);
	}
    
    public void usLocalize() {

		// Store angles at which the walls are seen.
		double[] odoValues = new double[4];

		float[] data = new float[us.sampleSize()];

		us.fetchSample(data, 0);

		try {
			Thread.sleep(70);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		findD();

		navigate.spinLeft(150);

		boolean found = false;

		boolean above = data[0] * 100 > D + K;
		boolean middle = false;

		while (!found) {
			us.fetchSample(data, 0);

			float distCM = data[0] * 100;

			
			if ((above || middle) && distCM < D - K) {
				// Distance went below the threshold for a falling edge.
				found = true;
				above = false;
				odoValues[1] = odometer.getXYT()[2];

				navigate.stop();				
			} else if (above && distCM < D + K && distCM > D - K) {
				// Distance within the noise interval.
				above = false;
				middle = true;
				odoValues[0] = odometer.getXYT()[2];
			} else if (distCM > D + K) {
				above = true;
				middle = false;
			}

			if (found && !middle) {
				// Distance far from wall.
				odoValues[0] = odoValues[1];
			}
			//			String s = data[0] * 100 + "cm";
			//			
			//			LocalEV3.get().getTextLCD().clear(6);
			//			LocalEV3.get().getTextLCD().drawString(s, 0, 6);

			try {
				Thread.sleep(70);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		navigate.spin(150);

		found = false;
		middle = false;

		// Repeat first while loop in opposite direction.
		while (!found) {
			us.fetchSample(data, 0);

			float distCM = data[0] * 100;

			if ((above || middle) && distCM < D - K) {
				found = true;
				above = false;
				odoValues[3] = odometer.getXYT()[2];

				navigate.stop();

				LocalEV3.get().getAudio().setVolume	(25);
				LocalEV3.get().getAudio().playTone(300, 500);

			} else if (above && distCM < D + K && distCM > D - K) {
				above = false;
				middle = true;
				odoValues[2] = odometer.getXYT()[2];
			} else if (distCM > D + K) {
				above = true;
				middle = false;
			}

			if (found && !middle) {
				odoValues[2] = odoValues[3];
			}
			//			String s = data[0] * 100 + "cm";
			//			
			//			LocalEV3.get().getTextLCD().clear(6);
			//			LocalEV3.get().getTextLCD().drawString(s, 0, 6);

			try {
				Thread.sleep(70);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Calculate the angles
		double firstEdgeTheta = (odoValues[0] + odoValues[1]) / 2;
		double secondEdgeTheta = (odoValues[2] + odoValues[3]) / 2;

		// Find the middle.
		double middleTheta;
		middleTheta = ((firstEdgeTheta + secondEdgeTheta) / 2) % 360;
		if (firstEdgeTheta > secondEdgeTheta) {
			middleTheta += 180;
		}

		// Turn to 0.
		navigate.turnTo(middleTheta - 45 - 90);
		odometer.setXYT(Params.cornerParams[corner][0], Params.cornerParams[corner][1], Params.cornerParams[corner][2]);		
		
	}
    
    
    @Override
    public void stop() {
        _stop = true;
    }
    
}
