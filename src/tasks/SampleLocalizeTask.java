package tasks;

import fsm.Task;

public class SampleLocalizeTask implements Task {

  @Override
  public boolean start(boolean prevTaskSuccess) {
      // TODO Auto-generated method stub
      System.out.println("Localize Task");
      return false;
  }

  @Override
  public void stop() {
    // TODO Auto-generated method stub

  }

}
