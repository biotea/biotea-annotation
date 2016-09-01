package ws.biotea.ld2rdf.annotation.model;

import java.math.BigInteger;

public class PositionLocator {
	private BigInteger from, to;			

	public PositionLocator(BigInteger from, BigInteger to) {
		this.from = from;
		this.to = to;
	}

	/**
	 * @return the from
	 */
	public BigInteger getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(BigInteger from) {
		this.from = from;
	}

	/**
	 * @return the to
	 */
	public BigInteger getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(BigInteger to) {
		this.to = to;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}
	
} 