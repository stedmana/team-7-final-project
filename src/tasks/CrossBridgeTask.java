package tasks;

import fsm.Task;
import main.Params;
import navigation.Navigate;
import odometer.Odometer;
import odometer.OdometerExceptions;

public class CrossBridgeTask implements Task {
  
    Navigate nav = null;
    Odometer odo = null;
    
    public CrossBridgeTask(Navigate nav){
        this.nav = nav;
        try {
          odo = Odometer.getOdometer();
        } catch (OdometerExceptions e) {
        }
    }
    
    @Override
    public boolean start(boolean prevTaskSuccess) {
        double[] pos = odo.getXYT();
        
        nav.squareUp();
        nav.goForward(Params.SPEED/2, 2.5*Params.TILE_LENGTH);
        
        nav.squareUp();
        nav.goForward(Params.SPEED/2, Params.SENSOR_DIST);
        double theta = Math.round(pos[2]/90) * 90;
        double eps = Math.ulp(1.0);
        
        /* if theta == 0 or theta == 180*/
        if(theta%180 == 0) {
            int sgn = theta <= eps ? 1:-1;
            odo.setXYT(pos[0], 
                       pos[1] + sgn*3.5*Params.TILE_LENGTH, 
                       theta);
            double test[] = odo.getXYT();
            boolean val = test[0] == theta;
        /* if theta == 90 or theta == 270 */
        } else {
            int sgn = Math.abs(theta-90) <= eps ? 1:-1;
            odo.setXYT(pos[0] + sgn*3.5*Params.TILE_LENGTH, 
                       pos[1] , 
                       theta);    
        }
        return true;
    }
    
    

  @Override
  public void stop() {
  }

}
