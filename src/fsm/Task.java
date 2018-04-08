package fsm;

/**
 * Task interface to be used by the TaskManager and Main classes.
 * Includes start boolean and stop method.
 * 
 */
public interface Task {
  boolean start(boolean prevTaskSuccess);
  void stop();
}
