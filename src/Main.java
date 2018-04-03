import lejos.hardware.Button;
import main.Params;
import fsm.*;
import fsm.TaskManager.TaskType;
import tasks.*;
import static fsm.TaskManager.TaskType.*;
import java.util.*;
public class Main {
  
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
      Task iTask = new InitTask(Params.SERVER_IP, Params.TEAM_ID, Params.debugCompetitionParams, false);
      
      TaskManager t = TaskManager.get();
      getKillThread().start();
      
      // Start init task
      t.setDebugTaskOrder(INIT);
      t.registerTask(TaskManager.TaskType.INIT, iTask, 10000);
      t.start();
      Button.waitForAnyPress();
  }

}
