package tasks;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import localization.Localization;
import navigation.Navigate;


public class InitTask implements Task {

    static WifiConnection conn;
    
    private final String server;
    private final int teamNum;

    @SuppressWarnings("rawtypes")
    private Map debugParams;

    private boolean debug;
    
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
        
        if(debug) {
            debugInit(this.debugParams, showDebugMenu());
        }else {
            Map data;
            try {
                data = getParams();
            } catch (Exception e) {
               success = false;
               return success; // stop if we fail at getting params
            }
            fullInit(data);
        }
            
        return success;
    }
    
    private void fullInit(Map data) {
        int corner = (int)data.get("RedCorner");
        Navigate n = getNavObject();
        Localization locTask = getLocalizationTask(n, corner);
        
        TaskManager.get().registerTask(TaskType.LOCALIZE, locTask, 60*1000);
        TaskManager.get().setDebugTaskOrder(TaskType.INIT, TaskType.LOCALIZE);
    }


    private void debugInit(Map data, List<TaskType> tasks) {
      // TODO Auto-generated method stub
      System.out.println(tasks);
      
    }
    

    private List<TaskType> showDebugMenu() {
        int tasksLength  = TaskType.values().length;
        // Wait for debug input
        int buttonInput;
        int currentChoice = 0;
        List<TaskType> tasks = new ArrayList<>();
        while((buttonInput = Button.waitForAnyPress()) != Button.ID_ESCAPE) {
            drawText(0, currentChoice+1);
            switch(buttonInput) {
              case Button.ID_DOWN:
                if(currentChoice < tasksLength - 1)
                    currentChoice += 1;
                break;
              case Button.ID_UP:
                if(currentChoice > 1)
                    currentChoice -= 1;
                break;
              case Button.ID_ENTER:
                Sound.beep();
                tasks.add(TaskType.values()[currentChoice]);
                break;
            }
        }
        return tasks;
    }
    
    private void drawText(int optionsOffset, int indicatorPosition) {
         final String[] taskMap = {
              "LOCALIZE", 
              "NAV_TO_BRIDGE", 
              "NAV_TO_TUNNEL", 
              "NAV_TO_HOME", 
              "CROSS_BRIDGE", 
              "CROSS_TUNNEL", 
              "SEARCH"
             };
         final TextLCD lcd = LocalEV3.get().getTextLCD();
         lcd.drawString("Options:", 0, 0);
         for( int i = 0; i < taskMap.length; i++) {
           String s = i + ": "+ taskMap[i];
           lcd.drawString(s, 0, i+optionsOffset);
         }
         lcd.drawChar('<', lcd.getTextWidth()-1, optionsOffset+indicatorPosition);
      }
    
    @SuppressWarnings("rawtypes")
    private Map getParams() throws UnknownHostException, IOException, ParseException
    {
      Map data;
      if(!debug)
      {
          // Init the wifi connection if we don't have it set up.
          if(conn == null)
              conn = new WifiConnection(server, teamNum, false);
          data = conn.getData();
      } else {
          data = this.debugParams;
      }
      return data;
    }
    
    @SuppressWarnings("resource")
    public Navigate getNavObject()
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
    public Localization getLocalizationTask(Navigate n, int corner)
    {
        EV3UltrasonicSensor usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S2"));
        SampleProvider sp = usSensor.getMode("RED");
        Localization locTask = new Localization(sp, n , corner);
        return locTask;
    }
    
    
    
  
    @Override
    public void stop() {
      // TODO Auto-generated method stub
      
    }

}
