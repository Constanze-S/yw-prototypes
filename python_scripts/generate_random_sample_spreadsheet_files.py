import math
from random import randint
import csv
import os

# erstellt sample_spreadsheet-Datein mit Zufallszahlen
if __name__ == '__main__':
    # Anzahl der sample_spreadsheet-Dateien, die erstellt werden sollen
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
        