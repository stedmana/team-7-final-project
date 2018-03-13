package tasks;

import fsm.Task;

public class SampleInitTask implements Task{

    @Override
    public boolean start(boolean prevTaskSuccess) {
        boolean success = true;
        System.out.println("InitTask");
        return success;
    }
  
    @Override
    public void stop() {
        // Don't care we are not interruptible.
    }

}
