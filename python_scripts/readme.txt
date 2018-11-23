Mini-Beispiel (Evaluation):

Pfade zu den erstellten und gelesenen Dateien beachten.

1. Mit generate_random_sample_spreadsheet_files.py 50 (oder beliebig viele) sample_spreadsheet-Dateien generieren.
2. Mit simulate_data_collection_with_data_and_loc_annotation.py Workflow ausfuehren und so Trainingsdaten erstellen.
	(Die Spalte number_of_raw_image_files_whole_spreadsheet muss aus den Daten aggregiert werden (nicht notwendig fuer den hier beschriebenen Test). Dazu muessen dann auch Zeilen, die Duplikate darstellen, entfernt werden.)
3. Header aus der test_train_data50.csv entfernen und Zeilenfolge zufällig vertauschen (z.B. mit Excel)
	Datei umbenennen: test_train_data50_mixed.csv
4. Evaluationsskript1_bel_viele_Bloecke.py ausführen, um Vorhersage ueber beliebig viele Bloecke zu evaluieren.
5. Evaluationsskript2_drei_Bloecke.py ausführen, um fuer den in der Arbeit betrachteten Fall die Evaluation mit kombinierten Modellen auszuführen.