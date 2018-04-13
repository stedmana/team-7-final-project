import lejos.hardware.Button;
import main.Params;
import fsm.*;
import fsm.TaskManager.TaskType;
import tasks.*;
import static fsm.TaskManager.TaskType.*;
import java.util.*;

/**
 * Main class used to create a thread to kill our program, then start the task manager.
 */
public class Main {
  
  /**
   * Creates a thread that can be used to kill the robot software with a button press combination.
   * 
   * @return A corresponding Thread object
   */
  public static Thread getKillThread()
  {
    Thread killThread = new Thread(new Runnable() {
      @Override
      public void run() {
          while(true)
          {
            Button.waitForAnyPress();
            if(Button.readButtons() == (Button.ID_ENTER | Button.ID_ESCAPE))
              System.exit(1);
          }
      }
    });
    return killThread;
  }
  
  public static void main(String[] args) {
      Task iTask = new InitTask(Params.SERVER_IP, Params.TEAM_ID, null, false);
      
      TaskManager t = TaskManager.get();
      getKillThread().start();
      
      // Start init task
      t.setDebugTaskOrder(INIT);
      t.registerTask(TaskManager.TaskType.INIT, iTask, 10000);
      t.start();
      Button.waitForAnyPress();
  }

}
