import math
from random import randint
import csv
import os

# generates random sample_spreadsheet-files
if __name__ == '__main__':
    # number of sample_spreadsheet files to create
    count=50
    dir='C:/Users/Conny/Desktop/MAYesWorkflow/PythonSkripte/{0}'.format(str(count)+'sample_spreadsheets')    
    os.makedirs(dir)
        
    for j in range(0,count):
        file = str(dir)+'/cassette_q{0}_spreadsheet.csv'.format(j)
        print (file)
        with open(file,'w', newline='') as genData_file:           
            generatedData =csv.writer(genData_file)
            generatedData.writerow(["id","score"])
            lines=randint(2,26)
            for k in range(1,lines):
                id = "DRT" + str(randint(100,999))
                score = randint(1,100)
                generatedData.writerow([id,score])
        