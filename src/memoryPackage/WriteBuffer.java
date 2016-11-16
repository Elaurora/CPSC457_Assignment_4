package memoryPackage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * 
 * @author joshua
 *
 */
public class WriteBuffer {
	
	
	/**
	 * If true buffer uses TSO, FIFO
	 * If false buffer uses PSO, and it is FIFO per variable
	 */
	private final boolean writeAlgorithm_isTSO;
	
	
	/**
	 * Index used to access the single element in the hashmap if a TSO is being used
	 */
	private static final String default_TSO_Index = "Patrick's_Mom's_Spaghetti";
	
	
	/**
	 * The data waiting to be stored to main memory
	 * In the case where TSO is being used, there will be only one non null entry with the key default_TSO_Index
	 */
	private HashMap<String, ConcurrentLinkedQueue<PendingStore>> buffer;
	
	
	/**
	 * Queue that keeps track of which variables turn it is to get written to the main memory
	 */
	private ConcurrentLinkedQueue<String> storeQueue_PSO;
	
	
	/**
	 * Creates a WriteBuffer with the indicated write algorithm
	 * a value of true means it is using TSO
	 * a value of false means it is using PSO
	 */
	public WriteBuffer(boolean writeAlgorithm){
		this.writeAlgorithm_isTSO = writeAlgorithm;
		if(this.writeAlgorithm_isTSO == true){
			
			//Giving an intial size of 1 and load factor of 2 ensures the minumum amount of space will be allocated for the 
			//map, since while using TSO we know there will only be one entry in the hashmap
			buffer = new HashMap<String, ConcurrentLinkedQueue<PendingStore>> (1, 2);
			buffer.put(default_TSO_Index, new ConcurrentLinkedQueue<PendingStore>());
			
		} else{//Otherwise if this is a PSO buffer
			
			//Give the buffer the default size and load factor
			buffer = new HashMap<String, ConcurrentLinkedQueue<PendingStore>>();
			storeQueue_PSO = new ConcurrentLinkedQueue<String>();
		}
		
	}
	
	
	/**
	 * 
	 * @return The next value that is set to to be sent to main main memory. Returns null if the buffer is empty
	 */
	public PendingStore nextValueToBeStored(){
		
		if(this.writeAlgorithm_isTSO == true){
			return getAndRemoveNextToBeStoredTSO();
		}
		return getAndRemoveNextToBeStoredPSO();
	}
	
	
	/**
	 * 
	 * @return
	 */
	private PendingStore getAndRemoveNextToBeStoredTSO(){
		return buffer.get(default_TSO_Index).poll();
	}
	
	
	/**
	 * 
	 * @return
	 */
	private PendingStore getAndRemoveNextToBeStoredPSO(){
		
		if(this.storeQueue_PSO.isEmpty()){
			return null;
		}
		
		PendingStore returned = null;
		String nextToBeStoredIndex = this.storeQueue_PSO.poll();
		ConcurrentLinkedQueue<PendingStore> nextToBeStoredQueue = buffer.get(nextToBeStoredIndex);
		
		returned = nextToBeStoredQueue.poll();
		
		if(!nextToBeStoredQueue.isEmpty()){// If that was not the last value to be stored for this variable, return it to the queue
			this.storeQueue_PSO.add(nextToBeStoredIndex);
		} else{// If that was the last value to be stored for that variable, remove its queue from the buffer map
			buffer.put(nextToBeStoredIndex, null);
		}
		
		return returned;
	}
	
	
	/**
	 * Gets the most up to date value of the given index.
	 * @param index - the name of the variable being loaded
	 * @return The most up to date value of the given variable waiting to be stored to main memory
	 * @throws NotInBufferException if the requested variable is not currently waiting to be stored
	 */
	public Integer load(String index) throws NotInBufferException{
		
		if(this.writeAlgorithm_isTSO == true){ // TSO method
			return loadTSO(index);
		}
		return loadPSO(index);
	}
	
	
	/**
	 * Helper function for load, loads using TSO algorithm
	 * @param index - the name of the variable being loaded
	 * @return The most up to date value of the given variable waiting to be stored to main memory
	 * @throws NotInBufferException - if the requested variable is not currently waiting to be stored
	 */
	private Integer loadTSO(String index) throws NotInBufferException{
		Integer returned = null;
		
		ConcurrentLinkedQueue<PendingStore> indexQueue;
		
		Iterator<PendingStore> queueIter;
		
		indexQueue = buffer.get(default_TSO_Index);
		
		queueIter = indexQueue.iterator();
		
		while(queueIter.hasNext()){
			PendingStore queueVal = queueIter.next();
			if(queueVal.getIndex().equals(index)){
				returned = queueVal.getValue();
			}
		}
		
		if(returned == null){// If it is still null then no instances of the given index were foudn in the queue
			throw new NotInBufferException();
		}
		
		return returned;
	}
	
	/**
	 * Helper function for load, loads using PSO algorithm
	 * @param index - the name of the variable being loaded
	 * @return The most up to date value of the given variable waiting to be stored to main memory
	 * @throws NotInBufferException - if the requested variable is not currently waiting to be stored
	 */
	private Integer loadPSO(String index) throws NotInBufferException{
		Integer returned = null;
		
		ConcurrentLinkedQueue<PendingStore> indexQueue;
		
		Iterator<PendingStore> queueIter;
		
		indexQueue = buffer.get(index);
		
		if(indexQueue == null){ 
			throw new NotInBufferException();
		}
		
		queueIter = indexQueue.iterator();
		
		while(queueIter.hasNext()){
			returned = queueIter.next().getValue();
		}
		
		return returned;
	}
	
	/**
	 * Adds the 
	 * @param index
	 * @param value
	 */
	public void store(String index, Integer value){
		
	}
	
	/**
	 * a value of true means it is using TSO
	 * a value of false means it is using PSO
	 * @return A boolean indicating the type of store algorithm being used for this buffer
	 */
	public boolean getAlgorithm(){
		return this.writeAlgorithm_isTSO;
	}
}
