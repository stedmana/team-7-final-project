package tasks;

import fsm.Task;

public class CrossTunnelTask implements Task {

  @Override
    public boolean start(boolean prevTaskSuccess) {
        return false;
    }
  
    @Override
    public void stop() {
        
    }

}
