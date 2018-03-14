package localization;

import odometer.OdometerExceptions;
import fsm.Task;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import main.Params;
import navigation.Navigate;
import odometer.Odometer;

public class Localization implements Task{
    
    final private SampleProvider us;
    final private EV3LargeRegulatedMotor leftMotor, rightMotor;
    
    private Odometer odometer;
    
    /**
     * Creates a Localization Object
     * @param sensor sensor to record distance with
     * @param leftMotor left motor object.
     * @param rightMotor right motor object.
     */
    public Localization(final SampleProvider sensor, 
                          final EV3LargeRegulatedMotor leftMotor,
                          final EV3LargeRegulatedMotor rightMotor)
    {
        this.us = sensor;
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        Odometer tmpOdom = null;
        try {
          tmpOdom = Odometer.getOdometer();
        } catch (OdometerExceptions e) {
          e.printStackTrace();
        }
        this.odometer = tmpOdom;
    }

    @Override
    public boolean start(boolean prevTaskSuccess) {
        boolean success = false;
        float lastReading, currentReading = -Float.MAX_VALUE;
        do
        {
            lastReading  = currentReading;
            try {
              currentReading = doScan(this.us);
            } catch (OdometerExceptions e) {
              e.printStackTrace();
            }
        }while(Math.abs(lastReading-currentReading) > 20);
        Navigate.turnTo((lastReading+currentReading)/2);
        Navigate.squareUp();
        return success;
    }

    private float doScan(SampleProvider us) throws OdometerExceptions{
      
      final float alpha = (float) 0.4;
      float sample[] = new float[us.sampleSize()];
      
      int currentDegree = (int)Odometer.getOdometer().getXYT()[2];
      
      double rotation = (Params.TRACK * 180/ Params.WHEEL_RAD);
      
      leftMotor.setSpeed(100);
      rightMotor.setSpeed(100);
      
      leftMotor.rotate((int)rotation, true);
      rightMotor.rotate((int)-rotation, true);
      
      us.fetchSample(sample, 0);
      float prevSample = 0;
      float currSample = sample[0] > 255 ? 255 : sample[0];
      
      float minDiff = 0, maxDiff = 0;
      int minIndex = 0, maxIndex = 0;
      while(leftMotor.isMoving() || rightMotor.isMoving()){
          int odoDegree = (int)Odometer.getOdometer().getXYT()[2];
          
          //if the degree has been incremented
          if(currentDegree != odoDegree) {
              prevSample = currSample;
              us.fetchSample(sample, 0);
              if(sample[0] < 255)
                currSample = sample[0];
              else
                currSample = 255;
              currSample = prevSample * alpha + (1-alpha)*currSample;
              
              float currDiff = (currSample - prevSample) / (odoDegree - currentDegree);
              if(maxDiff < currDiff) {
                 maxDiff = currDiff;
                 maxIndex = ((odoDegree+currentDegree)+1)/2;
              }
              else if(minDiff > currDiff)
              {
                 minDiff = currDiff;
                 minIndex = ((odoDegree+currentDegree)+1)/2;
              }
              currentDegree = odoDegree;
          }  
        }
      
        float cornerAngle = maxIndex+minIndex;
        cornerAngle /= 2.0;
        
        if(maxIndex < minIndex)
          cornerAngle = cornerAngle-45;
        else
          cornerAngle = cornerAngle-225;
        
        return cornerAngle;
    }
    
    @Override
    public void stop() {
      // This task is non premptable
    }
    
}
