package tasks;

import fsm.Task;
import main.Params;
import navigation.Navigate;
import odometer.Odometer;
import odometer.OdometerExceptions;

/**
 * Calculates and navigates to a rectangle in the tile grid. The navigation will take it to the
 * the center of the tile immediately before the closest entrance to the rectangle.
 * 
 * Used for tunnel and bridge navigation.
 * 
 * @author Yasasa
 */
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
      nav.travelTo(bridgePosition[0], 
                     bridgePosition[1], 
                     bridgePosition[2], 
                     false);
      
      odo.setXYT((bridgePosition[0]) * Params.TILE_LENGTH, 
                 (bridgePosition[1]) * Params.TILE_LENGTH, 
                  bridgePosition[2]);
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
