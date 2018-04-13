package fsm;

/**
 * Interface specifying a task.
 * @author Yasasa
 */
public interface Task {
  /**
   * Called at the start of a task entry.
   * @param prevTaskSuccess Success of the immideate dependency tasks.
   * @return true on success false otherwise.
   */
  boolean start(boolean prevTaskSuccess);
  
  /**
   * Called when the task has to be exit early. If this call is ignored then the task
   * manager will not do anything.
   */
  void stop();
}
