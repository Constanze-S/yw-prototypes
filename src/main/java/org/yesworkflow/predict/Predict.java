package org.yesworkflow.predict;

import java.util.Map;

import org.yesworkflow.YWStage;
import org.yesworkflow.config.Configurable;

public interface Predict extends YWStage, Configurable {
	Predict configure(String key, Object value) throws Exception;

	Predict configure(Map<String, Object> config) throws Exception;

	Predict predict() throws Exception;

	Double getPrediction();
}
