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
	 * How many error in the critical section this class has detected
	 */
	public static int errorTotal = 0;
	
	/**
	 * How many successes (defined as not errors) have occured in the critical section
	 */
	public static int successTotal = 0;
	
	/**
	 * The backup memory unit to load from
	 */
	private MainMemory mainMemory;
	
	/**
	 * A mutable integer for critical section demos
	 */
	private MutableInteger a;
	
	/**
	 * A mutable integer for critical section demos
	 */
	private MutableInteger b;

	/**
	 * The original value of a, what it should be
	 */
	private int originalA;

	/**
	 * The original value of b, what it should be
	 */
	private int originalB;
	
	/**
	 * Initialzies a processor to run
	 * @param id
	 * 		This processors ID
	 * @param processCount
	 * 		How many processors exist in total
	 * @param writeBuffer
	 * 		The buffer for writing to
	 * @param mainMemory
	 * 		Main memory for writing to
	 * @param a
	 * 		A shared integer for critical section demos
	 * @param b
	 * 		A shared integer for critical section demos
	 */
	public Processor(int id, int processCount, WriteBuffer writeBuffer, MainMemory mainMemory, MutableInteger a, MutableInteger b) {
		this.id = id;
		this.processCount = processCount;
		this.writeBuffer = writeBuffer;
		this.mainMemory = mainMemory;
		this.a = a;
		this.b = b;
		//initialize our level flag, this must be done to ensure that the initialization is done before the program starts
		this.mainMemory.store("process" + this.id + "level", -1);
	}

	/**
	 * Repeatedly attempts to access critical section and use it
	 */
	public void run() {
		while(!done) {
			originalA = (int) (Math.random() * 100);
			originalB = (int) (Math.random() * 100);
			
			
			
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
		//this.writeBuffer.store("process" + this.id + "level", -1);
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
		
		//this.writeBuffer.store("process" + this.id + "level", level);
		this.writeBuffer.store("process" + this.id + "level", level);
		
		//this.writeBuffer.store("level" + level + "turn", this.id);
		this.writeBuffer.store("level" + level + "turn", this.id);
		
//		try {
//			sleep(100);
//		} catch (InterruptedException e) {
//			//nothing to handle
//		}
		
		while(processAboveUs(level) && weAreLastProcessIntoOurLevel(level)) {	
			try {
				sleep(1);
			} catch (InterruptedException e) {
				//nothing to handle
			}
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
		this.a.value = originalA;
		this.b.value = originalB;
		
		try {
			sleep(1);
		} catch (InterruptedException e) {
			//no harm no foul
		}
		
		int loadedA = this.a.value;
		int loadedB = this.b.value;
		
		int correctSum = originalA + originalB;
		int actualSum = loadedA + loadedB;
		
		if(actualSum != correctSum) {
			System.err.println("Error in critical section, apparently " + originalA + " + " + originalB +  " = " + actualSum);
			errorTotal++;
		} else {
			successTotal++;
			//System.out.println("Success in critical section");
		}
	}
	
	/**
	 * Tell the thread to stop executing
	 */
	public void done() {
		this.done = true;
	}
}
