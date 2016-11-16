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

	/**
	 * Repeatedly attempts to access critical section and use it
	 */
	public void run() {
		while(!done) {
			this.entrySection();
			this.criticalSection();
			this.exitSection();
			//no remainder to increase odds of error
		}
	}
	
	/**
	 * Exit section of concurrency algorithm
	 */
	private void exitSection() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Entry section of concurrency algorithm (Peterson's algorithm
	 */
	private void entrySection() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Simulation critical section, might fail if multiple programs execute this concurrently
	 */
	public void criticalSection() {
		Integer originalA = (int) Math.random() % 100;
		Integer originalB = (int) Math.random() % 100;
		
		this.mainMemory.store("a", originalA);
		
		this.mainMemory.store("b", originalB);
		
		try {
			sleep(200);
		} catch (InterruptedException e) {
			//no harm no foul
		}
		
		Integer loadedA = this.mainMemory.load("a");
		
		Integer loadedB = this.mainMemory.load("b");
		
		Integer correctSum = originalA + originalB;
		Integer actualSum = loadedA + loadedB;
		
		if(actualSum != correctSum) {
			System.err.println("Error in critical section, apparently " + originalA + " + " + originalB +  " = " + actualSum);
		}
	}
	
	/**
	 * Tell the thread to stop executing
	 */
	public void done() {
		this.done = true;
	}
}
