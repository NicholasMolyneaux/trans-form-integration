package ch.epfl.transpor.transform.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.transpor.transform.model.AppProperties;
import ch.epfl.transpor.transform.model.ModelType;
import ch.epfl.transpor.transform.model.Sequence;
import ch.epfl.transpor.transform.model.exceptions.ModelFailedException;
import ch.epfl.transpor.transform.model.exceptions.RunModelException;

public class ModelRunner {
	
	public void runModel(Sequence seq, int step, Date simTime, AppProperties appProps) throws RunModelException, InterruptedException, IOException, ModelFailedException {
		
		DataHandler dh = new DataHandler();
		
		ArrayList<String> cmdArgs = new ArrayList<String>();
		File workingDir = null;
		File f = null;
		
		ModelType mt = seq.steps.get(step);
		
		switch(mt) {
		case HUB:
			for (int i=step-1;i>=0;i--) {
				if(seq.steps.get(i) == ModelType.URBAN) {
					dh.xfrmUrbanToHubFiles(appProps);
					break;
				}	
				else if(seq.steps.get(i) == ModelType.HUB)
					break;
			}
			
			cmdArgs.add("java");
			
			//this will stop hub model execution when debugging
			//to save time...
			if(appProps.debug)
				return;
			
			f = new File(appProps.getHubCfgFile());
			workingDir = f.getParentFile();
			cmdArgs.add("-jar");
			cmdArgs.add(appProps.getHubSolver());
			cmdArgs.add("--conf");
			cmdArgs.add(f.getName());
			break;
			
		case URBAN:
			
			//transform hub data if not done earlier
			for (int i=step-1;i>=0;i--) {
				if(seq.steps.get(i) == ModelType.HUB) {
					dh.transformHubToUrban(simTime, appProps);
					break;
				}	
				else if(seq.steps.get(i) == ModelType.URBAN)
					break;
			}
			
			//transform regional data if not done before
			for (int i=step-1;i>=0;i--) {
				if(seq.steps.get(i) == ModelType.REGIONAL) {
					dh.transformRegionalToUrban(simTime, appProps);
					break;
				}	
				else if(seq.steps.get(i) == ModelType.URBAN)
					break;
			}

			if(System.getProperty("os.name").startsWith("Mac"))
				cmdArgs.add(appProps.getUrbanSolver() + "/Contents/MacOS/mezzo_s");
			else 
				cmdArgs.add(appProps.getUrbanSolver());
			f = new File(appProps.getUrbanSolver());
			workingDir = f.getParentFile();
			f = new File(appProps.getBMMasterFile());
			cmdArgs.add(f.getName());
			break;
			
		case REGIONAL:
			cmdArgs.add("java");
			cmdArgs.add("-jar");
			cmdArgs.add(appProps.getRegionalSolver());
			f = new File(appProps.getRegionalSolver());
			workingDir = f.getParentFile();
			
			cmdArgs.add(appProps.getRegInput());
			cmdArgs.add(appProps.getRegOutput());
			break;
		}
		
		String[] cmdStr = new String[cmdArgs.size()]; 
		cmdArgs.toArray(cmdStr);
		ProcessBuilder pb = new ProcessBuilder(cmdStr);
		pb.redirectErrorStream(true);
		if(workingDir != null)
			pb.directory(workingDir);
		Process p = pb.start();
		// Then retreive the process output
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()), 10000);
		
		String line;
		StringBuilder errMsg = new StringBuilder();
		
		while(p.isAlive()){
			while ((line = in.readLine()) != null) {
			    errMsg.append(line + "\n");
			}
			Thread.sleep(5000);
		}
		
		if(p.exitValue() != 0) {
			System.out.println(errMsg.toString());
			throw new ModelFailedException("Model execution error. See stdout for details.");
					
		}
	
	}
	
	public void runSequence(Sequence seq, Date simTime) throws RunModelException, InterruptedException, IOException, ModelFailedException {
		
		for(int i=0;i<seq.steps.size();i++) {
			System.out.println("Started model: " + seq.steps.get(i));
			runModel(seq, i, simTime, seq.appProps != null ? seq.appProps:AppProperties.getDefaultProps());
			System.out.println("Finished model: " + seq.steps.get(i));
		}	
		
	}

}
