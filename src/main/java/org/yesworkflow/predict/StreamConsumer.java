
package org.yesworkflow.predict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class StreamConsumer extends Thread {
	private InputStream inputStream = null;
	private PrintStream stdoutStream = null;
	@SuppressWarnings("unused")
	private PrintStream stderrStream = null;
	private double result = -1;

	public StreamConsumer(final InputStream inputStream, PrintStream stdoutStream, PrintStream stderrStream) {
		this.inputStream = inputStream;
		this.stdoutStream = stdoutStream;
		this.stderrStream = stderrStream;
	}

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

	public Double getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}
}