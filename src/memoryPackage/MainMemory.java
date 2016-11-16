package memoryPackage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global hashmap of variables for the writebuffer simuator
 * @author Patrick
 *
 */
public class MainMemory {
	/**
	 * The hashmap of variables the memory object is tracking
	 */
	ConcurrentHashMap<String, Integer> data = new ConcurrentHashMap<>();
	
	/**
	 * Loads the variable index from memory
	 * @param index
	 * 		The variable name
	 * @return
	 * 		The loaded value, or null if it isn't in the memory
	 */
	public Integer load(String index) {
		return data.get(index);
	}
	
	/**
	 * Puts a variable into main memory
	 * @param index
	 * 		The name of the variable to store
	 * @param value
	 * 		The value of the variable to store
	 */
	public void store(String index, Integer value) {
		data.put(index, value);
	}
}
