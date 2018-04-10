package tasks;

import fsm.Task;
import lejos.robotics.SampleProvider;
import main.Params;
import navigation.Navigate;
import odometer.Odometer;
import odometer.OdometerExceptions;

/**
 * Task is invoked after navigating to the start of the tunnel. Includes theta correction, 
 * driving straight across the tunnel speedbumps, and a final correction on the other side. 
 *
 */

public class CrossTunnelTask implements Task {
  
    final static double DISTANCE = 0.057;

    private static final double BAND = 0.01;
    
    Navigate nav = null;
    Odometer odo = null;
    SampleProvider us = null;
    
    public CrossTunnelTask(Navigate nav, SampleProvider usSensor){
        this.us = usSensor;
        this.nav = nav;
        try {
          odo = Odometer.getOdometer();
        } catch (OdometerExceptions e) {
        }
    }
    
    @Override
    public boolean start(boolean prevTaskSuccess) {
      
        double dt = 1000; // ms
        
        Navigate.PIController controller = nav.PITraveller(Params.SPEED, 
                                                           3*Params.TILE_LENGTH, 
                                                           Params.TILE_LENGTH/2);
        
        long time = System.currentTimeMillis();
        while(!controller.isStopped()) {
            time = System.currentTimeMillis();
            float samples[]  = new float[us.sampleSize()];
            us.fetchSample(samples, 0);
            float currentVal = samples[0];
            
            float error = 0;
            
            if(currentVal < DISTANCE - BAND)
               error = -1;
            else if(currentVal > DISTANCE + BAND)
               error = 1;
            
            controller.set_error(error);
            if(System.currentTimeMillis()-time < dt) {
                try {
                    Thread.sleep((long) (dt-(System.currentTimeMillis()-time)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
  
    @Override
    public void stop() {
    }

}
