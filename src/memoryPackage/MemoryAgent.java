package memoryPackage;

/**
 * Seperate thread that reads values out of the writebuffer and into mainMemory
 * @author Patrick
 */
public class MemoryAgent extends Thread {
	/**
	 * The buffer of stores that have yet to be commited to main memory
	 */
	private WriteBuffer pendingStoreBuffer;
	
	/**
	 * The main memory object to accept stores
	 */
	private MainMemory mainMemory;
	
	/**
	 * Indicates if the thread should terminate
	 */
	private boolean done = false;
	
	/**
	 * Assigns the buffer and main memory objects
	 * @param buffer
	 * 		The buffer of stores that have yet to be commited to main memory
	 * @param mainMemory
	 * 		The main memory object to accept stores
	 */
	public MemoryAgent(WriteBuffer buffer, MainMemory mainMemory) {
		this.pendingStoreBuffer = buffer;
		
		this.mainMemory = mainMemory;
	}
	
	/**
	 * Continously reads values out of writeBuffer and stores them to main memory
	 */
	public void run() {
		//while we aren't done, read a value from the buffer, 
		//and if it existed, store it into main memory
		while(!done) {
			PendingStore store = this.pendingStoreBuffer.nextValueToBeStored();
			
			if(store != null) {
				this.mainMemory.store(store.getIndex(), store.getValue());
			}
		}
	}
	
	/**
	 * Tells the thread that it is time to quit
	 */
	public void done() {
		this.done = true;
	}
	
}
