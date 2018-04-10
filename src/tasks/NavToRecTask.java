package tasks;

import fsm.Task;
import main.Params;
import navigation.Navigate;
import odometer.Odometer;
import odometer.OdometerExceptions;

/**
 * Navigates to any rectangle in the playing field defined by two points.
 * This includes the search area, the bridge, and the tunnel. The class
 * will navigate to the relative bottom of the rectangle. 
 *
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
  
  /**
   * Starts the navigation to the specified rectangle
   * 
   * @param prevTaskSuccess
   * @return true if successful
   */
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
  
  /**
   * Used to find the entrance to a tunnel or bridge.
   * 
   * @param currentPos - array given by odometer
   * @return position the robot should be to cross the obstacle
   */
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
