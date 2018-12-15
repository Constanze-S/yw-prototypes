#!/usr/bin/env python 
# -*- coding: utf-8 -*-
import numpy as np
import csv
import itertools
import matplotlib.pyplot as plt
from sklearn import linear_model
from sklearn import metrics
from sklearn.svm import SVR
from sklearn.svm import SVC
from sklearn.cross_validation import cross_val_score
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
from sklearn.datasets import load_iris
from sklearn.preprocessing import PolynomialFeatures
from matplotlib import pyplot as plt, markers
from matplotlib.pyplot import autoscale
from sklearn.linear_model.base import LinearRegression
from sklearn.model_selection import learning_curve
from sklearn.svm import SVC

# settings for plot
ymin=-200
ymax=20
xmin=-2
xmax=55
label_y="neg. median absolute error"


class draw:
    
    def __init__(self, linRegMarker, polyRegMarkers, xdata, linRegData, polyRegData):
        self.__xdata = xdata
        self.__linRegData = linRegData
        self.__polyRegData = polyRegData
        self.__linRegMarker = linRegMarker
        self.__polyRegMarkers = polyRegMarkers
        
    # generate plot showing all results
    def plotAll (self):
        linRegMarker = self.__linRegMarker
        polyRegMarkers = itertools.cycle(self.__polyRegMarkers)       
        x = self.__xdata
        
        for label in self.__linRegData:
            plt.plot(x, self.__linRegData[label], linRegMarker , label=label)
        for label in self.__polyRegData:
            plt.plot(x, self.__polyRegData[label], polyRegMarkers.__next__(), label=label)
            
        plt.legend(loc='lower right')
        plt.grid(True)
        plt.ylabel(label_y)        
        plt.xlabel("Anzahl der Datensätze")        
        plt.autoscale(enable=True, axis='both', tight=None)
        plt.xlim(xmin,xmax)       
        plt.ylim(ymin,ymax)
        plt.show()
        
    # generate plot for linear regression
    def plotLinReg (self):
        x = self.__xdata
        linRegMarker = self.__linRegMarker
        
        for label in self.__linRegData:
            plt.plot(x, self.__linRegData[label], linRegMarker , label=label)
            
        plt.legend(loc='lower right')
        plt.grid(True)
        plt.ylabel(label_y)
        plt.xlabel("Anzahl der Datensätze")        
        plt.autoscale(enable=True, axis='both', tight=None)
        plt.xlim(xmin,xmax)
        plt.ylim(ymin,ymax)
        plt.show()
      
    # generate plots for polynomial regressions
    def plotPolyRegs (self):
        polyRegMarkers = itertools.cycle(self.__polyRegMarkers)       
        x = self.__xdata
        
        for label in self.__polyRegData:
            plt.plot(x, self.__polyRegData[label], polyRegMarkers.__next__(), label=label)            
            plt.legend(loc='lower right')
            plt.grid(True)
            plt.ylabel(label_y)
            plt.xlabel("Anzahl der Datensätze")        
            plt.autoscale(enable=True, axis='both', tight=None)
            plt.xlim(xmin,xmax)
            plt.ylim(ymin,ymax)
            plt.show()
        
 
class data:    

    # reads the file containing the training/testing data
    def __init__(self, dateiname):        
        self.__dateiname = dateiname        
        with open(self.__dateiname, 'r') as f:
            reader = csv.reader(f, delimiter=',')            
            full_list = list(reader)
        fullarray = np.array(full_list)
        
        # Mapping of columns in the used csv-file
        # 0 cassette_id
        # 1 number_of_lines_in_sample_spreadsheet
        # 2 sample_name
        # 3 sample_quality
        # 4 sample_score_cutoff
        # 5 rejected_sample
        # 6 size_of_energies
        # 7 accepted_sample
        # 8 num_images
        # 9 number_of_raw_image-files
        #10 duration_in_sec
        #11 number_of_raw_image_files_whole_spreadsheet
        
        # feature-columns       
        self.__features = fullarray[:, [3,4]].astype(float)
        # print("features: ")
        # print(self.__features)
        
        # target-columns
        self.__targets = fullarray[:, 9].astype(float)
        # print("targets: ")
        # print(self.__targets)
    
    # returns features and targets as string    
    def __str__(self):
        return "Features" + "\n" + str(self.__features) + "\n" + "Targets" + "\n" + str(self.__targets)
    
    # returns features
    def getFeatures(self, num):
        for i in range(0, num):
            print(self.__features[i])
        return self.__features[:num]          

    # returns targets
    def getTargets(self, num):       
        for i in range(0, num):
            print(self.__targets[i])
        return self.__targets[:num]   
   
    # trains a linear regression model and evaluates it using 5-fold cross validation
    def evalLinearRegression(self, dataSize):
        print("\n##################### Lineare Regression mit " + str(dataSize) + " Datensaetzen ########################")
        regressor = linear_model.LinearRegression()        
        scores = cross_val_score(regressor, self.__features[:dataSize], self.__targets[:dataSize], cv=5, scoring='neg_median_absolute_error') 
        print('NMAEs der 5 Modelle: ', scores.tolist())
        print ("Gemittelter Wert (Cross Validation): %0.3f (+/- %0.3f)" % (scores.mean(), scores.std() / 2))
        # show coefficients
        regressor.fit(self.__features[:dataSize], self.__targets[:dataSize])
        print('Koeffizienten: ', regressor.coef_.tolist())
        return scores.mean()
    
    # trains a polynomial regression model of the degree given and evaluates it using 5-fold cross validation
    def evalPolyRegression(self, dataSize, degree):
        print("\n###### Polyonmielle Regression vom Grad " + str(degree) +  " mit "   + str(dataSize) + " Datensaetzen ######")
        poly_features = PolynomialFeatures(degree)  
        X_poly = poly_features.fit_transform(self.__features[:dataSize])
        poly_model = linear_model.LinearRegression()
        scores = cross_val_score(poly_model, X_poly, self.__targets[:dataSize] , cv=5, scoring='neg_median_absolute_error')        
        print (scores.tolist())
        print ("Gemittelter Wert (Cross Validation): %0.3f \n" % (scores.mean()))
        return scores.mean()
    
    # collects the evaluation results
    def collectResults(self, dataSizeArray, degreeArray):   
        
        for i in range(0, len(dataSizeArray)):
            if i == 0:
                resultsLinearRegression = {"Lineare Regression" : []}
                resultsLinearRegression.get("Lineare Regression").append(self.evalLinearRegression(dataSizeArray[i]))
            else:
                resultsLinearRegression.get("Lineare Regression").append(self.evalLinearRegression(dataSizeArray[i]))
            for j in range(0, len(degreeArray)):
                if i == 0 and j == 0:
                    resultsPolyRegression = {"Polynomielle Regression vom Grad " + str(degreeArray[j]) : []}
                    resultsPolyRegression.get("Polynomielle Regression vom Grad " + str(degreeArray[j])).append(self.evalPolyRegression(dataSizeArray[i], degreeArray[j]))                
                elif i == 0:
                    newResultsPolyRegression = {"Polynomielle Regression vom Grad " + str(degreeArray[j]) : []}
                    newResultsPolyRegression.get("Polynomielle Regression vom Grad " + str(degreeArray[j])).append(self.evalPolyRegression(dataSizeArray[i], degreeArray[j]))
                    resultsPolyRegression.update(newResultsPolyRegression)
                else:
                    resultsPolyRegression.get("Polynomielle Regression vom Grad " + str(degreeArray[j])).append(self.evalPolyRegression(dataSizeArray[i], degreeArray[j]))
        
        return (resultsLinearRegression, resultsPolyRegression)     

        
if __name__ == '__main__':
    
    # code for toy example
    # the file must not contain a header
    # in order to obtain meaningful results, the line sequence in the file must be swapped randomly    
    dateiname = 'C:/Users/Conny/Desktop/MAYesWorkflow/PythonSkripte/test_train_data50_mixed.csv'
    dataSizeArray = [10, 15, 20, 25, 30, 35, 40, 45, 50]
    
    # the steps chosen for the evaluation:
    #dataSizeArray = [10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 150, 200, 250, 300, 350, 400, 450, 500, 600, 700, 800, 900, 1000,1250,1500,1750,2000,2250,2500,2750,3000,3500,4000,4500,5000,6000,7000,8000,9000,10000,11000,12000,12465]    
    # for CSV-file without duplicates:
    # dataSizeArray = [10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100,150, 200,250,300,350,400,450,500,550,600,650,700,750,800,850,900,950,1000]
    
    # defines the degrees of the polynomial regressions used
    degreeArray = [3, 4, 5]
    # markers for plot
    linRegMarker = 'kX'
    polyRegMarkers =['go','rP','b^','md']
    
    # get the data
    dataset = data(dateiname)
    # get the evaluation results
    linRegData, polyRegData = dataset.collectResults(dataSizeArray, degreeArray)
    # plot the results    
    plot = draw(linRegMarker,polyRegMarkers,dataSizeArray,linRegData,polyRegData)
    plot.plotAll()
    plot.plotLinReg()
    plot.plotPolyRegs()     
   