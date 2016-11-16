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
	private static final String default_TSO_Index = "Patricks_Moms_Spaghetti";
	
	/**
	 * The data waiting to be stored to main memory
	 */
	private HashMap<String, ConcurrentLinkedQueue<PendingStore>> buffer;
	
	private Iterator<ConcurrentLinkedQueue<PendingStore>> PSOIterator;
	
	/**
	 * Creates a WriteBuffer with the indicated write algorithm
	 * a value of true means it is using TSO
	 * a value of false means it is using PSO
	 */
	public WriteBuffer(boolean writeAlgorithm){
		this.writeAlgorithm_isTSO = writeAlgorithm;
		
		buffer = new HashMap<String, ConcurrentLinkedQueue<PendingStore>> ();
		
	}
	
	
	/**
	 * 
	 * @return The next value that is set to to be sent to main main memory. Returns null if the buffer is empty
	 */
	public PendingStore nextValueToBeStored(){
		
		return null;
	}
	
	/**
	 * Gets the 
	 * @param index
	 * @return The most up to date value of the given variable waiting to be stored to main memory
	 * @throws NotInBufferException if the requested variable is not currently waiting to e stored
	 */
	public Integer load(String index) throws NotInBufferException{
		
		Integer returned = null;
		
		ConcurrentLinkedQueue<PendingStore> indexQueue;
		
		Iterator<PendingStore> queueIter;
		
		if(this.writeAlgorithm_isTSO == true){ // TSO method
			
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
			
		} else{ // Else if it is PSO
			
			indexQueue = buffer.get(index);
			
			if(indexQueue == null){ 
				throw new NotInBufferException();
			}
			
			queueIter = indexQueue.iterator();
			
			while(queueIter.hasNext()){
				returned = queueIter.next().getValue();
			}
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
