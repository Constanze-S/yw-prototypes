package org.yesworkflow.annotations;

import org.yesworkflow.YWKeywords;
import org.yesworkflow.YWKeywords.Tag;

/**
 * This class defines the "@LOC" annotation.
 */
public class Loc extends Annotation {

	/**
	 * Constructs a Loc object
	 * 
	 * @param id         The ID of the annotation.
	 * @param sourceId   The ID of the source file.
	 * @param lineNumber The line number where the annotation was found.
	 * @param comment    The corresponding comment.
	 * @throws Exception If an error occurs, an exception is thrown.
	 */
	public Loc(Long id, Long sourceId, Long lineNumber, String comment) throws Exception {
		super(id, sourceId, lineNumber, comment, YWKeywords.Tag.LOC);
	}

	/**
	 * Constructs a Loc object.
	 * 
	 * @param id         The ID of the annotation.
	 * @param sourceId   The ID of the source file.
	 * @param lineNumber The line number where the annotation was found.
	 * @param comment    The corresponding comment.
	 * @param tag        The type of the annotation.
	 * @throws Exception If an error occurs, an exception is thrown.
	 */
	public Loc(Long id, Long sourceId, Long lineNumber, String comment, Tag tag) throws Exception {
		super(id, sourceId, lineNumber, comment, tag);
	}

	/**
	 * Concatenation of certain fields as a string.
	 */
	@Override
	public String toString() {
		// Taken from Flow.java
		StringBuffer sb = new StringBuffer();

		sb.append(keyword).append("{value=").append(value);

		if (this.description() != null) {
			sb.append(",description=").append(description());
		}

		sb.append("}");

		return sb.toString();
	}
}
