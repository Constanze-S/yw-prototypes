package org.yesworkflow.annotations;

import org.yesworkflow.YWKeywords;

/**
 * This class defines the "@DATA" annotation.
 */
public class Data extends AliasableAnnotation {

	/**
	 * Constructs a Data object.
	 * 
	 * @param id         The ID of the annotation.
	 * @param sourceId   The ID of the source file.
	 * @param lineNumber The line number where the annotation was found.
	 * @param comment    The corresponding comment.
	 * @throws Exception If an error occurs, an exception is thrown.
	 */
	public Data(Long id, Long sourceId, Long lineNumber, String comment) throws Exception {
		super(id, sourceId, lineNumber, comment, YWKeywords.Tag.DATA);
	}

	/**
	 * Concatenation of certain fields as a string.
	 */
	@Override
	public String toString() {
		// Taken from Flow.java
		StringBuffer sb = new StringBuffer();

		sb.append(keyword).append("{value=").append(value);

		if (as != null) {
			sb.append(",alias=").append(as.value);
		}

		if (this.description() != null) {
			sb.append(",description=").append(description());
		}

		sb.append("}");

		return sb.toString();
	}
}
