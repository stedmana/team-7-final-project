package fsm;

public interface Task {
  boolean start(boolean prevTaskSuccess);
  void stop();
}
