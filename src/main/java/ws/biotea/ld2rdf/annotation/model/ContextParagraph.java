package ws.biotea.ld2rdf.annotation.model;

public class ContextParagraph {
	private int lastPosition;
	private String contextURL;
	
	public ContextParagraph(int lastPosition, String contextURL) {
		this.lastPosition = lastPosition;
		this.contextURL = contextURL;
	}

	/**
	 * @return the lastPosition
	 */
	public int getLastPosition() {
		return lastPosition;
	}

	/**
	 * @param lastPosition the lastPosition to set
	 */
	public void setLastPosition(int lastPosition) {
		this.lastPosition = lastPosition;
	}

	/**
	 * @return the contextURL
	 */
	public String getContextURL() {
		return contextURL;
	}

	/**
	 * @param contextURL the contextURL to set
	 */
	public void setContextURL(String contextURL) {
		this.contextURL = contextURL;
	}

}
