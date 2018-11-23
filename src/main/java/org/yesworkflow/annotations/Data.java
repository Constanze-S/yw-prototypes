package org.yesworkflow.annotations;

import org.yesworkflow.YWKeywords;

public class Data extends AliasableAnnotation {

	public Data(Long id, Long sourceId, Long lineNumber, String comment) throws Exception {
		super(id, sourceId, lineNumber, comment, YWKeywords.Tag.DATA);
	}

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
