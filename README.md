# trans-form-integration-tool
This repository contains the tool for integrating the three different models used in the european project "TRANS-FORM". It should help the users of these models to transform the data from the different formats and also allow the sequenetial execution of the models.
It relies on the data and programs stored in the "integration_data" folder. There is the data for the common case study in The Hague. As the executable for one of the models is not cross-platform, this repo can only be used as-is on *Windows* !

## Compilation
The java code can be compiled using any IDE like eclipse or intellij. The main function is located in the view/MainWindow.java. By running this program, the UI will appear.
There are project files for eclipse inside the TransFormInt directory which should enable an easy opening of the project inside eclipse.

## Settings
The most import settings which must be correctly set are the paths to the individual models. This can be done by going to File -> Settings. Here the paths to the idividual executable must be provided. By programs we mean the file which should be executed by the tool in order to simulate the different levels.
For the hub model, this is a JAR file, for BusMezzo it is a BATCH file, for the Regional model is again a JAR. These are located in the integration_data directory.

## Model configuration
Each model requires a given set of input data. The way this should be configured is explained for each model.
### Hub model
All parameters and input data is passed to the hub model via a config file. The format is HOCON ( https://github.com/lightbend/config/blob/master/HOCON.md ) which is a simplified version of JSON. This file must be provided to the hub model as a command lnie argument with the key "--conf".
Therefeore, all which should be done to be able to run the hub model using the UI, is to specify the path to this config file in the main window under the seciton "Hub model". 
### BusMezzo
Since multiple runs of BM must be performed, we pass a BATCH file to BM containg the main config file and the number of iterations to perform. For example, the script which is usually used for running BM is called "batch_10.bat" and is located in the data folder for BusMezzo.
For this model, all files must be stored in the same folder. This data set to use is in the folder "AM 190213" inside the BM folder of the integration data.
### Regional model
The execution of the regional model relies on a JAR as well. The configuration for this model requires the path to the folder containing the input data and also the folder to the outpput directory.

## Running the simulations
Before event attempting to run the models, two things must be done:
- unzip the file "path_set_generation.tar.gz" in the folder case-study/BM/. The file inside should be named "path_set_generation.dat"
- download and install the gurobi solver. The regional model was built against version 6.5.1. Replace the file "gurobi.jar.placeholder" with the functional "gurobi.jar".

Each model can be run individually to test the configuration. In order to run different models in sequence, a specific sequence must be configured. This can be done using the sequence editor File -> Edit Sequences.
Here one can create, edit or delete any sequence of models. The possibility to provide specfic configuration files for each step of the sequence exists. This can be useful if some simulations have been done separately and the tool should only transform the data between the different models.
To tranform the data between the regional model and the bus mzzo (urban model), a reference time must be provided. For the case study contained in this repo, this time must be set to 6am. This is done my default, so nothing actually needs changing. 

