package bothSidesExceptions;

public class CommunicationProtocolException extends Exception{
	private static final long serialVersionUID = 1L;

	public CommunicationProtocolException(){
		super();
	}

	public CommunicationProtocolException(String message){
		super(message);
	}
}