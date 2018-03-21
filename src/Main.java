import lejos.hardware.Button;
import main.Params;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import main.Params;
import navigation.Navigate;
import odometer.Odometer;
import odometer.OdometerExceptions;
import fsm.*;
import fsm.TaskManager.TaskType;
import tasks.*;
import static fsm.TaskManager.TaskType.*;
import java.util.*;
public class Main {
  
  public static void main(String[] args) throws OdometerExceptions {
	  
    //motor initialization
    final EV3LargeRegulatedMotor leftMotor =
    	      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
    final EV3LargeRegulatedMotor rightMotor =
    	      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
    
    //left light sensor initialization
    final Port rightLightPort = LocalEV3.get().getPort("S3");
    SensorModes rightLight = new EV3ColorSensor(rightLightPort);
    final SampleProvider rightVal = rightLight.getMode("Red");
    
    //right light sensor initialization
    final Port leftLightPort = LocalEV3.get().getPort("S4");
    SensorModes leftLight = new EV3ColorSensor(leftLightPort);
    final SampleProvider leftVal = leftLight.getMode("Red");
    
    //odometer initialization in new thread
	Odometer odo = Odometer.getOdometer(leftMotor, rightMotor, Params.TRACK, Params.WHEEL_RAD);
    new Thread(odo).start();
    
    //create navigation object
    Navigate nav = new Navigate(leftMotor,rightMotor,leftVal,rightVal);
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
    killThread.start();
    
    System.out.println("waiting for press");
    Button.waitForAnyPress();
    
    //travel to commands
    nav.travelTo(0,6,false);
    
    System.out.println("X: "+odo.getXYT()[0] + " Y: "+odo.getXYT()[1]+" Theta: "+odo.getXYT()[2]);
    
    
  }

}
