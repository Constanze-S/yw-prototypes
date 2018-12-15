This fork refers to the master's thesis, Praediktive Modelle in YesWorfklow.
It contains the code extending the YesWorkflow prototype, as described in the thesis and the scripts used for the evaluation. 
 
Toy example for evaluation:
! Mind paths for read and written files !
1. Use generate_random_sample_spreadsheet_files.py for generating 50 (or more) sample_spreadsheet-files. 
2. Use simulate_data_collection_with_data_and_loc_annotation.py for executing the workflow (generates training/testing data).
	(The column number_of_raw_image_files_whole_spreadsheet must be aggregated from the data (not necessary for the test described here). For using this, like described in the thesis, lines that represent duplicates must also be removed.)
3. Remove the header of test_train_data50.csv and swap line order randomly (e.g. using Microsoft Excel) -> rename file: test_train_data50_mixed.csv
4. Run Evaluationsskript1_bel_viele_Bloecke.py to evaluate prediction for any number of blocks.
5. Run Evaluationsskript2_drei_Bloecke.py to perform the evaluation with combined models for the case considered in the master's thesis.

Use of the extended YW-prototype:
- ! Mind the paths !
	- Especially path to the Python script (default_regression_script.py) executing the prediction! It refers to test_train_data50_mixed_with_header.csv as training data, so that a small amount of training data is already available. 
- Example YW-command: predict C:/<YOUR PATH>/simulate_data_collection_with_data_and_loc_annotation.py -c predict.input=(number_of_lines_in_sample_spreadsheet,30);(sample_score_cutoff,5);(size_of_energies,4) -c predict.output=number_of_raw_image-files -c predict.model=poly3 -c predict.verbose=on -c predict.python=C:/<YOUR PATH>/python
	1. Searches the script C:/<YOUR PATH>/simulate_data_collection_with_data_and_loc_annotation.py for the contained LOC-annotation
	2. Uses the training data, which is specified by the LOC-annotation, for training a polynomial regression model of 3rd degree
	3. Uses this model to predict the target (number_of_raw_image-files) for the given features (number_of_lines_in_sample_spreadsheet = 30 and sample_score_cutoff = 5)
	4. Returns the prediction to the console. 
	! Using only few training data (like test_train_data50_mixed_with_header.csv) the result may be poor ! 