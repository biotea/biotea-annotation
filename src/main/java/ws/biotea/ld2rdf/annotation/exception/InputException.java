package ws.biotea.ld2rdf.annotation.exception;

public class InputException extends Exception {
	private static final long serialVersionUID = 1L;
	public InputException() {
		super();
	}
    public InputException(String message) {
		super(message);
	} 
    public InputException(String message, Throwable cause) {
		super(message, cause);
	}
    public InputException(Throwable cause) {
    	super(cause);
    }
}
