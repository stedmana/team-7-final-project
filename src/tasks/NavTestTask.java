package tasks;

import fsm.Task;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import main.Params;
import navigation.Navigate;
import odometer.Odometer;
import odometer.OdometerExceptions;

public class NavTestTask implements Task{

	@Override
	public boolean start(boolean prevTaskSuccess) {
		
		EV3ColorSensor leftColorSensor = 
		        new EV3ColorSensor(LocalEV3.get().getPort("S4"));
	    EV3ColorSensor rightColorSensor = 
	        new EV3ColorSensor(LocalEV3.get().getPort("S3"));
	    SampleProvider lSampleProv = leftColorSensor.getRedMode();
	    SampleProvider rSampleProv = rightColorSensor.getRedMode();
	    
	    //create navigate object
	    Navigate nav = new Navigate(new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B")),
                new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A")),lSampleProv, rSampleProv);
	    
	    Odometer odo = null;
	    
	    try {
	    odo = Odometer.getOdometer();
	    }
	    catch(OdometerExceptions e) {
	    	System.out.println(e);
	    }
	    
	    
	    Button.waitForAnyPress();
	    nav.travelTo(2, 2, 0, true);
	    System.out.println("X: "+ odo.getXYT()[0]/Params.TILE_LENGTH + " Y: "+odo.getXYT()[1]/Params.TILE_LENGTH +" theta: "+odo.getXYT()[2]);
	    
	    
	    
		
		return false;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
