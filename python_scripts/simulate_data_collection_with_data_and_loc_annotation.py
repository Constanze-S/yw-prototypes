import csv
import sys
import math
import os
import time
from datetime import datetime
from numpy.random import sample
from random import randint 
import re

'''
YW-annotated Python script for data collection from protein crystal samples. 
This version contains LOC and DATA annotations, as described in the master's thesis, Praediktive Modelle in YesWorkflow.
Executing the script creates a csv-file containing the training data, as described in the thesis. 
If the column number_of_raw_image_files_whole_spreadsheet is needed, it has to be aggregated from the directly collected data (for example with Microsoft Excel).
The column duration_in_s is added only for evaluation purpose. Therefore no corresponding DATA-annotation exists. 
 
This code is basically taken from:
https://github.com/yesworkflow-org/yw-tapp-15-recon/blob/master/example/simulate_data_collection.py
All block comments have been converted to line comments. 
To reduce the runtime the creation of the log files is commented out.
Further changes to the original version are marked in the comments.
'''

# @begin simulate_data_collection

# added LOC annotation:
# @loc Trainingsdaten @uri file:python_scripts/test_train_data50_mixed_with_header.csv

# @param cassette_id 

# added DATA annotation:
# @data cassette_id 

# @param sample_score_cutoff

# added DATA annotation:
# @data sample_score_cutoff

# @in sample_spreadsheet @uri file:cassette_{cassette_id}_spreadsheet.csv

# added DATA annotation:
# @data sample_spreadsheet @as number_of_lines_in_sample_spreadsheet

# @in calibration_image  @uri file:calibration.img 
# @out corrected_image   @uri file:run/data/{}/{}_{}eV_{}.img
# @out run_log           @uri file:run/run_log.txt
# @out collection_log    @uri file:run/collected_images.csv
# @out rejection_log     @uri file:run/rejected_samples.txt


def simulate_data_collection(cassette_id, sample_score_cutoff):

    # train_data_file added for collecting additional information
    # usually path has to match with path in the LOC annotation
    # this was ignored at this point for a simpler testing of both examples, that are described in the readme.txt 
    with open('C:/Users/Conny/Desktop/MAYesWorkflow/PythonSkripte/test_train_data50.csv', 'a', newline='') as train_data_file:
        train_data=csv.writer(train_data_file)
        if (os.stat('C:/Users/Conny/Desktop/MAYesWorkflow/PythonSkripte/test_train_data50.csv').st_size == 0):
            train_data.writerow(["cassette_id","number_of_lines_in_sample_spreadsheet","sample_name","sample_quality","sample_score_cutoff","rejected_sample","size_of_energies","accepted_sample","num_images","number_of_raw_image-files","duration_in_s"])

    
        # @begin initialize_run
        # @out run_log  @uri file:run/run_log.txt
        
        if not os.path.exists('run'):
            os.makedirs('run')

        with run_logger(log_file_name="run/run_log.txt") as run_log:
            # run_log.write("Processing samples in cassette " + cassette_id)
            # run_log.write("Sample quality cutoff:" + str(sample_score_cutoff))
            
            # @end initialize_run
            
                 
            # @begin load_screening_results
            # @param cassette_id
            # @in sample_spreadsheet  @uri file:cassette_{cassette_id}_spreadsheet.csv
            # @out sample_name 
            
            # added DATA annotation:
            # @data sample_name 
            
            # @out sample_quality
            
            # added DATA annotation:
            # @data sample_quality
            
            # path to sample_spreadsheet-files adjusted
            sample_spreadsheet = '50sample_spreadsheets/cassette_{0}_spreadsheet.csv'.format(cassette_id)
            for sample_name, sample_quality in spreadsheet_rows(sample_spreadsheet):
                start_sample = time.time()
                rawimage_counter = 0
                # run_log.write("Sample {0} had score of {1}".format(sample_name, sample_quality))
                 
                # @end load_screening_results 
                

                
                # @begin calculate_strategy
                # @param sample_score_cutoff
                # @in sample_name 
                # @in sample_quality
                # @out accepted_sample 
                
                # added DATA annotation:
                # @data accepted_sample
                
                # @out rejected_sample 
                
                # added DATA annotation:
                # @data rejected_sample
                
                
                # @out num_images

                # added DATA annotation:
                # @data num_images
                
                # @out energies
                
                # added DATA annotation:
                # @data energies @as size_of_energies
                
                if sample_quality >= sample_score_cutoff: 
                    accepted_sample = sample_name
                    rejected_sample = None
                    num_images = sample_quality + 2
                    # cast to int necessary for newer version
                    energies = [10000, 11000, 12000, 13000, 14000][:int(sample_quality/sample_score_cutoff)]
                    acc_sample = 1
                    rej_sample = 0
                else:
                    accepted_sample = None
                    rejected_sample = sample_name            
                    acc_sample = 0
                    rej_sample = 1  
                # @end calculate_strategy 
                
        
                
                # @begin log_rejected_sample 
                # @param cassette_id 
                # @in rejected_sample
                # @out rejection_log @uri file:/run/rejected_samples.txt
                
                if (rejected_sample is not None):
                    # run_log.write("Rejected sample {0}".format(rejected_sample))
                    with open('run/rejected_samples.txt', 'at') as rejection_log:
                        rejection_log.write("Rejected sample {0} in cassette {1}\n"
                                            .format(rejected_sample, cassette_id))
                    
                    end_sample=time.time()
                    train_data.writerow([cassette_id,float((sum(1 for line in open(sample_spreadsheet))-1)),sample_name,float(sample_quality),float(sample_score_cutoff),float(rej_sample),float(0),float(acc_sample),float(0),float(rawimage_counter),float(end_sample-start_sample)])
                    continue
                 
                # @end log_rejected_sample 
                

                 
                # @begin collect_data_set
                # @call collect_next_image
                # @param cassette_id
                # @in accepted_sample
                # @param num_images
                # @param energies
                # @out sample_id
                # @out energy 
                # out frame_number
                # out raw_image_path @as raw_image 
                     # @uri file:run/raw/{cassette_id}/{sample_id}/e{energy}/image_{frame_number}.raw

                # added DATA annotation:
                # @data raw_image_path @as number_of_raw_image-files
                
                # run_log.write("Collecting data set for sample {0}".format(accepted_sample))
                sample_id = accepted_sample
                for energy, frame_number, intensity, raw_image_path in collect_next_image(
                    cassette_id, sample_id, num_images, energies, 
                    'run/raw/{cassette_id}/{sample_id}/e{energy}/image_{frame_number:03d}.raw'):                
                    # run_log.write("Collecting image {0}".format(raw_image_path))                
                    rawimage_counter = rawimage_counter + 1 
                    # @end collect_data_set 
                    
        
                    
                    # @begin transform_images
                    # @call transform_image
                    # @in sample_id 
                    # @in energy 
                    # @in frame_number
                    # @in raw_image_path @as raw_image
                    # @in calibration_image @uri file:calibration.img
                    # @out corrected_image  @uri file:data/{sample_id}/{sample_id}_{energy}eV_{frame_number}.img
                    # @out corrected_image_path
                    # @out total_intensity
                    # @out pixel_count
                    
                    corrected_image_path = 'run/data/{0}/{0}_{1}eV_{2:03d}.img'.format(
                                            sample_id, energy, frame_number)
                    (total_intensity, pixel_count) = transform_image(
                                                        raw_image_path, corrected_image_path, 'calibration.img')
                    
                    # run_log.write("Wrote transformed image {0}".format(corrected_image_path))
                     
                    # @end transform_images
                    
                    
                    
                    # @begin log_average_image_intensity
                    # @param cassette_id 
                    # @param sample_id 
                    # @param frame_number
                    # @in total_intensity
                    # @in pixel_count
                    # @in corrected_image_path
                    # @out collection_log @uri file:run/collected_images.csv
                    
                    average_intensity = total_intensity / pixel_count
                    with open('run/collected_images.csv', 'at') as collection_log_file:            
                        collection_log = csv.writer(collection_log_file)
                        collection_log.writerow([cassette_id, sample_id, energy, 
                                            average_intensity, corrected_image_path ])
                     
                    # @end log_average_image_intensity 
                
                end_sample=time.time()
                train_data.writerow([cassette_id,float((sum(1 for line in open(sample_spreadsheet))-1)),sample_name,float(sample_quality),float(sample_score_cutoff),float(rej_sample),float(len(energies)),float(acc_sample),float(num_images),float(rawimage_counter),float(end_sample-start_sample)])
    
    # @end simulate_data_collection    
    


# @begin collect_next_image
# @param cassette_id
# @param sample_id
# @param num_images
# @param energies
# @param image_path_template
# @return energy
# @return frame_number
# @return intensity
# @return raw_image_path

def collect_next_image(cassette_id, sample_id, num_images, energies, image_path_template):
    for energy in energies:
        for frame_number in range(1, num_images + 1):
            raw_image_path = image_path_template.format(
                        cassette_id=cassette_id, sample_id=sample_id, 
                        energy=energy, frame_number=frame_number)
            with new_image_file(raw_image_path) as raw_image:
                intensity = int((energy / (frame_number + 1)) % math.sqrt(energy))
                raw_image.write_values(10 * [intensity])
            yield energy, frame_number, intensity, raw_image_path

# @end collect_next_image



# @begin transform_image
# @param raw_image_path
# @param corrected_image_path
# @param calibration_image_path
# @return total_intensity
# @return pixel_count

def transform_image(raw_image_path, corrected_image_path, calibration_image_path):

    with open(raw_image_path, 'rt') as raw_image, \
         open(calibration_image_path, 'rt') as calibration_image, \
         new_image_file(corrected_image_path) as corrected_image:

        pixel_count = 0
        total_intensity = 0
        for line in raw_image:
            raw_value = int(line)
            correction = int(calibration_image.readline())
            adjusted_value = raw_value - correction
            corrected_value = adjusted_value if adjusted_value >= 0 else 0
            corrected_image.write(corrected_value)
            total_intensity += corrected_value
            pixel_count += 1

    return total_intensity, pixel_count
 
# @end transform_image 


def spreadsheet_rows(spreadsheet_file_name):
    with open(spreadsheet_file_name, 'rt') as screening_results:
        sample_results = csv.DictReader(screening_results)
        for sample in sample_results:
            yield sample['id'], int(sample['score'])

class run_logger:
    
    def __init__(self, terminal=sys.stderr, log_file_name=None):
        self.log_file = open(log_file_name, 'wt') if log_file_name is not None else None
        self.terminal = terminal
        
    def __enter__(self):
        return self
        
    def write(self, message):
        current_time = time.time()
        timestamp = datetime.fromtimestamp(current_time).strftime('%Y-%m-%d %H:%M:%S')
        log_message = "{0} {1}\n".format(timestamp, message)
        for log in (self.log_file, self.terminal):
            if (log is not None):
                log.write(log_message)

    def __exit__(self, type, value, traceback):
        if self.log_file is not None:
            self.log_file.close()

class new_image_file:
    
    def __init__(self, image_path):
        image_dir = os.path.dirname(image_path)
        if not os.path.isdir(image_dir):
            os.makedirs(image_dir)
        self.image_file = open(image_path, 'wt')
        
    def __enter__(self):
        return self
        
    def write(self, value):
        self.image_file.write(str(value))
        self.image_file.write('\n')    

    def write_values(self, values):
        for value in values:
            self.write(value)   

    def name(self):
        return self.image_file.name
        
    def __exit__(self, type, value, traceback):
        self.image_file.close()

if __name__ == '__main__':
    
    '''
    # original version:
    cassette_id = 'q55'
    sample_score_cutoff = 12
    simulate_data_collection(cassette_id, sample_score_cutoff)
    '''

    # Debug-Output
    debug = True
    
    # runs over all random generated spreadsheet-files 
    for file in (os.listdir('C:/Users/Conny/Desktop/MAYesWorkflow/PythonSkripte/50sample_spreadsheets')):    
        cassette_id = re.search('q\d+', file).group()        
        sample_score_cutoff = randint(1,50) 
        
        # runtime logging - only for evaluation purpose
        start_spreadsheet = time.time()
        
        if debug:
            print("cassette_id: "+ cassette_id + " sample_score_cutoff: "+ str(sample_score_cutoff))
        
        simulate_data_collection(cassette_id, sample_score_cutoff)
        
        end_spreadsheet = time.time()        
        with open('C:/Users/Conny/Desktop/MAYesWorkflow/PythonSkripte/50sample_spreadsheets.csv', 'a', newline='') as time_log_file:
            time_log=csv.writer(time_log_file)
            if (os.stat('C:/Users/Conny/Desktop/MAYesWorkflow/PythonSkripte/50sample_spreadsheets.csv').st_size == 0):
                time_log.writerow(["cassette_id","duration_in_s"])
            time_log.writerow([cassette_id,float(end_spreadsheet-start_spreadsheet)])   
    
    if debug:
        print("Done")
    