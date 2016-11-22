package memoryPackage;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 * Buffer for the MainMemory, 
 * @author joshua walters
 */
public class WriteBuffer {
	
	private MainMemory mainMemory;
	
	/**
	 * If true buffer uses TSO, FIFO
	 * If false buffer uses PSO, and it is FIFO per variable
	 */
	private final boolean writeAlgorithm_isTSO;
	
	
	/**
	 * Queue that keeps track of which variables turn it is to get written to the main memory
	 */
	private ConcurrentLinkedDeque<PendingStore> storeQueue;

	private boolean pendingStore;
	
	
	/**
	 * Creates a WriteBuffer with the indicated write algorithm
	 * a value of true means it is using TSO
	 * a value of false means it is using PSO
	 */
	public WriteBuffer(boolean writeAlgorithm, MainMemory mainMemory){
		this.writeAlgorithm_isTSO = writeAlgorithm;
		
		storeQueue = new ConcurrentLinkedDeque<PendingStore>();
		
		this.mainMemory = mainMemory;
		
	}
	
	
	/**
	 * Swaps the value of the given variable in main memory with the new given value and returns its old value
	 * @param vName - Name of the variable you are swapping the value of
	 * @param newValue - The new value for the variable
	 * @return Old value of the Variable
	 */
	public synchronized Integer SwapAtomic(String vName, Integer newValue){
		Integer oldValue = null;
		
		try{
			oldValue = this.load(vName);
		} catch(NotInBufferException e){
			oldValue = this.mainMemory.load(vName);
		}
		
		this.store(vName, newValue);
		this.flushVariable(vName);
		
		return oldValue;
	}
	
	
	/**
	 * If TSO - flushed the entire buffer to be sotred immediatly into main memory
	 * if PSO - Flushes all values of the given variable name to the main memory
	 * @param vName - Name of the variable to be flushed
	 */
	private void flushVariable(String vName){
		this.pendingStore = true;
		PendingStore store = this.nextValueToBeStored();
		while(store != null){
			this.mainMemory.store(store.getIndex(), store.getValue());
			store = this.nextValueToBeStored();
		}
		this.storeComplete();
	}
	
	
	/**
	 * Returns the value whos turn it is to be stored, and removes that value from the buffer
	 * @return The next value that is set to to be sent to main main memory. Returns null if the buffer is empty
	 */
	public synchronized PendingStore nextValueToBeStored(){
		this.pendingStore = true;
		return this.storeQueue.poll();
	}
	
	/**
	 * Notification from the memory agent that it has completed storing a variable to main memory
	 */
	public synchronized void storeComplete() {
		this.pendingStore = false;
		notifyAll();
	}
	
	
	/**
	 * Gets the most up to date value of the given index.
	 * @param index - the name of the variable being loaded
	 * @return The most up to date value of the given variable waiting to be stored to main memory
	 * @throws NotInBufferException if the requested variable is not currently waiting to be stored
	 */
	public synchronized Integer load(String index) throws NotInBufferException{
		
		while(pendingStore){
			try {
				wait();
			} catch (InterruptedException e) { }
		}
		
		Iterator<PendingStore> queueIter;
		
		queueIter = storeQueue.descendingIterator();
		
		while(queueIter.hasNext()){
			PendingStore queueVal = queueIter.next();
			if(queueVal.getIndex().equals(index)){
				return queueVal.getValue();
			}
		}
		
		throw new NotInBufferException();
	}
	
	
	/**
	 * Adds the given value for the given variable to the buffer
	 * @param index - Name of the variable
	 * @param value - value of the variable
	 */
	public synchronized void store(String index, Integer value){
		if(this.writeAlgorithm_isTSO == true){
			storeTSO(index, value);
		} else{
			storePSO(index, value);
		}
	}
	
	
	/**
	 * Helper function to store, does the store for th TSO algorithm
	 * @param index - Name of the variable
	 * @param value - value of the variable
	 */
	private void storeTSO(String index, Integer value){
		storeQueue.add(new PendingStore(index, value));
	}
	
	
	/**
	 * Helper function to store, does the store for th PSO algorithm
	 * @param index - Name of the variable
	 * @param value - value of the variable
	 */
	private void storePSO(String index, Integer value){
		try{
			this.load(index);
			storeTSO(index, value);
		}catch(NotInBufferException e){
			storeQueue.addFirst(new PendingStore(index, value));
		}
	}
	
	
	/**
	 * a value of true means it is using TSO
	 * a value of false means it is using PSO
	 * @return A boolean indicating the type of store algorithm being used for this buffer
	 */
	public boolean getAlgorithmUsed(){
		return this.writeAlgorithm_isTSO;
	}
}
