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
        
        
        if(pos[2] % 180 == 0) {
            int sgn = pos[2] == 0 ? 1:-1;
            odo.setXYT(pos[0], 
                       pos[1] + sgn*3.5*Params.TILE_LENGTH, 
                       pos[2]);
        } else {
            int sgn = pos[2] == 90 ? 1:-1;
            odo.setXYT(pos[0] + sgn*3.5* Params.TILE_LENGTH, 
                       pos[1], 
                       pos[2]);
        }
        return true;
    }

  @Override
  public void stop() {
  }

}
