package tasks;

import java.util.Map;
import ca.mcgill.ecse211.WiFiClient.WifiConnection;
import fsm.Task;


public class InitTask implements Task {

    static WifiConnection conn;
    
    private final String server;
    private final int teamNum;
    
    public InitTask(String server, int teamNum)
    {
        this.server = server;
        this.teamNum = teamNum;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public boolean start(boolean prevTaskSuccess) {
        boolean succ = false;
        
        // Init the wifi connection if we don't have it set up.
        if(conn == null)
            conn = new WifiConnection(server, teamNum, false);
        
        Map data = null;
        try {
            data = conn.getData();
        } catch(Exception e) {
            e.printStackTrace(System.out);
            succ = false;                
        }
        
        // Do nothing with this for now except print stuff
        System.out.println(data.get("TN_LL_x"));
        return succ;
    }
    
    @SuppressWarnings("rawtypes")
    public void createNavToBridgeParams(Map data) {
      
    }
    
    @SuppressWarnings("rawtypes")
    public void createCrossBridgeParams(Map data) {
        
    }
    
    @SuppressWarnings("rawtypes")
    public void createCrossTunnelParams(Map data) {
        
    }
    
    @SuppressWarnings("rawtypes")
    public void createNavToTunnelParams(Map data) {
        
    }
    
    @SuppressWarnings("rawtypes")
    public void createSearchParams(Map data) {
        
    }
    
    @SuppressWarnings("rawtypes")
    public void createNavToHomeParams(Map data) {
        
    }
    
  
    @Override
    public void stop() {
      // TODO Auto-generated method stub
      
    }

}
