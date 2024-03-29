package tasks;

import fsm.Task;
import main.Params;
import navigation.Navigate;
import odometer.Odometer;
import odometer.OdometerExceptions;

/**
 * This task crosses 2x1 rectangular area on the grid and performs 
 * odometry correction at the end. Assumption is made that if the previous task
 * succeeded then we have arrived at the start of the rectangle positioned in the center
 * of the tile immideatly before.
 * 
 * This task will not early exit.
 */
public class CrossRecTask implements Task {
  
    Navigate nav = null;
    Odometer odo = null;
    
    /**
     * Creates CrossRecTask Object
     * @param nav Navigate object.
     */
    public CrossRecTask(Navigate nav){
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
        /* if theta == 90 or theta == 270 */
        } else {
            int sgn = Math.abs(theta-90) <= eps ? 1:-1;
            odo.setXYT(pos[0] + sgn*3.5*Params.TILE_LENGTH, 
                       pos[1], 
                       theta);    
        }
        
        return true;
    }
    
    

  @Override
  public void stop() {
  }

}
