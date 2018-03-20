package tasks;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ParseException;
import ca.mcgill.ecse211.WiFiClient.WifiConnection;
import fsm.Task;
import fsm.TaskManager;
import fsm.TaskManager.TaskType;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.LCD;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import localization.Localization;
import main.Params;
import navigation.Navigate;


public class InitTask implements Task {
    static WifiConnection conn;
    
    private final int taskOffset = TaskType.INIT.ordinal();
    private final String server;
    private final int teamNum;

    @SuppressWarnings("rawtypes")
    private Map debugParams;
    private Map<TaskType, Task> taskMap = new HashMap<>(TaskType.values().length);
    
    private boolean debug;

    private Map data;
    
    public InitTask(String server, int teamNum, Map<String, Integer> params, boolean debug)
    {
        this.debug = debug;
        this.debugParams = params;
        this.server = server;
        this.teamNum = teamNum;
    }
    
    
    @SuppressWarnings("rawtypes")
    @Override
    public boolean start(boolean prevTaskSuccess) 
    {
        boolean success = true;
        try {
            this.data = getParams();
        } catch (Exception e) {
           success = false;
           return success; // stop if we fail at getting params
        }
        createTasks(data);
        if(debug) {
            debugInit(showDebugMenu());
        }else {
            initFullTaskOrder(getTeamColor(data));
        }
            
        return success;
    }
    
    private int getTeamColor(Map data){
      int team = ((long)data.get("RedTeam") == Params.TEAM_ID) ? 
                     TaskManager.TEAM_RED : TaskManager.TEAM_GREEN;
      return team;
    }
    
    private void createTasks(Map data){
        String teamCornerKey = getTeamColor(data) == TaskManager.TEAM_RED ? "RedCorner" : "GreenCorner";
        
        // Create navigate object
        Navigate n = getNavObject();
        
        // Create tasks and put into map
        Localization locTask = getLocalizationTask(n, (int)((long)data.get(teamCornerKey)));
        
        taskMap.put(TaskType.LOCALIZE, locTask);
        taskMap.put(TaskType.NAV_TO_TUNNEL, null);
        taskMap.put(TaskType.NAV_TO_HOME, null);
        taskMap.put(TaskType.NAV_TO_BRIDGE, null);
        taskMap.put(TaskType.SEARCH, null);
        taskMap.put(TaskType.CROSS_BRIDGE, null);
        taskMap.put(TaskType.CROSS_TUNNEL, null);
    }
    
    private void initFullTaskOrder(int teamID) {
        TaskManager.get().calculateTaskOrder(teamID);
    }


    private void debugInit(List<TaskType> tasks) {
        tasks.add(0, TaskType.INIT);
        TaskManager.get().setDebugTaskOrder((TaskType[]) tasks.toArray());
    }
    

    private List<TaskType> showDebugMenu() {
        LCD.clear();
        int tasksLength  = TaskType.values().length;
        // Wait for debug input
        boolean waitForInput = true;
        
        int currentChoice = 0;
        List<TaskType> tasks = new ArrayList<>();
        while(waitForInput){
            drawText(0, currentChoice, tasks);
            switch(Button.waitForAnyPress()) {
              case Button.ID_DOWN:
                if(currentChoice < tasksLength - 1)
                    currentChoice += 1;
                break;
              case Button.ID_UP:
                if(currentChoice > 0)
                    currentChoice -= 1;
                break;
              case Button.ID_ENTER:
                Sound.beep();
                if(!tasks.contains(TaskType.values()[currentChoice+taskOffset+1]))
                    tasks.add(TaskType.values()[currentChoice+taskOffset+1]);
                break;
              case Button.ID_ESCAPE:
                LCD.clear();
                waitForInput = false;
                break;
            }
        }
        return tasks;
    }
    
    private void drawText(int optionsOffset, int indicatorPosition, List<TaskType> currentTasks) {
         final String[] taskMap = {
              "LOCALIZE", 
              "NAV_TO_BRIDGE", 
              "NAV_TO_TUNNEL", 
              "NAV_TO_HOME", 
              "CROSS_BRIDGE", 
              "CROSS_TUNNEL", 
              "SEARCH"
             };
         final TextLCD lcd = LocalEV3.get().getTextLCD(Font.getFont(0, 0, Font.SIZE_SMALL));
         lcd.clear();
         for( int i = 0; i < taskMap.length; i++) {
           String s = i + ": "+ taskMap[i];
           lcd.drawString(s, 0, i+optionsOffset);
         }
         lcd.drawChar('<', lcd.getTextWidth()-1, optionsOffset+indicatorPosition);
         int taskListY = 1 + optionsOffset + taskMap.length;
         int taskListX = 0;
         
         // draw the selected tasks
         for(TaskType t : currentTasks) {
             String currTask = taskMap[t.ordinal()-taskOffset-1];
             if(taskListX + currTask.length() > lcd.getTextWidth()) {
               taskListX = 0;
               taskListY += 1;
             }
             lcd.drawString(currTask + ",", taskListX, taskListY);
             taskListX += (currTask.length() + 1);
         }
      }
    
    @SuppressWarnings("rawtypes")
    private Map getParams() throws UnknownHostException, IOException, ParseException
    {
      Map data;
      // Init the wifi connection if we don't have it set up.
      if(conn == null)
          conn = new WifiConnection(server, teamNum, false);
      data = conn.getData();
      return data;
    }
    
    @SuppressWarnings("resource")
    private Navigate getNavObject()
    {
        EV3ColorSensor leftColorSensor = 
            new EV3ColorSensor(LocalEV3.get().getPort("S4"));
        EV3ColorSensor rightColorSensor = 
            new EV3ColorSensor(LocalEV3.get().getPort("S3"));
        SampleProvider lSampleProv = leftColorSensor.getRedMode();
        SampleProvider rSampleProv = rightColorSensor.getRedMode();
        return new Navigate(new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B")),
                            new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A")),
                            lSampleProv, rSampleProv);
    }
    
    @SuppressWarnings("resource")
    private Localization getLocalizationTask(Navigate n, int corner)
    {
        EV3UltrasonicSensor usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S2"));
        SampleProvider sp = usSensor.getDistanceMode();
        Localization locTask = new Localization(sp, n , corner);
        return locTask;
    }
    
    
    
  
    @Override
    public void stop() {
      // TODO Auto-generated method stub
      
    }

}
