package ws.biotea.ld2rdf.annotation.exception;

import java.util.Date;

import org.springframework.http.HttpStatus;

public class ErrorResource {
    private long timestamp;
    private HttpStatus httpStatus;
    private String message, path;
    private String exception;
    
	public ErrorResource(long timestamp, HttpStatus httpStatus,
			String message, String path, String exception) {
		super();
		this.timestamp = timestamp;
		this.httpStatus = httpStatus;
		this.message = message;
		this.path = path;
		this.exception = exception;
	}
	
	public ErrorResource(HttpStatus httpStatus,
			String message, String path, String exception) {
		this(new Date().getTime(), httpStatus, message, path, exception);
	}
	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return the status
	 */
	public int getStatus() {
		return httpStatus.value();
	}
	/**
	 * @return the error
	 */
	public String getError() {
		return httpStatus.getReasonPhrase();
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
	/**
	 * @return the exception
	 */
	public String getException() {
		return exception;
	}
	/**
	 * @param exception the exception to set
	 */
	public void setException(String exception) {
		this.exception = exception;
	}
}
