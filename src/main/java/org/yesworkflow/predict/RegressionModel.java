package org.yesworkflow.predict;

public enum RegressionModel {

	LIN_REG, POLY_REG3, POLY_REG4, POLY_REG5;

	public static RegressionModel toRegressionModel(Object rm) throws Exception {

		if (rm instanceof String) {
			String rmstring = (String) rm;
			if (rmstring.equalsIgnoreCase("linear"))
				return RegressionModel.LIN_REG;
			if (rmstring.equalsIgnoreCase("poly3"))
				return RegressionModel.POLY_REG3;
			if (rmstring.equalsIgnoreCase("poly4"))
				return RegressionModel.POLY_REG4;
			if (rmstring.equalsIgnoreCase("poly5"))
				return RegressionModel.POLY_REG5;
		}

		throw new Exception("Unrecognized regression model: " + rm);
	}
}