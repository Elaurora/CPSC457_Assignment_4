/**
 * Represents a pending store to mainMemory
 * @author Patrick
 *
 */
public class PendingStore {
	/**
	 * The name of the variable to store
	 */
	private String index;
	
	/**
	 * The value of the variable to store
	 */
	private Integer value;
	
	/**
	 * Creates a new pending Store
	 * @param index
	 * 		he name of the variable to store
	 * @param value
	 * 		 The value of the variable to store
	 */
	public PendingStore(String index, Integer value) {
		this.index = index;
		this.value = value;
	}

	public String getIndex() {
		return index;
	}

	public Integer getValue() {
		return value;
	}
}
