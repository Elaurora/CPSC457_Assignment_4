package memoryPackage;
/**
 * Used by the WriteBuffer Method when load is called on
 * @author joshua
 *
 */
public class NotInBufferException extends Exception{

	/**
	 * Stops an eclipse error :)
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Ctor w/ message
	 * @param message
	 */
	public NotInBufferException(String message) {
		super(message);
	}
	
	/**
	 * Ctor w/ out message
	 */
	public NotInBufferException() {
		super("NotInBufferException");
	}

}
