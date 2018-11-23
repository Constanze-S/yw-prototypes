package org.yesworkflow.predict;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		// check for valid 'predict.input' parameter
		if (key.equalsIgnoreCase("input")) {
			if (((String) value).matches("\\([A-Za-z0-9_-]+,[0-9]+(.[0-9]+){0,1}\\)(;\\([A-Za-z0-9_-]+,[0-9]+(.[0-9]+){0,1}\\))*")) {
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
		// 'predict.input' and 'predict.output' must be set
		if (input == null || output == null) {
			throw new Exception("Parameters 'input' and 'output' must be set");
		}
		// only one source file is allowed
		if (ywdb.getRowCount(Table.SOURCE) > 1) {
			throw new Exception("Multiple source files are not supported");
		}
		// only one '@LOC' annotation is allowed
		if (ywdb.jooq().selectCount().from(Table.ANNOTATION).where(Column.TAG.eq(Tag.LOC.toString())).fetchOne(0, int.class) != 1) {
			throw new Exception("There must be one 'LOC' annotation. Multiple 'LOC' annotations are not supported");
		}

		// check if there is a '@IN', '@OUT' or '@PARAM' for each '@DATA' annotation
		if (ywdb.jooq().selectCount().from(Table.ANNOTATION)
				.where(Column.VALUE
						.notIn(ywdb.jooq().select(Column.VALUE).from(Table.ANNOTATION)
								.where(Column.TAG.eq(Tag.IN.toString()).or(Column.TAG.eq(Tag.OUT.toString()).or(Column.TAG.eq(Tag.PARAM.toString())))))
						.and(Column.TAG.eq(Tag.DATA.toString())))
				.fetchOne(0, int.class) > 0) {
			throw new Exception("One or more 'DATA' annotations without corresponding 'IN', 'OUT' or 'PARAM' annotation found");
		}

		// add input variable names
		List<String> varNames = new ArrayList<String>(
				Arrays.asList(input.replaceAll(",[0-9]+(.[0-9]+){0,1}\\);\\(", ";").replaceAll("\\(|,[0-9]+(.[0-9]+){0,1}\\)", "").split(";")));
		// add output variable name
		varNames.add(output);
		
		// check if there are '@DATA' annotations for all 'predict.input' and
		// 'predict.output' variables
		for (String varName : varNames) {
			// check for '@DATA' annotation
			if (ywdb.jooq().selectCount().from(Table.ANNOTATION).where(Column.TAG.eq(Tag.DATA.toString()).and(Column.VALUE.eq(varName))).fetchOne(0,
					int.class) == 0) {
				// check for '@AS' annotation
				if (ywdb.jooq().selectCount().from(Table.ANNOTATION)
						.where(Column.TAG.eq(Tag.AS.toString()).and(Column.VALUE.eq(varName))
								.and(Column.QUALIFIES.in(ywdb.jooq().select(Column.ID).from(Table.ANNOTATION).where(Column.TAG.eq(Tag.DATA.toString())))))
						.fetchOne(0, int.class) == 0) {
					throw new Exception("No 'DATA' annoation for '" + varName + "' found");
				}
			}
		}

		// check if there is a belonging '@URI' annotation
		Result<Record> rows = ywdb.jooq().select(Column.ID, Column.VALUE).from(Table.ANNOTATION)
				.where(Column.QUALIFIES.eq(ywdb.jooq().select(Column.ID).from(Table.ANNOTATION).where(Column.TAG.eq(Tag.LOC.toString())))).fetch();

		if (rows.size() != 1) {
			throw new Exception("No 'URI' annotation for 'LOC' found");
		}

		File csvData = new File(rows.get(0).getValue(Column.VALUE).toString().replace("file:", ""));
		if (!csvData.exists()) {
			throw new Exception("CSV-file '" + csvData.getAbsolutePath() + "' not found");
		}

		try {
			// build cmd command
			StringBuilder builder = new StringBuilder();
			if (pythonExecutable != null) {
				try {
					Path path = Paths.get(pythonExecutable);
					if (!path.isAbsolute()) {
						throw new Exception("Path to Python executable is not absolut");
					}
					builder.append(path);
					if (!path.endsWith(System.getProperty("file.separator"))) {
						builder.append(System.getProperty("file.separator"));
					}
				} catch (InvalidPathException ex) {
					throw new Exception("Path to Python executable could not be processed");
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

			// execute cmd command
			ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/C", builder.toString());
			processBuilder.directory(new File(System.getProperty("user.dir")));
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			StreamConsumer streamConsumer = new StreamConsumer(process.getInputStream(), this.stdoutStream, this.stderrStream);
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
						stdoutStream.print("Result=" + prediction);
					} else {
						stdoutStream.print("No prediction available");
					}
				} else if (process.exitValue() == 1) {
					throw new Exception("An error occurred while executing the script");
				} else if (process.exitValue() == 3) {
					throw new Exception("No column for one or more input variable names found");
				} else if (process.exitValue() == 4) {
					throw new Exception("No column for output variable name found");
				} else if (process.exitValue() == 5) {
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
