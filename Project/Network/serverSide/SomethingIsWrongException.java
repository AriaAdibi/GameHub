package serverSide;

public class SomethingIsWrongException extends Exception{
	private static final long serialVersionUID = 1L;

	public SomethingIsWrongException() {
		super();
	}

	public SomethingIsWrongException(String message) {
		super(message);
	}
	
}