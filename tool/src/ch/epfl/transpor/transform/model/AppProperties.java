package ch.epfl.transpor.transform.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

public class AppProperties implements Serializable {

	private static final long serialVersionUID = -769100277908173233L;
	protected transient static Properties prop = new Properties();
	protected transient static AppProperties defaultProp = null;
    
    //regional files
    private final String revTimetableFileName = "revisedTimetable.txt";
    protected String regSolver = "";
    protected String regInput = "";
    protected String regOutput = "";
    protected File revRegTimetable = null;
    
    //urban files
    protected String urbanSolver = "";
    protected String urbanMasterFile = "";
    protected File bmFolder = null;
    protected File timetableBM = null;
    protected File passFlowBM = null;
    protected File bmTransitNetwork = null;
    
    //hub files
    protected String hubSolver = "";
    protected String hubConfig = "";
    protected File hubFolder = null;
    protected File timetableHub = null;
    protected File passFlowHub = null;
    protected File hubStopsZones = null;
    protected File pedWalkTimeDist = null;
    
    //app properties
    public boolean debug = false;
    
    public AppProperties() {}
    
    public String getHubSolver() {
		return hubSolver;
	}
	
	public void setHubSolver(String hs) {
		hubSolver = hs;
	}
	
	public String getHubCfgFile() {
		return hubConfig;
	}
	
	public void setHubCfgFile(String rs) {
		hubConfig = rs;
		if(rs != null && !rs.isEmpty())
			readHubCfgFile(rs);
	}
	
	public String getUrbanSolver() {
		return urbanSolver;
	}
	
	public void setUrbanSolver(String us) {
		urbanSolver = us;
	}
	
	public String getBMMasterFile() {
		return urbanMasterFile;
	}
	
	public void setBMMasterFile(String rs) {
		urbanMasterFile = rs;
		if(rs != null && !rs.isEmpty())
			readBMMasterFile(rs);
	}
	
	public String getRegionalSolver() {
		return regSolver;
	}
	
	public void setRegionalSolver(String rs) {
		regSolver = rs;
	}
	
	public String getRegInput() {
		return regInput;
	}
	
	public String getRegOutput() {
		return regOutput;
	}
	
	public void setRegInputOutput(String in, String out) {
		regInput = in;
		regOutput = out;
		if(out != null && !out.isEmpty())
			setRegFiles(out);
	}
			
	private void setRegFiles(String rs) {
		File regOutDir = new File(rs);
		Path p = Paths.get(regOutDir.getAbsolutePath(), revTimetableFileName);
		revRegTimetable = p.toFile();
	}
    
    public File getRevRegTimetable() {
		return revRegTimetable;
	}

	public File getBmFolder() {
		return bmFolder;
	}

	public File getTimetableBM() {
		return timetableBM;
	}

	public File getPassFlowBM() {
		return passFlowBM;
	}

	public File getBmTransitNetwork() {
		return bmTransitNetwork;
	}

	public File getTimetableHub() {
		return timetableHub;
	}

	public File getPassFlowHub() {
		return passFlowHub;
	}

	public File getHubStopsZones() {
		return hubStopsZones;
	}

	public File getPedWalkTimeDist() {
		return pedWalkTimeDist;
	}
	
	public File getRegUrbanStopMapping() {
		return new File("regUrbanStopMap.csv");
	}
    
    public static AppProperties getDefaultProps() {
    		if (defaultProp == null) {
    			defaultProp = new AppProperties();
    			defaultProp.readDefaultProps();
    		}
    		return defaultProp;
    }
    
    private void readHubCfgFile(String rs) {
		
		File file = new File(rs);
		hubFolder = file.getParentFile();
		
		Charset charset = Charset.forName("UTF-8");
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), charset)) {
		    		    
			String line = "";
		    while ((line = reader.readLine()) != null) {
		    	
		    		//do transformation
		    		String[] lineParts = line.split("=");
		    		if(lineParts.length>1) {
		    			if(lineParts[0].trim().equalsIgnoreCase("timetable_TF")) {
		    				Path p = Paths.get(hubFolder.getAbsolutePath(), lineParts[1].trim());
		    				timetableHub = p.toFile();
		    			} else if (lineParts[0].trim().equalsIgnoreCase("flows_TF")) {
		    				Path p = Paths.get(hubFolder.getAbsolutePath(), lineParts[1].trim());
		    				passFlowHub = p.toFile();
		    			} else if (lineParts[0].trim().equalsIgnoreCase("zones_to_vertices_map")) {
		    				Path p = Paths.get(hubFolder.getAbsolutePath(), lineParts[1].trim());
		    				hubStopsZones = p.toFile();
		    			} else if (lineParts[0].trim().equalsIgnoreCase("write_tt_4_transform_file_name")) {
		    				Path p = Paths.get(hubFolder.getAbsolutePath(), lineParts[1].trim());
		    				pedWalkTimeDist = p.toFile();
		    			}
		    			
		    		}
		    }
		    
		    		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void readBMMasterFile(String rs) {
		
		File file = new File(rs);
		bmFolder = file.getParentFile();
		
		Path p = Paths.get(bmFolder.getAbsolutePath(), "o_transitlog_out.dat");
		timetableBM = p.toFile();
		
		p = Paths.get(bmFolder.getAbsolutePath(), "o_selected_paths.dat");
		passFlowBM = p.toFile();
		
		p = Paths.get(bmFolder.getAbsolutePath(), "transit_network.dat");
		bmTransitNetwork = p.toFile();
			
	}

	public void readDefaultProps() {

		try {
	    		File file = new File("config.properties");
	    		if (!file.exists())
	    			file.createNewFile();
	    		else {
		    		InputStream input = new FileInputStream(file);
			        prop.load(input);
			        
			        setHubSolver(prop.getProperty("hubSolver"));
			        setHubCfgFile(prop.getProperty("hubCfgFile"));
			        setUrbanSolver(prop.getProperty("urbanSolver"));
			        setBMMasterFile(prop.getProperty("BMMasterFile"));
			        setRegionalSolver(prop.getProperty("regionalSolver"));
			        setRegInputOutput(prop.getProperty("regInput"), prop.getProperty("regOutput"));
			        
			        try {
			        	debug = Boolean.parseBoolean(prop.getProperty("debug", "false"));
			        } catch (Exception e) {
			        	e.printStackTrace();
			        }		        
	    		}       
	    }catch (IOException ex) {
	        ex.printStackTrace();
	    } 				
    }

	public void storeDeafultAppProps() {
		try {
	    		File file = new File("config.properties");
	    		if (!file.exists())
	    			file.createNewFile();
	    		
	        FileOutputStream out = new FileOutputStream(file);
	        prop.setProperty("hubSolver", hubSolver != null ? hubSolver : "");
	        prop.setProperty("hubCfgFile", hubConfig != null ? hubConfig : "");
	        prop.setProperty("urbanSolver", urbanSolver != null ? urbanSolver : "");
	        prop.setProperty("BMMasterFile", urbanMasterFile != null ? urbanMasterFile : "");
	        prop.setProperty("regionalSolver", regSolver != null ? regSolver : "");
	        prop.setProperty("regInput", regInput != null ? regInput : "");
	        prop.setProperty("regOutput", regOutput != null ? regOutput : "");
	        prop.store(out, "");
	        
	    }catch (IOException ex) {
	        ex.printStackTrace();
	    } 
	}
	
}
