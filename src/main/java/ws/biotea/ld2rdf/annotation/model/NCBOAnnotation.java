package ws.biotea.ld2rdf.annotation.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NCBOAnnotation {	
	private List<String> annotatedClassIds;
	private ArrayList<String> sourceOntologies;
	private Set<PositionLocator> annotationFromTo;
	private String annotationText;
	
	public NCBOAnnotation() {
		this.annotatedClassIds = new ArrayList<String>();
		this.setSourceOntologies(new ArrayList<String>());
		this.annotationFromTo = new HashSet<>();
	}

	public NCBOAnnotation(String text) {
		this();
		this.annotationText = text;
	}

	/**
	 * @return the annotationText
	 */
	public String getAnnotationText() {
		return annotationText;
	}

	/**
	 * @param annotationText the annotationText to set
	 */
	public void setAnnotationText(String annotationText) {
		this.annotationText = annotationText;
	}	
	
	/**
	 * @return the annotatedClassIds
	 */
	public List<String> getAnnotatedClassIds() {
		return annotatedClassIds;
	}

	public ArrayList<String> getSourceOntologies() {
		return sourceOntologies;
	}

	public void setSourceOntologies(ArrayList<String> sourceOntologies) {
		this.sourceOntologies = sourceOntologies;
	}

	/**
	 * @return the frequency
	 */
	public int getFrequency() {
		return this.annotationFromTo.size();
	}
	
	/**
	 * @return the annotationFromTo
	 */
	public Set<PositionLocator> getAnnotationFromTo() {
		return this.annotationFromTo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotationText == null) ? 0 : annotationText.hashCode());
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
		if (!(obj instanceof NCBOAnnotation))
			return false;
		NCBOAnnotation other = (NCBOAnnotation) obj;
		if (annotationText == null) {
			if (other.annotationText != null)
				return false;
		} else if (!annotationText.equals(other.annotationText))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NCBOAnnotation [annotatedClassIds=");
		builder.append(annotatedClassIds);
		builder.append(", sourceOntology=");
		builder.append(sourceOntologies);
		builder.append(", annotationFromTo=");
		builder.append(annotationFromTo);
		builder.append(", annotationText=");
		builder.append(annotationText);
		builder.append("]");
		return builder.toString();
	}
	
}
