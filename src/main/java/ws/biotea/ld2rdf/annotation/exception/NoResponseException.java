package ws.biotea.ld2rdf.annotation.exception;

public class NoResponseException extends Exception {
	private static final long serialVersionUID = 1L;
	public NoResponseException() {
		super();
	}
    public NoResponseException(String message) {
		super(message);
	} 
    public NoResponseException(String message, Throwable cause) {
		super(message, cause);
	}
    public NoResponseException(Throwable cause) {
    	super(cause);
    }
}
