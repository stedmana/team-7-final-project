import fsm.*;
import fsm.TaskManager.TaskType;
import tasks.*;
import static fsm.TaskManager.TaskType.*;
public class Main {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    Task iTask = new SampleInitTask();
    Task lTask = new SampleLocalizeTask();
    TaskManager t = TaskManager.get();
    t.registerTask(TaskType.CROSS_TUNNEL, new Task() {
      volatile boolean _stop = false;
      @Override
      public boolean start(boolean prevTaskSuccess) {
        while(!_stop)
        {
          System.out.println("Hello In random task");
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        return !_stop;
      }

      @Override
      public void stop() {
        // TODO Auto-generated method stub
        System.out.println("stopped");
        _stop = true;
      }
      
    }, 10000);
    t.registerTask(TaskManager.TaskType.INIT, iTask, 10000);
    t.registerTask(TaskManager.TaskType.LOCALIZE, lTask, 10000);
    t.registerTask(CROSS_BRIDGE, iTask, 10000);
    t.setDebugTaskOrder(INIT, LOCALIZE, CROSS_BRIDGE, CROSS_TUNNEL);
    t.start();
  }

}
