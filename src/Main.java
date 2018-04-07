import lejos.hardware.Button;
import main.Params;
import fsm.*;
import fsm.TaskManager.TaskType;
import tasks.*;
import static fsm.TaskManager.TaskType.*;
import java.util.*;


/**
 * Main method which runs on startup. Starts the task manager and InitTask in order
 * to begin running the FSM handled by TaskManager. Also starts a kill thread used to abort
 * any code being executed by the brick by pressing the center and back buttons.
 *
 */
public class Main {
  
	
  /**
   * Thread used to kill the program at any point in code execution.
   * Press the center and back buttons on the brick to kill code.
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
  /**
   * Main method that sets up the initialization phase run by TaskManager, and starts the kill thread.
   * 
   * @param args
   */
  public static void main(String[] args) {
      Task iTask = new InitTask(Params.SERVER_IP, Params.TEAM_ID, null/*Params.debugCompetitionParams*/, true);
      
      TaskManager t = TaskManager.get();
      getKillThread().start();
      
      // Start init task
      t.setDebugTaskOrder(INIT);
      t.registerTask(TaskManager.TaskType.INIT, iTask, 10000);
      t.start();
      Button.waitForAnyPress();
  }

}
