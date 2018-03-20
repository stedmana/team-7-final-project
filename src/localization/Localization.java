package localization;

import odometer.OdometerExceptions;
import fsm.Task;
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
        }while(Math.abs(lastReading-currentReading) > 20 && !_stop );
        
        if(!_stop) {
            success = true;
            navigate.turnTo((lastReading+currentReading)/2);
            navigate.squareUp();
            
            double[] position = Params.cornerParams[this.corner];
            odometer.setXYT(position[0], position[1], position[2]);
            navigate.goForward(Params.SPEED, Params.SENSOR_DIST);
        } else {
            success = false;
        }
        
        return success;
    }

    /**
     * Performs a scan of the area to calculate rising and falling edges.
     * @param us the Distance sensor used for the scan.
     * @return the angle of 0 heading.
     */
    private float doScan(SampleProvider us) throws OdometerExceptions{
      
      final float alpha = (float) 0.4;
      float sample[] = new float[us.sampleSize()];
      
      int currentDegree = (int)Odometer.getOdometer().getXYT()[2];
      us.fetchSample(sample, 0);
      float prevSample = 0;
      float currSample = sample[0] > 255 ? 255 : sample[0];
      navigate.spin(50);
      float minDiff = 0, maxDiff = 0;
      int minIndex = 0, maxIndex = 0;
      while(navigate.rightMotorSpinning() || navigate.leftMotorSpinning()){
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
        _stop = true;
    }
    
}
