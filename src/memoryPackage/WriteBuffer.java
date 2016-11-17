package memoryPackage;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
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
	 * Index used to access the single element in the hashmap if a TSO is being used
	 */
	private static final String default_TSO_Index = "";
	
	
	/**
	 * The data waiting to be stored to main memory
	 * In the case where TSO is being used, there will be only one non null entry with the key default_TSO_Index
	 */
	private ConcurrentHashMap<String, ConcurrentLinkedDeque<PendingStore>> buffer;
	
	
	/**
	 * Queue that keeps track of which variables turn it is to get written to the main memory
	 */
	private ConcurrentLinkedDeque<String> storeQueue_PSO;
	
	
	/**
	 * Creates a WriteBuffer with the indicated write algorithm
	 * a value of true means it is using TSO
	 * a value of false means it is using PSO
	 */
	public WriteBuffer(boolean writeAlgorithm, MainMemory mainMemory){
		this.writeAlgorithm_isTSO = writeAlgorithm;
		if(this.writeAlgorithm_isTSO == true){
			
			//Giving an intial size of 1 and load factor of 2 ensures the minumum amount of space will be allocated for the 
			//map, since while using TSO we know there will only be one entry in the hashmap
			buffer = new ConcurrentHashMap<String, ConcurrentLinkedDeque<PendingStore>> (1, 2);
			buffer.put(default_TSO_Index, new ConcurrentLinkedDeque<PendingStore>());
			
		} else{//Otherwise if this is a PSO buffer
			
			//Give the buffer the default size and load factor
			buffer = new ConcurrentHashMap<String, ConcurrentLinkedDeque<PendingStore>>();
			storeQueue_PSO = new ConcurrentLinkedDeque<String>();
		}
		
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
		if(this.writeAlgorithm_isTSO == true){ // TSO method
			flushTSO();
		} else{
			flushVariablePSO(vName);
		}
	}
	
	
	/**
	 * Writes everything in the buffer to main memory immediatly
	 */
	private void flushTSO(){
		PendingStore store = this.nextValueToBeStored();
		while(store != null){
			this.mainMemory.store(store.getIndex(), store.getValue());
			store = this.nextValueToBeStored();
		}
	}
	
	
	/**
	 * Flushed every value in the buffer of the given variable waiting be stored to main memory
	 * @param vName - Name of the variable to be flushed
	 */
	private void flushVariablePSO(String vName){
		
		ConcurrentLinkedDeque<PendingStore> toBeFlushed = buffer.get(vName);
		while(!toBeFlushed.isEmpty()){
			PendingStore toBeStored = toBeFlushed.poll();
			this.mainMemory.store(toBeStored.getIndex(), toBeStored.getValue());
		}
		buffer.remove(vName);
	}
	
	
	/**
	 * Returns the value whos turn it is to be stored, and removes that value from the buffer
	 * @return The next value that is set to to be sent to main main memory. Returns null if the buffer is empty
	 */
	public synchronized PendingStore nextValueToBeStored(){
		
		if(this.writeAlgorithm_isTSO == true){
			return getAndRemoveNextToBeStoredTSO();
		}
		return getAndRemoveNextToBeStoredPSO();
	}
	
	
	/**
	 * Helper function to nextValueToBeStored, gets the next value for the TSO algorithm
	 * @return The next value that is set to to be sent to main main memory. Returns null if the buffer is empty
	 */
	private PendingStore getAndRemoveNextToBeStoredTSO(){
		return buffer.get(default_TSO_Index).poll();
	}
	
	
	/**
	 * Helper function to nextValueToBeStored, gets the next value for the PSO algorithm
	 * @return The next value that is set to to be sent to main main memory. Returns null if the buffer is empty
	 */
	private PendingStore getAndRemoveNextToBeStoredPSO(){
		
		if(this.storeQueue_PSO.isEmpty()){
			return null;
		}
		
		PendingStore returned = null;
		String nextToBeStoredIndex = this.storeQueue_PSO.poll();
		ConcurrentLinkedDeque<PendingStore> nextToBeStoredQueue = buffer.get(nextToBeStoredIndex);
		
		returned = nextToBeStoredQueue.poll();
		
		if(!nextToBeStoredQueue.isEmpty()){// If that was not the last value to be stored for this variable, return it to the queue
			this.storeQueue_PSO.add(nextToBeStoredIndex);
		} else{// If that was the last value to be stored for that variable, remove its queue from the buffer map
			//removed the put null here
			buffer.remove(nextToBeStoredIndex);
		}
		
		return returned;
	}
	
	
	/**
	 * Gets the most up to date value of the given index.
	 * @param index - the name of the variable being loaded
	 * @return The most up to date value of the given variable waiting to be stored to main memory
	 * @throws NotInBufferException if the requested variable is not currently waiting to be stored
	 */
	public synchronized Integer load(String index) throws NotInBufferException{
		
		if(this.writeAlgorithm_isTSO == true){ // TSO method
			return loadTSO(index);
		} else{
			return loadPSO(index);
		}
	}
	
	
	/**
	 * Helper function for load, loads using TSO algorithm
	 * @param index - the name of the variable being loaded
	 * @return The most up to date value of the given variable waiting to be stored to main memory
	 * @throws NotInBufferException - if the requested variable is not currently waiting to be stored
	 */
	private Integer loadTSO(String index) throws NotInBufferException{
		synchronized(buffer){
			ConcurrentLinkedDeque<PendingStore> indexQueue;
			
			Iterator<PendingStore> queueIter;
			
			indexQueue = buffer.get(default_TSO_Index);
			
			queueIter = indexQueue.descendingIterator();
			
			while(queueIter.hasNext()){
				PendingStore queueVal = queueIter.next();
				if(queueVal.getIndex().equals(index)){
					return queueVal.getValue();
				}
			}
		}
		throw new NotInBufferException();
		
		
	}
	
	
	/**
	 * Helper function for load, loads using PSO algorithm
	 * @param index - the name of the variable being loaded
	 * @return The most up to date value of the given variable waiting to be stored to main memory
	 * @throws NotInBufferException - if the requested variable is not currently waiting to be stored
	 */
	private Integer loadPSO(String index) throws NotInBufferException{

		
		ConcurrentLinkedDeque<PendingStore> indexQueue;
		
		Iterator<PendingStore> queueIter;
		
		indexQueue = buffer.get(index);
		
		if(indexQueue == null){ 
			throw new NotInBufferException();
		}
		
		queueIter = indexQueue.descendingIterator();

		return queueIter.next().getValue();// The value at the tail of the queue is the most up to date one
		
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
		buffer.get(default_TSO_Index).add(new PendingStore(index, value));
	}
	
	
	/**
	 * Helper function to store, does the store for th PSO algorithm
	 * @param index - Name of the variable
	 * @param value - value of the variable
	 */
	private void storePSO(String index, Integer value){
		PendingStore ToBeAdded = new PendingStore(index, value);
		
		ConcurrentLinkedDeque<PendingStore> variablesQueue = buffer.get(index);
		
		if(variablesQueue == null){// If the variable is not currently in the buffer
			
			//Add a queue for this variable to the buffer, add variable to the write queue
			variablesQueue = new ConcurrentLinkedDeque<PendingStore>();
			variablesQueue.add(ToBeAdded);
			buffer.put(index, variablesQueue);
			storeQueue_PSO.add(index);
		} else{// If the variable is already in the buffer
			//Just add this value to the variables queue, it should already be in the write queue
			variablesQueue.add(ToBeAdded);
		}
		
	}
	
	
	/**
	 * Checks if a variable with the given name is waiting to be stored in this buffer
	 * @param index - variable name to search for
	 * @return true if the variable is waiting to be written, false otherwise
	 */
	public synchronized boolean isVariableInBuffer(String index){
		if(this.writeAlgorithm_isTSO == true){
			return isVariableInBufferTSO(index);
		} else{
			return isVariableInBufferPSO(index);
		}
	}
	
	
	/**
	 * Checks if the variable is in the buffer for the TSO algorithm
	 * @param index - variable name to search for
	 * @return true if the variable is waiting to be written, false otherwise
	 */
	private boolean isVariableInBufferTSO(String index){
		
		Iterator<PendingStore> tsoIter = buffer.get(default_TSO_Index).iterator();
		
		while(tsoIter.hasNext()){
			if(tsoIter.next().getIndex().equals(index)){
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Checks if the variable is in the buffer for the TSO algorithm
	 * @param index - variable name to search for
	 * @return true if the variable is waiting to be written, false otherwise
	 */
	private boolean isVariableInBufferPSO(String index){
		if(buffer.get(index) == null){
			return false;
		} else{
			return true;
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
