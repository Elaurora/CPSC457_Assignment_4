package memoryPackage;
/**
 * A simulation of a processor (process) using the simulated memory system
 * @author Patrick
 *
 */
public class Processor extends Thread {
	/**
	 * This processes ID
	 */
	private int id = -1;
	
	/**
	 * Indicates if the thread should terminate
	 */
	private boolean done = false;
	
	/**
	 * The buffer for the processor to write too, and load from
	 */
	private WriteBuffer writeBuffer;
	
	/**
	 * How many processes exist in the system
	 */
	private int processCount = -1;
	
	/**
	 * The backup memory unit to load from
	 */
	private MainMemory mainMemory;
	
	public Processor(int id, int processCount, WriteBuffer writeBuffer, MainMemory mainMemory) {
		this.id = id;
		this.processCount = processCount;
		this.writeBuffer = writeBuffer;
		this.mainMemory = mainMemory;
		
		this.mainMemory.store("process" + this.id + "level", -1);
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
	 * Exit section of concurrency algorithm, move the current process out of all queues
	 */
	private void exitSection() {
		this.writeBuffer.store("process" + this.id + "level", -1);
		
	}

	/**
	 * Entry section of concurrency algorithm (Peterson's algorithm)
	 * Blocks until the current process has permission to enter the critical section
	 */
	private void entrySection() {
		for(int level = 0; level < this.processCount - 1; level++) {
			this.waitAtLevel(level);
		}		
	}

	/**
	 * Enqueues the process at the provided level, and then blocks until the process has permission to move up a level
	 * @param level
	 * 		The level to enter at
	 */
	private void waitAtLevel(int level) {
		this.writeBuffer.store("process" + this.id + "level", level);
		this.writeBuffer.store("level" + level + "turn", this.id);
		
		while(processAboveUs(level) && weAreLastProcessIntoOurLevel(level)) {
			//try {
				//sleep(1);
			//} catch (InterruptedException e) {
				//nothing to handle
			//}
		}
	}

	/**
	 * Checks if the last process into the provided level was this process
	 * @param level
	 * 		The level to check at
	 * @return
	 * 		true if the condition passes, false otherwise
	 */
	private boolean weAreLastProcessIntoOurLevel(int level) {
		int turn = -1;
		try {
			turn = this.writeBuffer.load("level" + level + "turn");
		} catch (NotInBufferException e) {
			turn = this.mainMemory.load("level" + level + "turn");
		}
		
		if(turn == this.id){
			return true;
		}
		
		return false;
	}

	/**
	 * Checks if there is a process that isn't this one in or above the provided level
	 * @param level
	 * 		The level to start checking at
	 * @return
	 * 		true if such a process exists, false otherwise
	 */
	private boolean processAboveUs(int level) {
		for(int j = 0; j < this.processCount; j++) {
			if(j == this.id){
				continue;
			}
			
			int processLevel = -1;
			try {
				processLevel = this.writeBuffer.load("process" + j + "level");
			} catch (NotInBufferException e) {
				processLevel = this.mainMemory.load("process" + j + "level");
			}
			
			if(processLevel >= level) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Simulation critical section, might fail if multiple programs execute this concurrently
	 */
	public void criticalSection() {
		int originalA = (int) (Math.random() * 100);
		int originalB = (int) (Math.random() * 100);
		
		
		this.writeBuffer.store("a", originalA);
		this.writeBuffer.store("b", originalB);
		
		try {
			sleep(200);
		} catch (InterruptedException e) {
			//no harm no foul
		}
		
		int loadedA = -1;
		int loadedB = -1;
		
		try {
			loadedA = this.writeBuffer.load("a");
			loadedB = this.writeBuffer.load("b");
		} catch (NotInBufferException e) {
			loadedA = this.mainMemory.load("a");
			loadedB = this.mainMemory.load("b");
		}
		
		
		int correctSum = originalA + originalB;
		int actualSum = loadedA + loadedB;
		
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
