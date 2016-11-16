/**
 * 
 * @author Patrick
 */
public class MemoryAgent extends Thread {
	private WriteBuffer pendingStoreBuffer;
	private MainMemory mainMemory;
	
	public MemoryAgent(WriteBuffer buffer, MainMemory mainMemory) {
		this.pendingStoreBuffer = buffer;
		
		this.mainMemory = mainMemory;
	}
	
	public void run() {
		
	}
	
	
}
