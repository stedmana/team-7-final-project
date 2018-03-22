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
    Map<String, Integer> debugParams = new HashMap<String,Integer>();
    debugParams.put("RedCorner", 0);
    Task iTask = new InitTask("192.168.2.19", Params.TEAM_ID, debugParams, true);
    
    TaskManager t = TaskManager.get();
    getKillThread().start();
    
    // Start init task
    t.setDebugTaskOrder(INIT);
    t.registerTask(TaskManager.TaskType.INIT, iTask, 10000);
    t.start();
    Button.waitForAnyPress();
  }

}
