package fsm;

import java.util.*;
import java.util.concurrent.Semaphore;
import static fsm.TaskManager.TaskType.*;

class TaskInfo{
    public TaskInfo(Task t, long allotedTime) {
        task = t;
        allotedTimeMs = allotedTime;
        finished = false;
    }
    public Task task;
    public long allotedTimeMs;
    public boolean success;
    public boolean finished;
}

public class TaskManager {
      /* All our tasks are represented as an enumeration because they are fixed.*/
      public enum TaskType{ 
        NONE, 
        INIT,
        LOCALIZE, 
        NAV_TO_BRIDGE, 
        NAV_TO_TUNNEL, 
        NAV_TO_HOME, 
        CROSS_BRIDGE, 
        CROSS_TUNNEL, 
        SEARCH
     };
     
     private static final int TEAM_ORANGE = 0;
     private static final int TEAM_GREEN = 1;
     
     static TaskManager tm;
     
     private TaskType currentTaskID = NONE;
     
     HashMap<TaskType, TaskType> taskMap = new HashMap<>();
     HashMap<TaskType, TaskInfo> idMap = new HashMap<>();
     
     Timer t = new Timer();
     Semaphore interruptSem = new Semaphore(0);
     
     private TaskManager() {}
     
     public static TaskManager get() {
         if(tm == null)
           tm = new TaskManager();
         return tm;
     }
     
     public void registerTask(TaskType taskID, Task t, long allotedTime) {
         idMap.put(taskID, new TaskInfo(t, allotedTime));
     }
     
     public void calculateTaskOrder(int teamID) {
         taskMap.put(NONE, INIT);
         taskMap.put(INIT, LOCALIZE);
         if(teamID == TEAM_ORANGE){
             taskMap.put(LOCALIZE, NAV_TO_TUNNEL);
             taskMap.put(NAV_TO_TUNNEL, SEARCH);
             taskMap.put(SEARCH, NAV_TO_BRIDGE);
             taskMap.put(NAV_TO_BRIDGE, NAV_TO_HOME);
         }else {
             taskMap.put(LOCALIZE, NAV_TO_BRIDGE);
             taskMap.put(NAV_TO_BRIDGE, SEARCH);
             taskMap.put(SEARCH, NAV_TO_TUNNEL);
             taskMap.put(NAV_TO_TUNNEL, NAV_TO_HOME);
         }
     }
     
     public synchronized void onTimerCallback(TaskType taskID) {
         TaskInfo ti = idMap.get(taskID);
         if(!ti.finished) {
             ti.task.stop();
             ti.success = false;
         }
     }
     
     public void setDebugTaskOrder(TaskType...taskTypes) {
         taskMap.put(NONE, taskTypes[0]);
         for(int i = 1; i < taskTypes.length; i++)
             taskMap.put(taskTypes[i-1], taskTypes[i]);
     }
     
     public void start() {
         while(true) {
             final boolean prevTaskSuccess;
             if(idMap.containsKey(currentTaskID))
                 prevTaskSuccess = idMap.get(currentTaskID).success;
             else
                 prevTaskSuccess = false;
             
             final TaskType nextTaskID;
             if((nextTaskID = getNextTask(currentTaskID, prevTaskSuccess)) == NONE) 
                 break;
             currentTaskID = nextTaskID;     
             
             if(idMap.containsKey(currentTaskID)) {
                 final TaskInfo currentTask = idMap.get(currentTaskID);  
                 
                 t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                      onTimerCallback(nextTaskID);
                      this.cancel();
                    } 
                 }, currentTask.allotedTimeMs);
                 
                 currentTask.success = currentTask.task.start(prevTaskSuccess);
                 synchronized(this) {
                     currentTask.finished = true;
                 }
             }
         }         
     }

    private TaskType getNextTask(TaskType taskID, boolean prevTaskSuccess) {
        // If there are multiple paths a task can take then put that logic here..
        if(taskMap.containsKey(taskID))
            return taskMap.get(currentTaskID);
        return NONE;
    }
}
