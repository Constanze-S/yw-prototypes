
package org.yesworkflow.predict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Class for processing a stream in a thread in order to recognize a certain
 * pattern ("Result=").
 */
public class StreamConsumer extends Thread {
	private InputStream inputStream = null;
	private PrintStream stdoutStream = null;
	@SuppressWarnings("unused")
	private PrintStream stderrStream = null;
	private double result = -1;

	/**
	 * Constructs a StreamConsumer.
	 * 
	 * @param inputStream  The input stream to be processed.
	 * @param stdoutStream The standard output stream.
	 * @param stderrStream The error output stream.
	 */
	public StreamConsumer(final InputStream inputStream, PrintStream stdoutStream, PrintStream stderrStream) {
		this.inputStream = inputStream;
		this.stdoutStream = stdoutStream;
		this.stderrStream = stderrStream;
	}

	/**
	 * This method searches the stream for a specific pattern ("Result=").
	 */
	@Override
	public void run() {
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				if (DefaultPredict.VERBOSE) {
					stdoutStream.println(line);
				}
				if (line.startsWith("Result=")) {
					result = Double.valueOf(line.replace("Result=", ""));
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Getter for the result (prediction).
	 * 
	 * @return The result (predicted value).
	 */
	public Double getResult() {
		return result;
	}

	/**
	 * Setter for setting the result (prediction).
	 * 
	 * @param result The predicted value that is to be set.
	 */
	public void setResult(int result) {
		this.result = result;
	}
}