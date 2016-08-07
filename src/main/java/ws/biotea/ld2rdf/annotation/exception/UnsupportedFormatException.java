package ws.biotea.ld2rdf.annotation.exception;

public class UnsupportedFormatException extends Exception {
	private static final long serialVersionUID = 1L;
	public UnsupportedFormatException() {
		super();
	}
    public UnsupportedFormatException(String message) {
		super(message);
	} 
    public UnsupportedFormatException(String message, Throwable cause) {
		super(message, cause);
	}
    public UnsupportedFormatException(Throwable cause) {
    	super(cause);
    }
}
