#!/usr/bin/env python 
# -*- coding: utf-8 -*-
import numpy as np
from sklearn.metrics import median_absolute_error
import sklearn.linear_model as lm
import csv
import os
import matplotlib.pyplot as plt
from sklearn.preprocessing import PolynomialFeatures

# script for evaluating the combination of 4 models (all poly reg degree 5) 

# trains and tests the combination of the models for the datasizes given
def useSuperModel(datasize,cv): 
    foldsize=int(datasize/cv)
  
    # path to the csv-file containing the training data
    # the file must not contain a header
    # in order to obtain meaningful results, the line sequence in the file must be swapped randomly
    with open('C:/Users/Conny/Desktop/MAYesWorkflow/PythonSkripte/test_train_data50_mixed.csv' , 'r') as f:        
        reader = csv.reader(f, delimiter=',')            
        full_list = list(reader)
        fullarray = np.array(full_list)    
        
        i=0        
        scores=[]
        while i<(datasize):    
            
            # set train- and testdata for current fold
            features1 = fullarray[0:i,3:5].astype(float)
            features2 = fullarray[i+foldsize:datasize,3:5].astype(float)           
            features=np.concatenate((features1, features2), axis=0)  
            
            # train model 1
            targets1 = fullarray[0:i,[6]].astype(float)
            targets2 = fullarray[i+foldsize:datasize,[6]].astype(float)
            targets=np.concatenate((targets1, targets2), axis=0)            
            poly_features1 =PolynomialFeatures(5)
            x_transform1=poly_features1.fit_transform(features)
            lr1 = lm.LinearRegression()
            lr1.fit(x_transform1, targets)      
            
            # train model 2
            targets1 = fullarray[0:i,[7]].astype(float)
            targets2 = fullarray[i+foldsize:datasize,[7]].astype(float)
            targets=np.concatenate((targets1, targets2), axis=0)             
            poly_features2 =PolynomialFeatures(5)
            x_transform2=poly_features2.fit_transform(features)
            lr2 = lm.LinearRegression()
            lr2.fit(x_transform2, targets)            
            
            # train model 3
            targets1 = fullarray[0:i,[8]].astype(float)
            targets2 = fullarray[i+foldsize:datasize,[8]].astype(float)
            targets=np.concatenate((targets1, targets2), axis=0)              
            poly_features3 =PolynomialFeatures(5)
            x_transform3=poly_features3.fit_transform(features)
            lr3 = lm.LinearRegression()
            lr3.fit(x_transform3, targets)
            
            # set train- and testdata for model 4
            features1 = fullarray[0:i,6:9].astype(float)
            features2 = fullarray[i+foldsize:datasize,6:9].astype(float)            
            features=np.concatenate((features1, features2), axis=0)
            targets1 = fullarray[0:i,[9]].astype(float)
            targets2 = fullarray[i+foldsize:datasize,[9]].astype(float)
            targets=np.concatenate((targets1, targets2), axis=0)  
            
            # train model 4          
            poly_features4 =PolynomialFeatures(5)
            x_transform4=poly_features4.fit_transform(features)
            lr4 = lm.LinearRegression()
            lr4.fit(x_transform4, targets)
            
            # set features and target for testing the generated supermodel
            features_test = fullarray[i:i+foldsize,3:5].astype(float)
            targets_test = fullarray[i:i+foldsize,[9]].astype(float)            
                
            # combine models and generated predictions
            predictedValues=[]
            for feature in features_test:
                feature_transf = poly_features1.transform(np.array(feature).reshape(1,-1))
                size_of_energies=float(lr1.predict(feature_transf))
                
                feature_transf2 = poly_features2.transform(np.array(feature).reshape(1,-1))
                accepted_sample=float(lr2.predict(feature_transf2))
                
                feature_transf3 = poly_features3.transform(np.array(feature).reshape(1,-1))
                num_images=float(lr3.predict(feature_transf3))
                
                var =[size_of_energies, accepted_sample, num_images]
                var_transf = poly_features4.transform(np.array(var).reshape(1,-1))
                var2=lr4.predict(var_transf)                
                predictedValues.append(var2[0])
                           
            # determine the error
            nmae=median_absolute_error(targets_test[:,0], predictedValues)
            if nmae>0:                
                nmae=-1*nmae                
            
            i=i+foldsize
            scores.append(nmae)
            
        print("Fehlerwerte der 5 Modelle:")
        # print("errors of the 5 models:")
        print(scores)
        
        # determine mean value of the errors
        median_error=0
        for value in scores:
            median_error+=value
        median_error=median_error/cv       
        
    return median_error
    
# number of records to evaluate
dataSizeArray = [10, 15, 20, 25, 30, 35, 40, 45, 50]
errors=[]
for size in dataSizeArray:  
    print("\n###### Verwendete Datensaetze: "+ str(size) + " ######" )  
    # print("\n###### Number of records used: "+ str(size) + " ######" )
    res = useSuperModel(size,5)
    errors.append(res)
    print("Gemittelter Fehlerwert: " + str(res))
    # print("median error: " + str(res))

plt.plot(dataSizeArray,errors,'md',label="Polynomielle Regression vom Grad 5 für alle Modelle")
# plt.plot(dataSizeArray,errors,'md',label="Polynomial Regression of 5th degree (all models)")

# configure plot
ymin=-2000
ymax=20
xmin=-2
xmax=55
label_y="neg. median absolute error"
plt.legend(loc='lower right')
plt.grid(True)
plt.ylabel(label_y)
plt.xlabel("Anzahl der Datensätze")
# plt.xlabel("Number of records")         
plt.autoscale(enable=True, axis='both', tight=None)
plt.xlim(xmin,xmax)
plt.ylim(ymin,ymax)

plt.show()
       
           