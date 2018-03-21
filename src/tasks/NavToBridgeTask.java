package tasks;

import fsm.Task;
import main.Params;
import navigation.Navigate;
import odometer.Odometer;
import odometer.OdometerExceptions;

public class NavToBridgeTask implements Task {
  
  private int bridgeURY;
  private int bridgeLLY;
  private int bridgeLLX;
  private int bridgeURX;
  
  private Navigate nav;

  public NavToBridgeTask(Navigate n, int bridgeLLX, int bridgeLLY, int bridgeURX, int bridgeURY) {
      this.bridgeLLX = bridgeLLX;
      this.bridgeLLY = bridgeLLY;
      this.bridgeURX = bridgeURX;
      this.bridgeURY = bridgeURY;
      
      this.nav = n;
  }

  @Override
  public boolean start(boolean prevTaskSuccess) {
      double currentPos[] = null;
      try {
          currentPos = Odometer.getOdometer().getXYT();
      } catch (OdometerExceptions e) {
      }
      
      int bridgePosition[] = calculateBridgeEntrance(currentPos);
      nav.travelTo(bridgePosition[0], bridgePosition[1], false);
      return true;
  }
  
  public int[] calculateBridgeEntrance(double[] currentPos) {
      int bridgePosition[] = new int[2];
    
      // Transform our current position to tile coordinates
      double x = (currentPos[0] / Params.TILE_LENGTH);
      double y = (currentPos[1] / Params.TILE_LENGTH);
      
      // Calculate orientation
      if(bridgeURY-bridgeLLY > bridgeURX-bridgeLLX) {
          // bridge is straight across in y direction
          if(y < bridgeLLY) {
              bridgePosition[0] = bridgeLLX;
              bridgePosition[1] = bridgeLLY;
          } else {
              bridgePosition[0] = bridgeURX;
              bridgePosition[1] = bridgeURY;
          }
      } else {
          // bridge is across in the x direction
        if(x < bridgeLLX) {
            bridgePosition[0] = bridgeLLX;
            bridgePosition[1] = bridgeURY;
        } else {
            bridgePosition[0] = bridgeURX;
            bridgePosition[1] = bridgeLLY;
        }
      }
      
      return bridgePosition;
  }
  
  
  @Override
  public void stop() {
    // TODO Auto-generated method stub

  }
}
