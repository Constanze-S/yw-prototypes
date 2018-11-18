package org.yesworkflow.predict;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jooq.Record;
import org.jooq.Result;
import org.yesworkflow.YWKeywords.Tag;
import org.yesworkflow.db.Column;
import org.yesworkflow.db.Table;
import org.yesworkflow.db.YesWorkflowDB;

public class DefaultPredict implements Predict {

	public static RegressionModel DEFAULT_REGRESSION_MODEL = RegressionModel.LIN_REG;
	public static boolean VERBOSE = false;

	private final static String DEFAULT_REGRESSION_SCRIPT = "default_regression_script.py";
	private final static int DEFAULT_TIMEOUT_IN_SEC = 60;

	private YesWorkflowDB ywdb;
	private PrintStream stdoutStream = null;
	@SuppressWarnings("unused")
	private PrintStream stderrStream = null;
	private Double prediction = null;
	private String input = null;
	private String output = null;
	private String pythonExecutable = null;
	private RegressionModel regressionModel = DEFAULT_REGRESSION_MODEL;

	public DefaultPredict(YesWorkflowDB ywdb, PrintStream stdoutStream, PrintStream stderrStream) {
		this.ywdb = ywdb;
		this.stdoutStream = stdoutStream;
		this.stderrStream = stderrStream;
	}

	@Override
	public DefaultPredict configure(String key, Object value) throws Exception {
		if (key.equalsIgnoreCase("input")) {
			if (((String) value)
					.matches("\\([A-Za-z0-9_]+,[0-9]+(.[0-9]+){0,1}\\)(;\\([A-Za-z0-9_]+,[0-9]+(.[0-9]+){0,1}\\))*")) {
				input = (String) value;
			} else {
				throw new Exception("Parameter 'predict.input' has the wrong format");
			}
		} else if (key.equalsIgnoreCase("output")) {
			output = (String) value;
		} else if (key.equalsIgnoreCase("python")) {
			pythonExecutable = (String) value;
		} else if (key.equalsIgnoreCase("model")) {
			regressionModel = RegressionModel.toRegressionModel(value);
		} else if (key.equalsIgnoreCase("verbose")) {
			if (LoggingLevel.getLoggingLevel(value) == LoggingLevel.VERBOSE) {
				VERBOSE = true;
			} else {
				VERBOSE = false;
			}
		}
		return this;
	}

	@Override
	public DefaultPredict configure(Map<String, Object> config) throws Exception {
		if (config != null) {
			for (Map.Entry<String, Object> entry : config.entrySet()) {
				configure(entry.getKey(), entry.getValue());
			}
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Predict predict() throws Exception {
		if (input == null || output == null) {
			throw new Exception("Parameters 'input' and 'output' must be set");
		}
		if (ywdb.getRowCount(Table.SOURCE) > 1) {
			throw new Exception("Multiple source files are not supported");
		}
		if (ywdb.jooq().selectCount().from(Table.ANNOTATION).where(Column.TAG.eq(Tag.LOC.toString())).fetchOne(0,
				int.class) != 1) {
			throw new Exception("There must be one 'LOC' annotation. Multiple 'LOC' annotations are not supported");
		}

		// add input variable names
		List<String> varNames = new ArrayList<String>(
				Arrays.asList(input.replaceAll(",[0-9]+(.[0-9]+){0,1}\\);\\(", ";")
						.replaceAll("\\(|,[0-9]+(.[0-9]+){0,1}\\)", "").split(";")));
		// add output variable name
		varNames.add(output);
		int count = ywdb.jooq().fetchCount(ywdb.jooq().selectDistinct(Column.VALUE).from(Table.ANNOTATION)
				.where(Column.TAG.eq(Tag.DATA.toString()).and(Column.VALUE.in(varNames))));
		if (count != varNames.size()) {
			throw new Exception("Input or output variables without corresponding 'DATA' annotations found");
		}

		if (ywdb.jooq().selectCount().from(Table.ANNOTATION)
				.where(Column.TAG.eq(Tag.DATA.toString())
						.and(Column.VALUE.notIn(ywdb.jooq().select(Column.VALUE).from(Table.ANNOTATION)
								.where(Column.TAG.eq(Tag.PARAM.toString())).or(Column.TAG.eq(Tag.IN.toString()))
								.or(Column.TAG.eq(Tag.OUT.toString())))))
				.fetchOne(0, int.class) > 0) {
			throw new Exception(
					"One or more 'DATA' annotations without corresponding 'IN', 'OUT' or 'PARAM' annotation found");
		}

		Result<Record> rows = ywdb.jooq().select(Column.ID, Column.VALUE).from(Table.ANNOTATION)
				.where(Column.QUALIFIES.eq(
						ywdb.jooq().select(Column.ID).from(Table.ANNOTATION).where(Column.TAG.eq(Tag.LOC.toString()))))
				.fetch();

		if (rows.size() != 1) {
			throw new Exception("No 'URI' annotation for 'LOC' found");
		}

		File csvData = new File(rows.get(0).getValue(Column.VALUE).toString().replace("file:", ""));
		if (!csvData.exists()) {
			throw new Exception("CSV-file '" + csvData.getAbsolutePath() + "' not found");
		}

		try {
			StringBuilder builder = new StringBuilder();
			if (pythonExecutable != null) {
				builder.append(pythonExecutable);
				if (!pythonExecutable.endsWith(System.getProperty("file.separator"))) {
					builder.append(System.getProperty("file.separator"));
				}
			}
			builder.append("python").append(" ");
			builder.append(DEFAULT_REGRESSION_SCRIPT).append(" ");
			builder.append(input).append(" ");
			builder.append(output).append(" ");
			builder.append(csvData.getAbsolutePath()).append(" ");
			builder.append(regressionModel);

			if (VERBOSE) {
				stdoutStream.print("The following command will be executed: " + builder.toString());
			}

			ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/C", builder.toString());
			processBuilder.directory(new File(System.getProperty("user.dir")));
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			StreamConsumer streamConsumer = new StreamConsumer(process.getInputStream(), this.stdoutStream,
					this.stderrStream);
			streamConsumer.start();

			boolean success = process.waitFor(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);
			if (success) {
				int index = 0;
				while (streamConsumer.isAlive()) {
					index++;
					Thread.sleep(1000);
					if (index == 5) {
						break;
					}
				}
				if (process.exitValue() == 0) {
					if (streamConsumer.getResult() != -1) {
						prediction = streamConsumer.getResult();
						stdoutStream.print("Prediction: " + prediction);
					} else {
						stdoutStream.print("No prediction available");
					}
				} else if (process.exitValue() == 1) {
					throw new Exception("An error occurred while executing the script");
				} else if (process.exitValue() == 2) {
					throw new Exception("No column for one or more input variable names found");
				} else if (process.exitValue() == 3) {
					throw new Exception("No column for output variable name found");
				} else if (process.exitValue() == 4) {
					throw new Exception("Unknown regression model");
				} else {
					throw new Exception("Unhandled exit code " + process.exitValue());
				}

			} else {
				if (process.isAlive()) {
					process.destroyForcibly();
					throw new Exception("Timeout occurred");
				} else {
					process.destroyForcibly();
					throw new Exception("An unexpected error has occurred");
				}
			}
		} catch (InterruptedException ex) {
			throw new Exception("An unexpected error has occurred");
		} catch (IOException ex) {
			throw new Exception("An unexpected error has occurred");
		}
		return this;
	}

	@Override
	public Double getPrediction() {
		return prediction;
	}

}
