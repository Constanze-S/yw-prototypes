package org.yesworkflow.predict;

import java.util.Map;

import org.yesworkflow.YWStage;
import org.yesworkflow.config.Configurable;

/**
 * An interface for predicting numerical values.
 */
public interface Predict extends YWStage, Configurable {
	/**
	 * Method for setting a single configuration parameter.
	 * 
	 * @param key   The name of the parameter.
	 * @param value The value of the parameter.
	 * @return An object of type "Predict"
	 * @throws If an error occurs an exception will be thrown.
	 */
	Predict configure(String key, Object value) throws Exception;

	/**
	 * Method for setting multiple configuration parameters.
	 * 
	 * @param config A map with different key/value pairs.
	 * @return An object of type "Predict"
	 * @throws If an error occurs an exception will be thrown.
	 */
	Predict configure(Map<String, Object> config) throws Exception;

	/**
	 * This method executes the prediction of a value.
	 * 
	 * @return An object of type "Predict"
	 * @throws Exception If an error occurs an exception will be thrown.
	 */
	Predict predict() throws Exception;

	/**
	 * This method returns the predicted value.
	 * 
	 * @return The predicted value.
	 */
	Double getPrediction();
}
