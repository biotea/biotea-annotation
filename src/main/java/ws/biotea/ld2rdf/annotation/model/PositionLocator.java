package ws.biotea.ld2rdf.annotation.model;

public class PositionLocator {
	private int from, to;			

	public PositionLocator(int from, int to) {
		this.from = from;
		this.to = to;
	}

	/**
	 * @return the from
	 */
	public int getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(int from) {
		this.from = from;
	}

	/**
	 * @return the to
	 */
	public int getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(int to) {
		this.to = to;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + from;
		result = prime * result + to;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PositionLocator))
			return false;
		PositionLocator other = (PositionLocator) obj;
		if (from != other.from)
			return false;
		if (to != other.to)
			return false;
		return true;
	}

	
	
} 