package ws.biotea.ld2rdf.util.ncbo.annotator;

public class NCBOOntology {
	private String virtualId;
	private String description;
	private String ns;
	private String url;
	private String acronym;
	public NCBOOntology(String virtualId, String description, String ns, String url, String acronym) {
		this.virtualId = virtualId;
		this.description = description;
		this.ns = ns;
		this.url = url;
		this.acronym = acronym;
	}
	/**
	 * @return the virtualId
	 */
	public String getVirtualId() {
		return virtualId;
	}

	/**
	 * @return the name
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the prefix
	 */
	public String getNS() {
		return ns;
	}

	/**
	 * @return the url
	 */
	public String getURL() {
		return url;
	}
	
	/**
	 * @return the acronym
	 */
	public String getAcronym() {
		return acronym;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "NCBOOntology [virtualId=" + virtualId + ", description="
				+ description + ", ns=" + ns + ", url=" + url + ", acronym="
				+ acronym + "]";
	}
	
}
