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
import odometer.Odometer;
import odometer.OdometerExceptions;


public class InitTask implements Task {
    // Light sensor
    EV3ColorSensor leftColorSensor = 
        new EV3ColorSensor(LocalEV3.get().getPort("S4"));
    EV3ColorSensor rightColorSensor = 
        new EV3ColorSensor(LocalEV3.get().getPort("S3"));
    SampleProvider lSampleProv = leftColorSensor.getRedMode();
    SampleProvider rSampleProv = rightColorSensor.getRedMode();
  
    // Ultrasonic sensor
    EV3UltrasonicSensor usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S2"));
    SampleProvider sp = usSensor.getDistanceMode();
    
    //Colour Sensor
    static EV3ColorSensor cs = new EV3ColorSensor(LocalEV3.get().getPort("S1"));
  
    static WifiConnection conn;
    
    private final int taskOffset = TaskType.INIT.ordinal();
    private final String server;
    private final int teamNum;

    @SuppressWarnings("rawtypes")
    private Map debugParams;
    private Map<TaskType, Task> taskMap = new HashMap<>(TaskType.values().length);
    
    private boolean debug;

    private Map data;
    
    public InitTask(String server, int teamNum, Map<String, Long> debugParams, boolean debug)
    {
        this.debug = debug;
        this.debugParams = debugParams;
        this.server = server;
        this.teamNum = teamNum;
    }
    
    
    @Override
    public boolean start(boolean prevTaskSuccess) 
    {
        boolean success = true;
        if(debugParams == null) {
            try {
                this.data = getParams();
            } catch (Exception e) {
               success = false;
               return success; // stop if we fail at getting params
            }
        } else {
            data = debugParams;
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
      // TODO: This throws a error when green team is not given
      int team = ((long)data.get("GreenTeam") == Params.TEAM_ID) ? 
                     TaskManager.TEAM_GREEN : TaskManager.TEAM_RED;
      return team;
    }
    
    private void createTasks(Map data) {
      
        TaskManager tm = TaskManager.get();
        final int teamColor = getTeamColor(data);
        final String teamCornerKey = teamColor == TaskManager.TEAM_RED ? "RedCorner" : "GreenCorner";
        
        // Create navigate object
        final Navigate nav = getNavObject();
        
        // Create tasks and put into map
        final int corner = (int)((long)data.get(teamCornerKey));

        Localization locTask = getLocalizationTask(nav, corner);
        
        NavToRecTask navToBridge = new NavToRecTask(nav, 
                                                    (int)((long)data.get("BR_LL_x")), 
                                                    (int)((long)data.get("BR_LL_y")), 
                                                    (int)((long)data.get("BR_UR_x")), 
                                                    (int)((long)data.get("BR_UR_y")));
        
        NavToRecTask navToTunnel = new NavToRecTask(nav,
                                                    (int)((long)data.get("TN_LL_x")), 
                                                    (int)((long)data.get("TN_LL_y")), 
                                                    (int)((long)data.get("TN_UR_x")), 
                                                    (int)((long)data.get("TN_UR_y")));
        
        CrossRecTask cross = new CrossRecTask(nav);
        
        String teamPrefix = String.format("S%s_", 
            teamColor == TaskManager.TEAM_RED ? "G" : "R");
        final int sLLx = (int)(long)data.get(teamPrefix+"LL_x");
        final int sLLy = (int)(long)data.get(teamPrefix+"LL_y");
        final int sURx = (int)(long)data.get(teamPrefix+"UR_x");
        final int sURy = (int)(long)data.get(teamPrefix+"UR_y");
        
        int targetCol = (int)(long)(teamColor == TaskManager.TEAM_RED ? data.get("OG") : data.get("OR"));
        Search search = new Search(sp, nav, sURy, sURy, sURy, sURy, cs, targetCol, corner);
        
        tm.registerTask(TaskType.SEARCH, search, 60000);
        
        tm.registerTask(TaskType.LOCALIZE, locTask, 0);
        
        tm.registerTask(TaskType.NAV_TO_BRIDGE, navToBridge, 0);
        
        tm.registerTask(TaskType.NAV_TO_TUNNEL, navToTunnel, 0);
        
        tm.registerTask(TaskType.CROSS_BRIDGE, cross, 0);
        
        tm.registerTask(TaskType.CROSS_TUNNEL, cross, 0);
        
        tm.registerTask(TaskType.NAV_TO_SEARCH, new Task() {
          @Override
          public boolean start(boolean prevTaskSuccess) {
              int dx = sURx-sLLx;
              int dy = sURy-sLLy;
              double pos[] = new double[3];
              if(dx >= dy) {
                if(corner == 0 || corner == 1) {
                    pos[0] = sLLx - 0.5;
                    pos[1] = sLLy - 0.5;
                    pos[2] = 90;
                }
                else {
                    pos[0] = sURx + 0.5;
                    pos[1] = sURy + 0.5;
                    pos[2] = 270;
                }
              } else if(dx < dy) {
                if(corner == 0 || corner == 1) {
                    pos[0] = sURx + 0.5;
                    pos[1] = sLLy - 0.5;
                    pos[2] = 0;
                }
                else {
                    pos[0] = sLLx - 0.5;
                    pos[1] = sURy + 0.5;
                    pos[2] = 180;
                }
              }
              nav.navigateTo(pos[0], pos[1], pos[2]);
              return true;
          }
          @Override
          public void stop() {}    
        }, 0);
        
        tm.registerTask(TaskType.NAV_TO_HOME, new Task() {
            @Override
            public boolean start(boolean prevTaskSuccess) {
              nav.navigateTo(Params.cornerParams[corner][0]/Params.TILE_LENGTH, 
                             Params.cornerParams[corner][1]/Params.TILE_LENGTH, 
                             Params.cornerParams[corner][2]);
              return true;
            }
    
            @Override
            public void stop() {
              // TODO Auto-generated method stub
              
            }
        }, 0);
    }
    
    private void initFullTaskOrder(int teamID) {
        TaskManager.get().calculateTaskOrder(teamID);
    }


    private void debugInit(List<TaskType> tasks) {
        tasks.add(0, TaskType.INIT);
        TaskType taskArray[] = tasks.toArray(new TaskType[] {});
        TaskManager.get().setDebugTaskOrder(taskArray);
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
              "NAV_TO_SEARCH",
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
        return new Navigate(new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B")),
                            new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A")),
                            lSampleProv, rSampleProv);
    }
    
    @SuppressWarnings("resource")
    private Localization getLocalizationTask(Navigate n, int corner)
    {
        Localization locTask = null;
        try {
          locTask = new Localization(sp, n , corner);
        } catch (OdometerExceptions e) {
        }
        return locTask;
    }
    
    
    
  
    @Override
    public void stop() {
      // TODO Auto-generated method stub
      
    }

}
