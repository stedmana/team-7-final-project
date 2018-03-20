package navigation;

import fsm.Task;
import main.Params;

public class NavToTunnel implements Task {
	
	double x;
	double y;
	Navigate nav;
	
	public NavToTunnel(int x, int y, Navigate n) {
		this.x = x + 0.5;
		this.y = y;
		nav = n;
	}
	@Override
	public boolean start(boolean prevTaskSuccess) {
		// TODO Auto-generated method stub
		nav.travelTo(x, y, true);
		
		return true;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
