package tasks;

import fsm.Task;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import main.Params;

public class SquareDriver implements Task {

    EV3LargeRegulatedMotor leftMotor;
    EV3LargeRegulatedMotor rightMotor;
    
    public SquareDriver(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor)
    {
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
    }
    
    @Override
    public boolean start(boolean prevTaskSuccess) {
        double dist = Params.TILE_LENGTH * 0;
        int rotation = getWheelRotationFromDist(dist);
        
        for(int i = 0; i < 4; i++)
        {
            leftMotor.rotate(rotation, true);
            rightMotor.rotate(rotation, false);
            leftMotor.rotate(getWheelRotationFromDist(Math.PI * Params.TRACK * 90 / 360.0), true);
            rightMotor.rotate(-getWheelRotationFromDist(Math.PI * Params.TRACK * 90 / 360.0), false);
        }
        return true;
    }
    
    private int getWheelRotationFromDist(double distInCM)
    {
        int rotationAngle =  (int) ((distInCM*180)/(Math.PI*Params.WHEEL_RAD));
        return rotationAngle;
      
    }
  
    @Override
    public void stop() {
      // TODO Auto-generated method stub
  
    }

}
