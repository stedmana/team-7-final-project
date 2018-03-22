package tasks;

import fsm.Task;
import main.Params;
import navigation.Navigate;
import odometer.Odometer;
import odometer.OdometerExceptions;

public class NavToRecTask implements Task {
  
  private int URY;
  private int LLY;
  private int LLX;
  private int URX;
  
  private Navigate nav;

  public NavToRecTask(Navigate n, int LLX, int LLY, int URX, int URY) {
      this.LLX = LLX;
      this.LLY = LLY;
      this.URX = URX;
      this.URY = URY;
      
      this.nav = n;
  }

  @Override
  public boolean start(boolean prevTaskSuccess) {
      double currentPos[] = null;
      Odometer odo = null;
      try {
          odo = Odometer.getOdometer();
      } catch (OdometerExceptions e) {
      }
      currentPos = odo.getXYT();
      
      double  bridgePosition[] = calculateEntrance(currentPos);
      nav.navigateTo(bridgePosition[0] * Params.TILE_LENGTH, 
                     bridgePosition[1] * Params.TILE_LENGTH, 
                     bridgePosition[2]);
      
      nav.squareUp(false);
      nav.goForward(Params.SPEED/2, 3.5*Params.TILE_LENGTH);
      nav.squareUp();
      nav.goForward(Params.SPEED/2, Params.SENSOR_DIST);
      
      double[] pos = odo.getXYT();
      if(bridgePosition[2] % 180 == 0) {
        int sgn = bridgePosition[2] == 0 ? 1:-1;
        odo.setXYT(bridgePosition[0] * Params.TILE_LENGTH, 
                   (bridgePosition[1]+ sgn * 3.5)*Params.TILE_LENGTH, 
                   bridgePosition[2]);
      } else {
        int sgn = bridgePosition[2] == 90 ? 1:-1;
        odo.setXYT((bridgePosition[0]+ sgn * 3.5)*Params.TILE_LENGTH, 
                   bridgePosition[1]*Params.TILE_LENGTH, 
                   bridgePosition[2]);
      }
      return true;
      
  }
  
  public double[] calculateEntrance(double[] currentPos) {
      double bridgePosition[] = new double[3];
    
      // Transform our current position to tile coordinates
      double x = (currentPos[0] / Params.TILE_LENGTH);
      double y = (currentPos[1] / Params.TILE_LENGTH);
      
      // Calculate orientation
      if(URY-LLY > URX-LLX) {
          bridgePosition[0] = ((double)(URX + LLX)) / 2;
          // bridge is straight across in y direction
          if(y < LLY) {
              bridgePosition[1] = LLY-0.5;
              bridgePosition[2] = 0;
          } else {
              bridgePosition[1] = URY+0.5;
              bridgePosition[2] = 180;
          }
      } else {
          // bridge is across in the x direction
          bridgePosition[1] = ((double)(LLY + URY)) / 2;
          if(x < LLX) {
              bridgePosition[0] = LLX - 0.5;
              bridgePosition[2] = 90;
          } else {
              bridgePosition[0] = URX + 0.5;
              bridgePosition[2] = 270;
          }
      }
      
      return bridgePosition;
  }
  
  
  @Override
  public void stop() {
    // TODO Auto-generated method stub

  }
}