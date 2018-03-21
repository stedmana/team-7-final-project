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
     * Creates a Localization Object 
     * @param sensor sensor to record distance with
     * @param n 
     * @param leftMotor left motor object.
     * @param rightMotor right motor object.
     */
    public Localization(final SampleProvider sensor,
                        Navigate n, int corner)
    {
        this.us = sensor;
        this.corner = corner;
        this.navigate = n;
        Odometer tmpOdom = null;
        try {
          tmpOdom = Odometer.getOdometer();
        } catch (OdometerExceptions e) {
          e.printStackTrace();
        }
        this.odometer = tmpOdom;
        
    }

//    @Override
//    public boolean start(boolean prevTaskSuccess) {
//        boolean success = false;
//        float lastReading, currentReading = -Float.MAX_VALUE;
//        do
//        {
//            lastReading  = currentReading;
//            try {
//              currentReading = doScan(this.us);
//            } catch (OdometerExceptions e) {
//              e.printStackTrace();
//            }
//        }while(Math.abs(lastReading-currentReading) > 20 && !_stop );
//        
//        if(!_stop) {
//            success = true;
//            navigate.turnTo((lastReading+currentReading)/2);
//            navigate.squareUp();
//            
//            double[] position = Params.cornerParams[this.corner];
//            odometer.setXYT(position[0], position[1], position[2]);
//            navigate.goForward(Params.SPEED, Params.SENSOR_DIST);
//        } else {
//            success = false;
//        }
//        
//        return success;
//    }
//
//    /**
//     * Performs a scan of the area to calculate rising and falling edges.
//     * @param us the Distance sensor used for the scan.
//     * @return the angle of 0 heading.
//     */
//    private float doScan(SampleProvider us) throws OdometerExceptions{
//      
//      final float alpha = (float) 0.4;
//      float sample[] = new float[us.sampleSize()];
//      
//      int currentDegree = (int)Odometer.getOdometer().getXYT()[2];
//      us.fetchSample(sample, 0);
//      float prevSample = 0;
//      float currSample = sample[0] > 255 ? 255 : sample[0];
//      navigate.spin(50);
//      float minDiff = 0, maxDiff = 0;
//      int minIndex = 0, maxIndex = 0;
//      while(navigate.rightMotorSpinning() || navigate.leftMotorSpinning()){
//          int odoDegree = (int)Odometer.getOdometer().getXYT()[2];
//          
//          //if the degree has been incremented
//          if(currentDegree != odoDegree) {
//              prevSample = currSample;
//              us.fetchSample(sample, 0);
//              if(sample[0] < 255)
//                currSample = sample[0];
//              else
//                currSample = 255;
//              currSample = prevSample * alpha + (1-alpha)*currSample;
//              
//              float currDiff = (currSample - prevSample) / (odoDegree - currentDegree);
//              if(maxDiff < currDiff) {
//                 maxDiff = currDiff;
//                 maxIndex = ((odoDegree+currentDegree)+1)/2;
//              }
//              else if(minDiff > currDiff)
//              {
//                 minDiff = currDiff;
//                 minIndex = ((odoDegree+currentDegree)+1)/2;
//              }
//              currentDegree = odoDegree;
//          }  
//        }
//      
//        float cornerAngle = maxIndex+minIndex;
//        cornerAngle /= 2.0;
//        
//        if(maxIndex < minIndex)
//          cornerAngle = cornerAngle-45;
//        else
//          cornerAngle = cornerAngle-225;
//        
//        return cornerAngle;
//    } 
//    
    
    private double nearestMultiple(double base, double num) {
    	return Math.round(num / base) * base;
    }
    
    public boolean start(boolean prevTaskSuccess) {
    	//boolean success = false;
    	
    	usLocalize();
    	
    	navigate.squareUp();
    	odometer.setTheta(nearestMultiple(90, odometer.getXYT()[2]));
    	if (corner == 0 || corner == 2) {
    		odometer.setY(nearestMultiple(Params.TILE_LENGTH, odometer.getXYT()[1]));
    	} else {
    		odometer.setX(nearestMultiple(Params.TILE_LENGTH, odometer.getXYT()[0]));
    	}
    	
    	return true;
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

		D = minDist * 100 + 8;
		K = D / 4;
		
		odometer.setX(minDist * 100 + 9.5);
		odometer.setY(minDist * 100 + 9.5);
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

				Sound.beep();
				
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

		// Turn to the middle which is at 45°.
		navigate.turnTo(middleTheta - 45);
		odometer.setXYT(Params.cornerParams[corner][0], Params.cornerParams[corner][1], Params.cornerParams[corner][2]);		
		// Turn to 0.
	}
    
    
    @Override
    public void stop() {
        _stop = true;
    }
    
}
