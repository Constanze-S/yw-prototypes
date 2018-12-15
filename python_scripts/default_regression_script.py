import sys
import numpy as np
from sklearn import linear_model
from sklearn.preprocessing import PolynomialFeatures
from sklearn.linear_model.base import LinearRegression
import csv

# Script for use with the extended YW-prototype
# as described in the master's thesis, Praediktive Modelle in YesWorkflow.
# Trains the requested model and returns the prediction.

# exitcodes: 
# 3=column for input variable not found
# 4=column for output variable not found
# 5=unknown regression model

class data:

    def __init__(self, my_file, input, output):
        # read training data
        self.__my_file = my_file
        with open(self.__my_file, 'r') as f:
            header = next(f).split(",")
            print(str(header))
            reader = csv.reader(f, delimiter=',')  
            full_list = list(reader)
        fullarray = np.array(full_list)

        # get features
        input_array=input.split(";")
        selected_columns=[]
        self.__input_values=[]
        for i in range(0, len(input_array)):
            var=input_array[i][1:-1].split(",")
            if var[0] in header:
                selected_columns.append(header.index(var[0])) 
                self.__input_values.append(float(var[1]))
            else:
                print("Column not found:" + var[0])
                sys.exit(3)
        print("selected input columns:")
        print(selected_columns)
        print("input_values:")
        print(self.__input_values)
        self.__features = fullarray[:, selected_columns].astype(float)
        print("features: ")
        print(self.__features)

        # get targets
        if output in header:
            selected_output_column=header.index(output)
        else:
            print("Column not found:" + output)
            sys.exit(4)
        print("selected output column:")
        print(selected_output_column)
        self.__targets = fullarray[:, selected_output_column].astype(float)
        print("targets: ")
        print(self.__targets)
    
    # returns features and targets as string
    def __str__(self):
        return "Features" + "\n" + str(self.__features) + "\n" + "Targets" + "\n" + str(self.__targets)

    # returns features
    def getFeatures(self, num):
        print("getFeatures")
        for i in range(0, num):
            print(self.__features[i])
        return self.__features[:num]         
    
    # returns targets
    def getTargets(self, num):
        print("getTargets")     
        for i in range(0, num):
            print(self.__targets[i])
        return self.__targets[:num]          

    # trains a linear regression model for the required feature(s) and target(s)
    # returns a prediction for the given input
    def doLinearRegression(self):
        dataSize=len(self.__features)
        print("\n\n##################### Linear Regression mit " + str(dataSize) + " Datensaetzen ########################")     
        lin_reg = linear_model.LinearRegression()
        lin_reg.fit(self.__features[:dataSize], self.__targets[:dataSize])
        return lin_reg.predict([self.__input_values])

    # trains a polynomial regression model of the degree given for the required feature(s) and target(s)
    # returns a prediction for the given input
    def doPolyRegression(self, degree):
        dataSize=len(self.__features)
        print("\n\n##################### Polynomielle Regression Grad" + str(degree) + " mit " + str(dataSize) + " Datensaetzen ########################")

        poly_features = PolynomialFeatures(degree)
        X_transform = poly_features.fit_transform(self.__features[:dataSize])
        poly_model = linear_model.LinearRegression() 
        poly_model.fit(X_transform, self.__targets[:dataSize])

        X_test=np.array(self.__input_values).reshape(1, -1)
        X_test_transform = poly_features.fit_transform(X_test)
        return (poly_model.predict(X_test_transform))
 
    # calls regression of required type
    def do_Regression(self, regression_model):
        result = -1
        if regression_model == "LIN_REG":
            result=self.doLinearRegression()
        elif regression_model == "POLY_REG3":
            result=self.doPolyRegression(3)   
        elif regression_model == "POLY_REG4":
            result=self.doPolyRegression(4)
        elif regression_model == "POLY_REG5":
            result=self.doPolyRegression(5)
        else:
            sys.exit(5)
        return result

if __name__ == '__main__':
    #Argument 1: input variables
    #Argument 2: output variable
    #Argument 3: full path to csv
    #Argument 4: regression model
    print ("Arg1" + str(sys.argv[1]))
    print ("Arg2" + str(sys.argv[2]))
    print ("Arg3" + str(sys.argv[3]))
    print ("Arg4" + str(sys.argv[4]))   

    dataset = data(sys.argv[3], sys.argv[1],sys.argv[2])
    print("Result=" + str(dataset.do_Regression(sys.argv[4])[0]))