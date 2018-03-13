import lejos.hardware.Button;
import fsm.*;
import fsm.TaskManager.TaskType;
import tasks.*;
import static fsm.TaskManager.TaskType.*;
public class Main {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    Task iTask = new InitTask("192.168.2.11", 1);
    Task lTask = new SampleLocalizeTask();
    TaskManager t = TaskManager.get();
    
    Thread killThread = new Thread(new Runnable() {

      @Override
      public void run() {
          Button.waitForAnyPress();
          System.exit(1);
      }
      
    });
    killThread.start();

    t.registerTask(TaskManager.TaskType.INIT, iTask, 10000);
    t.registerTask(TaskManager.TaskType.LOCALIZE, lTask, 10000);
    t.registerTask(CROSS_BRIDGE, iTask, 10000);
    t.setDebugTaskOrder(INIT, LOCALIZE, CROSS_BRIDGE, CROSS_TUNNEL);
    t.start();
    Button.waitForAnyPress();
  }

}
