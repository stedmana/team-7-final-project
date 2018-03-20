package ca.mcgill.ecse211.detectColor;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.ev3.LocalEV3;

public class DetectColor {

	static EV3ColorSensor cs = new EV3ColorSensor(LocalEV3.get().getPort("S4"));
	private static double[] meanBlue = new double[3];
	private static double[] meanRed = new double[3];
	private static double[] meanWhite = new double[3];
	private static double[] meanYellow = new double[3];
	private static TextLCD lcd; 


	public DetectColor() {


		this.lcd = LocalEV3.get().getTextLCD();

		meanBlue[0] = 0.007720;
		meanBlue[1] = 0.020343;
		meanBlue[2] = 0.029656;

		meanRed[0] = 0.062464;
		meanRed[1] = 0.009383;
		meanRed[2] = 0.007002;

		meanYellow[0] = 0.100279;
		meanYellow[1] = 0.071433;
		meanYellow[2] = 0.010504;

		meanWhite[0] = 0.097338;
		meanWhite[1] = 0.102520;
		meanWhite[2] = 0.028570;


	}

	public int detectC() {

		//blue = 1, red = 2, yellow = 3, white = 4, 0 = nothing
		
		int c = 0;

		SampleProvider cSensor = cs.getRGBMode();
		float sample[] = new float[cSensor.sampleSize()];


		//fetch samples
		cSensor.fetchSample(sample, 0);


		//blue
		double bZero = sample[0] - meanBlue[0];
		double bOne = sample[1] - meanBlue[1];
		double bTwo = sample[2] - meanBlue[2];
		double bValZero = Math.pow(bZero, 2);
		double bValOne = Math.pow(bOne, 2);
		double bValTwo = Math.pow(bTwo, 2);
		double bSampleDist = Math.sqrt((bValZero + bValOne + bValTwo));

		double bMeanZero = Math.pow(meanBlue[0], 2);
		double bMeanOne = Math.pow(meanBlue[1], 2);
		double bMeanTwo = Math.pow(meanBlue[2], 2);
		double bMeanDist = Math.sqrt((bMeanZero + bMeanOne + bMeanTwo));

		double bStandZero = Math.pow(0.000628, 2);
		double bStandOne = Math.pow(0.001256, 2);
		double bStandTwo = Math.pow(0.000869, 2);
		double bStandDist = Math.sqrt((bStandZero + bStandOne + bStandTwo));

		//red

		double rZero = sample[0] - meanRed[0];
		double rOne = sample[1] - meanRed[1];
		double rTwo = sample[2] - meanRed[2];
		double rValZero = Math.pow(rZero, 2);
		double rValOne = Math.pow(rOne, 2);
		double rValTwo = Math.pow(rTwo, 2);
		double rSampleDist = Math.sqrt((rValZero + rValOne + rValTwo));

		double rMeanZero = Math.pow(meanRed[0], 2);
		double rMeanOne = Math.pow(meanRed[1], 2);
		double rMeanTwo = Math.pow(meanRed[2], 2);
		double rMeanDist = Math.sqrt((rMeanZero + rMeanOne + rMeanTwo));

		double rStandZero = Math.pow(0.001853, 2);
		double rStandOne = Math.pow(0.000771, 2);
		double rStandTwo = Math.pow(0.000370, 2);
		double rStandDist = Math.sqrt((rStandZero + rStandOne + rStandTwo));

		//yellow
		double yZero = sample[0] - meanYellow[0];
		double yOne = sample[1] - meanYellow[1];
		double yTwo = sample[2] - meanYellow[2];
		double yValZero = Math.pow(yZero, 2);
		double yValOne = Math.pow(yOne, 2);
		double yValTwo = Math.pow(yTwo, 2);
		double ySampleDist = Math.sqrt((yValZero + yValOne + yValTwo));

		double yMeanZero = Math.pow(meanYellow[0], 2);
		double yMeanOne = Math.pow(meanYellow[1], 2);
		double yMeanTwo = Math.pow(meanYellow[2], 2);
		double yMeanDist = Math.sqrt((yMeanZero + yMeanOne + yMeanTwo));

		double yStandZero = Math.pow(0.010795, 2);
		double yStandOne = Math.pow(0.002187, 2);
		double yStandTwo = Math.pow(0.002979, 2);
		double yStandDist = Math.sqrt((yStandZero + yStandOne + yStandTwo));

		//white
		double wZero = sample[0] - meanWhite[0];
		double wOne = sample[1] - meanWhite[1];
		double wTwo = sample[2] - meanWhite[2];
		double wValZero = Math.pow(wZero, 2);
		double wValOne = Math.pow(wOne, 2);
		double wValTwo = Math.pow(wTwo, 2);
		double wSampleDist = Math.sqrt((wValZero + wValOne + wValTwo));

		double wMeanZero = Math.pow(meanWhite[0], 2);
		double wMeanOne = Math.pow(meanWhite[1], 2);
		double wMeanTwo = Math.pow(meanWhite[2], 2);
		double wMeanDist = Math.sqrt((wMeanZero + wMeanOne + wMeanTwo));

		double wStandZero = Math.pow(0.005929, 2);
		double wStandOne = Math.pow(0.005933, 2);
		double wStandTwo = Math.pow(0.080952, 2);
		double wStandDist = Math.sqrt((wStandZero + wStandOne + wStandTwo));

		lcd.drawString("" + sample[0], 0, 2);
		lcd.drawString("" + sample[1], 0, 3);
		lcd.drawString("" + sample[2], 0, 4);

		if((sample[0] != 0 || !(sample[0] <= 10*Math.pow(10, -4))) && (sample[1] != 0 || !(sample[1] <= 10*Math.pow(10, -4))) 
				&& (sample[2] != 0 || !(sample[0] <= 10*Math.pow(10, -4)))) {
			
			//must be a colored block...
			
			if(!(bSampleDist > bMeanDist + 2*bStandDist) && !(bSampleDist < bMeanDist - 2*bStandDist)) {
				//its probably blue
				lcd.clear();

				lcd.drawString("< Object Detected >", 0, 5);
				lcd.drawString("< Blue            >", 0, 6);

				c = 1;
			
			} else if(!(rSampleDist > rMeanDist + 2*rStandDist) && !(rSampleDist < rMeanDist - 2*rStandDist)) {
				//its probably red

				lcd.clear();

				lcd.drawString("< Object Detected >", 0, 5);
				lcd.drawString("< Red             >", 0, 6);

				c = 2;
				
			} else if(!(ySampleDist > yMeanDist + 2*yStandDist) && !(ySampleDist < yMeanDist - 2*yStandDist)) {
				//its probably yellow

				lcd.clear();

				lcd.drawString("< Object Detected >", 0, 5);
				lcd.drawString("< Yellow          >", 0, 6);

				c = 3;
				
			} else if(!(wSampleDist > wMeanDist + 2*wStandDist) && !(wSampleDist < wMeanDist - 2*wStandDist)) {
				//its probably white

				lcd.clear();

				lcd.drawString("< Object Detected >", 0, 5);
				lcd.drawString("< White           >", 0, 6);

				c = 4;
				
			}
			
		} else {
			//no object detected

			lcd.clear();

			lcd.drawString("< No Object Detected >", 0, 5);

		}
		
		return c;

	}

}
