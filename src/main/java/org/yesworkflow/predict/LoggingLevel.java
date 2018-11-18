package org.yesworkflow.predict;

public enum LoggingLevel {

	NORMAL, VERBOSE;

	public static LoggingLevel getLoggingLevel(Object ll) throws Exception {

		if (ll instanceof LoggingLevel)
			return (LoggingLevel) ll;

		if (ll instanceof String) {
			String esstring = (String) ll;
			if (esstring.equalsIgnoreCase("on"))
				return LoggingLevel.VERBOSE;
			if (esstring.equalsIgnoreCase("off"))
				return LoggingLevel.NORMAL;
		}

		throw new Exception("Unrecognized logging level: " + ll);
	}
}