package fsm;

import java.util.*;
import java.util.concurrent.Semaphore;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import static fsm.TaskManager.TaskType.*;

class TaskInfo{
    public TaskInfo(Task t, long allotedTime) {
        task = t;
        allotedTimeMs = allotedTime;
        finished = false;
        started = false;
    }
    public Task task;
    public long allotedTimeMs;
    public boolean success;
    public boolean finished;
    public boolean started;
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
        NAV_TO_SEARCH,
        SEARCH
     };
     
     public static final int TEAM_RED = 0;
     public static final int TEAM_GREEN = 1;
     
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
     
     /**
      * registerTask Register a Task object to perform one of the TaskTypes
      * @param taskID TaskType corresponding to the Task
      * @param t Task object specifying the actions to perform
      * @param allotedTime Time allowed for the task, only used for premptimble tasks
      */
     public void registerTask(TaskType taskID, Task t, long allotedTime) {
         if((allotedTime *= 1000) == 0) {
             allotedTime = Integer.MAX_VALUE;
         }
         idMap.put(taskID, new TaskInfo(t, allotedTime));
     }
     
     /**
      * calculateTaskOrder Calculate the mapping of tasks based on our team color
      * @param teamID TEAM_GREEN or TEAM_ORANGE corresponding to the team.
      */
     public void calculateTaskOrder(int teamID) {
         taskMap.put(NONE, INIT);
         taskMap.put(INIT, LOCALIZE);
         if(teamID == TEAM_GREEN){
             taskMap.put(LOCALIZE, NAV_TO_TUNNEL);
             taskMap.put(NAV_TO_TUNNEL, CROSS_TUNNEL);
             taskMap.put(CROSS_TUNNEL, NAV_TO_BRIDGE);
             taskMap.put(NAV_TO_BRIDGE, CROSS_BRIDGE);
             taskMap.put(CROSS_BRIDGE, NAV_TO_HOME);
         }else if(teamID == TEAM_RED) {
             taskMap.put(LOCALIZE, NAV_TO_BRIDGE);
             taskMap.put(NAV_TO_BRIDGE, CROSS_BRIDGE);
             taskMap.put(CROSS_BRIDGE, NAV_TO_TUNNEL);
             taskMap.put(NAV_TO_TUNNEL, CROSS_TUNNEL);
             taskMap.put(CROSS_TUNNEL, NAV_TO_HOME);
         }
     }
     
     /**
      * onTimeCallback prempts the running task.
      * @param taskID task to check for out of time.
      */
     public synchronized void onTimerCallback(TaskType taskID) {
         TaskInfo ti = idMap.get(taskID);
         if(!ti.finished && ti.started) {
             ti.task.stop();
             ti.success = false;
         }
     }
     
     /**
      * Allows placing a custom task map other than the full competition one calculated using team ID
      * @param taskTypes List of tasktypes, according to how to set the tasks.
      */
     public void setDebugTaskOrder(TaskType...taskTypes) {
         taskMap.put(NONE, taskTypes[0]);
         for(int i = 1; i < taskTypes.length; i++)
             taskMap.put(taskTypes[i-1], taskTypes[i]);
     }
     
     /**
      * Starts the task_runner, this method will not return until all the tasks are complete.
      */
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
                 
                 LCD.clear();
                 System.out.println(currentTaskID);
                 currentTask.success = currentTask.task.start(prevTaskSuccess);
                 currentTask.started = true;
                 synchronized(this) {
                     currentTask.finished = true;
                 }
                 Sound.twoBeeps();
             }
         }         
     }

    /**
     * getNextTask: called when the state machine finishes a task is proceeding to another task.
     * @param taskID TaskType of the current task to be switched out of
     * @param prevTaskSuccess Indicates the success of the current task
     * @return new TaskType which will be the next task that is run.
     */
    private TaskType getNextTask(TaskType taskID, boolean prevTaskSuccess) {
        // If there are multiple paths a task can take then put that logic here..
        if(taskMap.containsKey(taskID))
            return taskMap.get(currentTaskID);
        return NONE;
    }
}
