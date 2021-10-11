package bothSidesExceptions;

public class WrongArgsException extends Exception{
	private static final long serialVersionUID = 1L;

	public WrongArgsException(){
		super();
	}

	public WrongArgsException(String message){
		super(message);
	}
}