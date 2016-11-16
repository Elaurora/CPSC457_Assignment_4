package MemoryPackage;
/**
 * A simulation of a processor (process) using the simulated memory system
 * @author Patrick
 *
 */
public class Processor extends Thread {
	/**
	 * Indicates if the thread should terminate
	 */
	private boolean done = false;
	
	public void run() {
		while(!done) {
			//TODO: stuff
		}
	}
	
	/**
	 * Tell the thread to stop executing
	 */
	public void done() {
		this.done = true;
	}
}
