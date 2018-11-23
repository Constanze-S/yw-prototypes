package org.yesworkflow.annotations;

import org.yesworkflow.YWKeywords;
import org.yesworkflow.YWKeywords.Tag;

public class Loc extends Annotation {

	public Loc(Long id, Long sourceId, Long lineNumber, String comment) throws Exception {
		super(id, sourceId, lineNumber, comment, YWKeywords.Tag.LOC);
	}

	public Loc(Long id, Long sourceId, Long lineNumber, String comment, Tag tag) throws Exception {
		super(id, sourceId, lineNumber, comment, tag);
	}

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
