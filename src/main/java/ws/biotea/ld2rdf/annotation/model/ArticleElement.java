package ws.biotea.ld2rdf.annotation.model;

public class ArticleElement {
	private String identifier;
	private String text;
	
	public ArticleElement(String id, String text) {
		this.identifier = id;
		this.text = text;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	

}
