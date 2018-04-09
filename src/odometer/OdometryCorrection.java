/*
 * OdometryCorrection.java
 */
package odometer;

import lejos.hardware.port.Port;
import java.util.Arrays;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.SampleProvider;

public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;
  private static final double LIGHT_THRESHOLD = 0.45;
  private static final double TILE_LENGTH = 30.48;
  private static final double SENSOR_OFFSET = 4.5;
  private Odometer odometer;

  private static final Port port = LocalEV3.get().getPort("S2");
  private static final EV3ColorSensor lightSensor = new EV3ColorSensor(port);
  private SampleProvider provider;
  private float[] sample;

  /**
   * This is the default class constructor. An existing instance of the odometer is used. This is to
   * ensure thread safety.
   * 
   * @throws OdometerExceptions
   */
  public OdometryCorrection() throws OdometerExceptions {
    provider = lightSensor.getMode("Red");
    sample = new float[provider.sampleSize()];
    this.odometer = Odometer.getOdometer();
  }

  /**
   * Here is where the odometer correction code should be run.
   * 
   */
  // run method (required for Thread)
  public void run() {
    long correctionStart, correctionEnd;

    boolean crossing = false;
    while (true) {
      correctionStart = System.currentTimeMillis();
      provider.fetchSample(sample, 0);

      // TODO Trigger correction (When do I have information to correct?)
      if (sample[0] < LIGHT_THRESHOLD) {

        if (crossing == false) {
          double[] position = odometer.getXYT();
          LocalEV3.get().getAudio().playTone(500, 300, 50);
          int dir = ((int) Math.round(position[2] / 90) + 1) % 4; // Get the orientation of the
                                                                  // robot as a multiple of 90deg
          double offset = (dir == 0 || dir == 3) ? SENSOR_OFFSET : -SENSOR_OFFSET; // Adjust the
                                                                                   // offset based
                                                                                   // on the
                                                                                   // direction we
                                                                                   // are facing.
          // Calculate the new position.
          position[dir % 2] =
              (int) (position[dir % 2] / (TILE_LENGTH - 7.5)) * TILE_LENGTH + offset;
          odometer.setXYT(position[0], position[1], position[2]);
          crossing = true; // We are crossing the line.
        }
      } else {
        crossing = false; // We have crossed the line.
      }
      // this ensure the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here
        }
      }
    }
  }
}
