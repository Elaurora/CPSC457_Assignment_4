package memoryPackage;
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
	
	/**
	 * The buffer for the processor to write too, and load from
	 */
	private WriteBuffer writeBuffer;
	
	/**
	 * The backup memory unit to load from
	 */
	private MainMemory mainMemory;
	
	public Processor(WriteBuffer writeBuffer, MainMemory mainMemory) {
		this.writeBuffer = writeBuffer;
		this.mainMemory = mainMemory;
	}

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
