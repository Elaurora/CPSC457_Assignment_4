package memoryPackage;


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
	private boolean tso;
	
	
	
	/**
	 * Creates a WriteBuffer with the indicated write 
	 */
	public WriteBuffer(boolean writeAlgorithm){
		this.tso = writeAlgorithm;
	}
	
	
	/**
	 * 
	 * @return The next value that is set to to be sent to main main memory. Returns null if the buffer is empty
	 */
	public PendingStore nextValueToBeStored(){
		//TODO:this
		
		return null;
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public Integer load(String index){
		//TODO:this
		
		return null;
	}
	
	/**
	 * 
	 * @param index
	 * @param value
	 */
	public void store(String index, Integer value){
		//TODO:this
	}
	
}
