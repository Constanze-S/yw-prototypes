package org.yesworkflow.predict;

/**
 * An enum type for the logging level.
 */
public enum LoggingLevel {

	NORMAL, VERBOSE;

	/**
	 * This method returns the corresponding logging level for a given input. An
	 * exception is thrown if the logging level is unrecognized.
	 * 
	 * @param ll The input variable that is to be mapped to a constant (logging
	 *           level).
	 * @return The corresponding logging level.
	 * @throws Exception If the logging level is unknown an exception will be
	 *                   thrown.
	 */
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