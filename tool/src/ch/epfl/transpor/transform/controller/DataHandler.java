package ch.epfl.transpor.transform.controller;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Random;

import ch.epfl.transpor.transform.model.AppProperties;
import ch.epfl.transpor.transform.model.BMTrip;
import ch.epfl.transpor.transform.model.RegBMStopMapping;
import ch.epfl.transpor.transform.model.exceptions.RunModelException;

import javax.json.*;

public class DataHandler {

    protected HashSet<String> hubStops = null;
    protected HashMap<String, RegBMStopMapping> regUrbStopMap = null;

    // cumulative probability distribution of the default mapping between walking stops and the hub zones.
    private double[] walkingZonesCumProb = null;

    // zones names corresponding the cumulative probability.
    private String[] walkingZones = null;

    // mapping for specific walking stops to hub zones. Stop to list of hub zones.
    private HashMap<String, String[]> stopSpecificDestination = new HashMap<>();

    // mapping for specific walking stops to hub zones. Stop to cumulative density function.
    private HashMap<String, double[]> stopSpecificProbabilities = new HashMap<>();


    /**
     * Based on the cumulative probability passed as argument, returns an index based on the probability distribution.
     *
     * @param arr CDF to sample from
     * @return index sampled
     */
    private int sampleArray(double[] arr) {
        double val = new Random().nextDouble();
        int idx = 0;
        while (val > arr[idx]) {idx++;}
        return idx;
    }

    /**
     * Takes a walking stop as input and returns a hub zone based on the probabilites defind in the JSON.
     *
     * @param stop
     * @return
     */
    private String walkingStopsToZones(String stop) {
        if (stopSpecificDestination.containsKey(stop)){
            // the stop is in the list of stop specific list

            return stopSpecificDestination.get(stop)[sampleArray(stopSpecificProbabilities.get(stop))];
        } else {
            // uses the default distribution across walking zones.

            return walkingZones[sampleArray(walkingZonesCumProb)];
        }
    }


    public void initialize(AppProperties cfg) throws FileNotFoundException {

        JsonReader reader = Json.createReader(new FileInputStream(cfg.getHubStopsZones()));
        JsonObject jsonObj = reader.readObject();

        JsonArray stops = jsonObj.getJsonArray("stop2nodes");

        hubStops = new HashSet<String>();
        for (JsonObject stop : stops.getValuesAs(JsonObject.class)) {
            hubStops.add("" + stop.getString("stop"));
        }

        // Gets the walking stops to nodes json object. This contains a default map and a stop specific map.
        JsonObject walkingStopsMapping = jsonObj.getJsonObject("walkingStops2Nodes");

        // Process the default map to build the cumulative probability function for choosing the walking destination.
        JsonArray defaultWalkingStops = walkingStopsMapping.getJsonArray("defaultDistribution");
        walkingZonesCumProb = new double[defaultWalkingStops.size()];
        walkingZones = new String[defaultWalkingStops.size()];

        {
            int defaultProbCounter = 0;
            for (JsonObject nodeProb : defaultWalkingStops.getValuesAs(JsonObject.class)) {
                if (defaultProbCounter == 0) {
                    walkingZonesCumProb[defaultProbCounter] = nodeProb.getJsonNumber("probability").doubleValue();
                } else {
                    walkingZonesCumProb[defaultProbCounter] = walkingZonesCumProb[defaultProbCounter - 1] + nodeProb.getJsonNumber("probability").doubleValue();
                }
                walkingZones[defaultProbCounter] = nodeProb.getString("node");
                defaultProbCounter++;
            }
        }

        // Processes the stop specific destinations
        JsonArray specificWalkingStops = walkingStopsMapping.getJsonArray("stopSpecificDistribution");
        for (JsonObject stopProb: specificWalkingStops.getValuesAs(JsonObject.class)) {
            int specificProbCounter = 0;
            double[] cumProb = new double[stopProb.getJsonArray("nodesProb").size()];
            String[] walkingZones = new String[stopProb.getJsonArray("nodesProb").size()];
            for (JsonObject nodeProb: stopProb.getJsonArray("nodesProb").getValuesAs(JsonObject.class)){
            	if (specificProbCounter == 0) {
                    cumProb[specificProbCounter] = nodeProb.getJsonNumber("probability").doubleValue();

            	} else {
                    cumProb[specificProbCounter] = walkingZonesCumProb[specificProbCounter-1] + nodeProb.getJsonNumber("probability").doubleValue();
            	}
                walkingZones[specificProbCounter] = nodeProb.getString("node");
                specificProbCounter++;
            }
            stopSpecificDestination.put(stopProb.getString("stop"), walkingZones);
            stopSpecificProbabilities.put(stopProb.getString("stop"), cumProb);
        }

        reader.close();

        //read mapping between urban and regional stops
        regUrbStopMap = new HashMap<String, RegBMStopMapping>();
        Path path = Paths.get(cfg.getRegUrbanStopMapping().getAbsolutePath());
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader rdr = Files.newBufferedReader(path, charset)) {

            String line = "";
            while ((line = rdr.readLine()) != null) {

                String[] lineParts = line.split(",");
                if (lineParts == null || lineParts.length < 2)
                    continue;
                
                RegBMStopMapping bmStops;
                if(regUrbStopMap.containsKey(lineParts[1]))
                	bmStops = regUrbStopMap.get(lineParts[1]);
                else
                	bmStops = new RegBMStopMapping();
                bmStops.tripID = lineParts[1];
                ArrayList<String> bms = new ArrayList<String>();
                for (int i=2; i<lineParts.length; i++) 
                	bms.add(lineParts[i]);
                bmStops.stopIDs.put(lineParts[0], bms);
                

                if (!regUrbStopMap.containsKey(lineParts[1]))
                    regUrbStopMap.put(lineParts[1], bmStops);
            }

            rdr.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void xfrmUrbanToHubFiles(AppProperties cfg) throws RunModelException, IOException {

        if (hubStops == null)
            initialize(cfg);

        //clean hub folder
        File hf = cfg.getTimetableHub().getParentFile();
        if (hf.exists()) {
            if (hf.isDirectory()) {
                for (File f : hf.listFiles()) {
                    f.delete();
                }
            }
        } else
            hf.mkdirs();


        //transform timetable files
        if (cfg.getTimetableBM() != null && cfg.getTimetableBM().exists())
            if (!trySplitTimetableBM(cfg.getTimetableBM()))
                xfrmTimetableUrbanToHub(cfg.getTimetableBM(), cfg.getTimetableHub());

        int cnt = 1;
        while (true) {

            //build BM timetable file
            String fp[] = cfg.getTimetableBM().getAbsolutePath().split("\\.");
            if (fp.length != 2) break;
            File ttBM = new File(fp[0] + "_" + cnt + "." + fp[1]);

            //build hub timetable file
            fp = cfg.getTimetableHub().getAbsolutePath().split("\\.");
            if (fp.length != 2) break;
            File ttHub = new File(fp[0] + "_" + cnt + "." + fp[1]);

            if (ttBM.exists()) {
                xfrmTimetableUrbanToHub(ttBM, ttHub);
                cnt++;
            } else
                break;
        }


        //transform passenger flow files
        if (cfg.getPassFlowBM() != null && cfg.getPassFlowBM().exists())
            if (!trySplitPassFlowBM(cfg.getPassFlowBM()))
                xfrmPassFlowUrbanToHub(cfg.getPassFlowBM(), cfg.getPassFlowHub());

        cnt = 1;
        while (true) {

            //build BM timetable file
            String fp[] = cfg.getPassFlowBM().getAbsolutePath().split("\\.");
            if (fp.length != 2) break;
            File psBM = new File(fp[0] + "_" + cnt + "." + fp[1]);

            //build hub timetable file
            fp = cfg.getPassFlowHub().getAbsolutePath().split("\\.");
            if (fp.length != 2) break;
            File psHub = new File(fp[0] + "_" + cnt + "." + fp[1]);

            if (psBM.exists()) {
                xfrmPassFlowUrbanToHub(psBM, psHub);
                cnt++;
            } else
                break;


        }
    }

    private boolean trySplitTimetableBM(File timetableBM) throws IOException {

        boolean ret = false;

        if (timetableBM == null || !timetableBM.exists())
            return false;

        Path path = Paths.get(timetableBM.getAbsolutePath());
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = "";
            StringBuilder oneFile = new StringBuilder();
            int fileCnt = 1;

            //read header
            String header = reader.readLine();
            oneFile.append(header + "\r\n");

            while ((line = reader.readLine()) != null) {
                String[] lineParts = line.split("\t");
                if (lineParts == null || lineParts.length < 4)
                    continue;

                if (line.trim().equals(header.trim())) {
                    //new iteration found

                    //build BM timetable file
                    String fp[] = timetableBM.getAbsolutePath().split("\\.");
                    if (fp.length != 2) break;
                    File ttBM = new File(fp[0] + "_" + fileCnt + "." + fp[1]);

                    try (BufferedWriter writer = Files.newBufferedWriter(ttBM.toPath(), charset)) {
                        writer.write(oneFile.toString());
                        writer.close();
                        oneFile = new StringBuilder();
                    } catch (Exception e) {
                    }

                    ret = true;
                    fileCnt++;
                }
                oneFile.append(line + "\r\n");
            }

            if (ret) {
                //last iteration
                //build BM timetable file
                String fp[] = timetableBM.getAbsolutePath().split("\\.");
                if (fp.length == 2) {
                    File ttBM = new File(fp[0] + "_" + fileCnt + "." + fp[1]);

                    try (BufferedWriter writer = Files.newBufferedWriter(ttBM.toPath(), charset)) {
                        writer.write(oneFile.toString());
                        writer.close();
                        oneFile = new StringBuilder();
                    } catch (Exception e) {
                    }
                }
            }

        }

        return ret;
    }

    private boolean trySplitTimetableBMOld(File timetableBM) throws IOException {

        boolean ret = false;

        if (timetableBM == null || !timetableBM.exists())
            return false;

        Path path = Paths.get(timetableBM.getAbsolutePath());
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = "";
            String tripIDstopID = "";
            StringBuilder oneFile = new StringBuilder();
            int fileCnt = 1;

            //read header
            String header = reader.readLine();
            oneFile.append(header + "\r\n");

            while ((line = reader.readLine()) != null) {
                String[] lineParts = line.split("\t");
                if (lineParts == null || lineParts.length < 4)
                    continue;

                if (tripIDstopID.isEmpty()) {
                    tripIDstopID = lineParts[1] + "_" + lineParts[3];
                } else if (tripIDstopID.equals(lineParts[1] + "_" + lineParts[3])) {
                    //new iteration found

                    //build BM timetable file
                    String fp[] = timetableBM.getAbsolutePath().split("\\.");
                    if (fp.length != 2) break;
                    File ttBM = new File(fp[0] + "_" + fileCnt + "." + fp[1]);

                    try (BufferedWriter writer = Files.newBufferedWriter(ttBM.toPath(), charset)) {
                        writer.write(oneFile.toString());
                        writer.close();
                        oneFile = new StringBuilder();
                        oneFile.append(header + "\r\n");
                    } catch (Exception e) {
                    }

                    ret = true;
                    fileCnt++;
                }
                oneFile.append(line + "\r\n");
            }

            if (ret) {
                //last iteration
                //build BM timetable file
                String fp[] = timetableBM.getAbsolutePath().split("\\.");
                if (fp.length == 2) {
                    File ttBM = new File(fp[0] + "_" + fileCnt + "." + fp[1]);

                    try (BufferedWriter writer = Files.newBufferedWriter(ttBM.toPath(), charset)) {
                        writer.write(oneFile.toString());
                        writer.close();
                        oneFile = new StringBuilder();
                    } catch (Exception e) {
                    }
                }
            }

        }

        return ret;
    }

    private boolean trySplitPassFlowBM(File passFlowBM) throws IOException {

        boolean ret = false;

        if (passFlowBM == null || !passFlowBM.exists())
            return false;

        Path path = Paths.get(passFlowBM.getAbsolutePath());
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = "";
            StringBuilder oneFile = new StringBuilder();
            int fileCnt = 1;

            //skip header
            String header = reader.readLine();
            oneFile.append(header + "\r\n");

            while ((line = reader.readLine()) != null) {
                String[] lineParts = line.split("\t");
                if (lineParts == null || lineParts.length < 4)
                    continue;

                if (line.trim().equals(header.trim())) {
                    //new iteration found

                    //build BM timetable file
                    String fp[] = passFlowBM.getAbsolutePath().split("\\.");
                    if (fp.length != 2) break;
                    File ttBM = new File(fp[0] + "_" + fileCnt + "." + fp[1]);

                    try (BufferedWriter writer = Files.newBufferedWriter(ttBM.toPath(), charset)) {
                        writer.write(oneFile.toString());
                        writer.close();
                        oneFile = new StringBuilder();
                    } catch (Exception e) {
                    }

                    ret = true;
                    fileCnt++;
                }
                oneFile.append(line + "\r\n");
            }

            if (ret) {
                //last iteration
                //build BM timetable file
                String fp[] = passFlowBM.getAbsolutePath().split("\\.");
                if (fp.length == 2) {
                    File ttBM = new File(fp[0] + "_" + fileCnt + "." + fp[1]);

                    try (BufferedWriter writer = Files.newBufferedWriter(ttBM.toPath(), charset)) {
                        writer.write(oneFile.toString());
                        writer.close();
                        oneFile = new StringBuilder();
                    } catch (Exception e) {
                    }
                }
            }

        }

        return ret;
    }


    private boolean trySplitPassFlowBMOld(File passFlowBM) throws IOException {

        boolean ret = false;

        if (passFlowBM == null || !passFlowBM.exists())
            return false;

        Path path = Paths.get(passFlowBM.getAbsolutePath());
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = "";
            String origIDdestID = "";
            StringBuilder oneFile = new StringBuilder();
            int fileCnt = 1;

            //skip header
            String header = reader.readLine();
            oneFile.append(header + "\r\n");

            int lineCnt = 1;
            int lastLine = -1;

            while ((line = reader.readLine()) != null) {
                String[] lineParts = line.split("\t");
                if (lineParts == null || lineParts.length < 4)
                    continue;

                if (origIDdestID.isEmpty()) {
                    origIDdestID = lineParts[1] + "_" + lineParts[3];
                    lastLine = lineCnt;
                } else if (origIDdestID.equals(lineParts[1] + "_" + lineParts[3])) {

                    if (lineCnt != lastLine + 1) {
                        //new iteration found

                        //build BM timetable file
                        String fp[] = passFlowBM.getAbsolutePath().split("\\.");
                        if (fp.length != 2) break;
                        File ttBM = new File(fp[0] + "_" + fileCnt + "." + fp[1]);

                        try (BufferedWriter writer = Files.newBufferedWriter(ttBM.toPath(), charset)) {
                            writer.write(oneFile.toString());
                            writer.close();
                            oneFile = new StringBuilder();
                            oneFile.append(header + "\r\n");
                        } catch (Exception e) {
                        }

                        ret = true;
                        fileCnt++;
                    }

                    lastLine = lineCnt;
                }
                oneFile.append(line + "\r\n");
                lineCnt++;
            }

            if (ret) {
                //last iteration
                //build BM timetable file
                String fp[] = passFlowBM.getAbsolutePath().split("\\.");
                if (fp.length == 2) {
                    File ttBM = new File(fp[0] + "_" + fileCnt + "." + fp[1]);

                    try (BufferedWriter writer = Files.newBufferedWriter(ttBM.toPath(), charset)) {
                        writer.write(oneFile.toString());
                        writer.close();
                        oneFile = new StringBuilder();
                    } catch (Exception e) {
                    }
                }
            }

        }

        return ret;
    }

    public void xfrmTimetableUrbanToHub(File timetableBM, File timetableHub) throws FileNotFoundException, RunModelException {

        //timetable
        if (timetableBM == null || !timetableBM.exists())
            return;

        Path path = Paths.get(timetableBM.getAbsolutePath());
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {

            JsonObjectBuilder tt = Json.createObjectBuilder();
            tt.add("location", "lausanne");
            JsonArrayBuilder trains = Json.createArrayBuilder();

            String line = "";
            while ((line = reader.readLine()) != null) {

                //do transformation
                String[] lineParts = line.split("\t");
                if (lineParts == null || lineParts.length < 10 || !hubStops.contains(lineParts[3]))
                    continue;

                JsonObjectBuilder train = Json.createObjectBuilder();
                train.add("stop_id", lineParts[3]);
                train.add("trip_id", lineParts[1]);
                train.add("arrival_time", lineParts[5]);
                train.add("departure_time", lineParts[9]);
                //write output
                trains.add(train);
            }

            tt.add("trains", trains);

            OutputStream out = new FileOutputStream(timetableHub);
            JsonWriter writer = Json.createWriter(out);
            writer.writeObject(tt.build());
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void xfrmPassFlowUrbanToHub(File passFlowBM, File passFlowHub) throws FileNotFoundException, RunModelException {

        //passenger flow

        if (passFlowBM == null || !passFlowBM.exists()) return;

        Path path = Paths.get(passFlowBM.getAbsolutePath());
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {

            JsonObjectBuilder passFlows = Json.createObjectBuilder();
            passFlows.add("location", "lausanne");
            JsonArrayBuilder ptFlows = Json.createArrayBuilder();

            String line = "";
            while ((line = reader.readLine()) != null) {

                //check the line format
                if (!line.matches(".+\\{.+\\}.*\\{.+\\}"))
                    continue;

                //getting the stops
                String stopsTrips = line.trim().substring(line.indexOf('{'), line.lastIndexOf('}') + 1);
                String stops = stopsTrips.substring(stopsTrips.indexOf('{') + 1, stopsTrips.indexOf('}')).trim();
                String trips = stopsTrips.substring(stopsTrips.lastIndexOf('{') + 1, stopsTrips.lastIndexOf('}')).trim();

                //do transformation
                String[] lineParts = line.split("\t");

                if (lineParts == null || lineParts.length < 4)
                    continue;

                String[] stopIds = stops.split("\t");
                String[] tripIds = trips.split("\t");

                int numberStops = stopIds.length;

                // the number of stops must be greater than 4 and even, otherwise there is a problem in the data from BM.
                if (numberStops < 4 || numberStops % 2 != 0) {continue;}

                // Deals with the different cases where the passenger can do a walking leg inside a hub.
                if (hubStops.contains(stopIds[0]) && hubStops.contains(stopIds[1])) {
                    // checks if the first leg of the trips takes place inside the hub (i.e. the stop IDs are in stopIds).

                    JsonObjectBuilder ptFlow = Json.createObjectBuilder();
                    ptFlow.add("pass_id", lineParts[0]);
                    ptFlow.add("origin", "z_" + walkingStopsToZones(stopIds[0]));
                    ptFlow.add("destination", "t_" + tripIds[0]);
                    ptFlow.add("origin_time", java.lang.Math.max(Double.parseDouble(lineParts[5])-120.0, 30.0*new Random().nextDouble()));
                    ptFlow.add("original_origin_stop", stopIds[0]);
                    ptFlow.addNull("original_destination_stop"); // null since irrelevant
                    //write output
                    ptFlows.add(ptFlow);

                } else if (hubStops.contains(stopIds[numberStops - 2]) && hubStops.contains(stopIds[numberStops-1])) {
                    // checks if the last leg of the trips takes place inside the hub (i.e. the stop IDs are in stopIds).

                    JsonObjectBuilder ptFlow = Json.createObjectBuilder();
                    ptFlow.add("pass_id", lineParts[0]);
                    ptFlow.add("origin", "t_" + tripIds[tripIds.length - 1]);
                    ptFlow.add("destination", "z_" + walkingStopsToZones(stopIds[numberStops-1]));
                    ptFlow.addNull("origin_time"); // null since irrelevant
                    ptFlow.addNull("original_origin_stop"); // null since irrelevant
                    ptFlow.add("original_destination_stop", stopIds[numberStops-1]);
                    //write output
                    ptFlows.add(ptFlow);
                } else {

                    int tripInd = 0;

                    // checks the transfers taking place inside the passenger's trip. As the list of stops alternates between
                    // walking and PT legs, we only have to check the walking legs. Hence the walking legs indices in
                    // the list of stops are (2,3) and (4,5) and (6,7), etc.
                    for (int i = 1; i < (stopIds.length)/2 - 1; i++) {

                        if (stopIds.length > 2*i + 1 && tripIds.length > tripInd + 1 && hubStops.contains(stopIds[2*i]) && hubStops.contains(stopIds[2*i + 1]) && stopIds[2*i].compareTo(stopIds[2*i + 1]) != 0) {
                            //hub is the ending point of one trip and starting point of the other, and the arriving stop is different from the leaving stop.

                            JsonObjectBuilder ptFlow = Json.createObjectBuilder();
                            ptFlow.add("pass_id", lineParts[0]);
                            ptFlow.add("origin", "t_" + tripIds[tripInd]);
                            ptFlow.add("destination", "t_" + tripIds[tripInd + 1]);
                            ptFlow.addNull("origin_time"); // null since irrelevant
                            ptFlow.addNull("original_origin_stop"); // null since irrelevant
                            ptFlow.addNull("original_destination_stop"); // null since irrelevant
                            //write output
                            ptFlows.add(ptFlow);
                        }
                        tripInd++;
                    }
                }
            }

            passFlows.add("PTFlows", ptFlows);

            OutputStream out = new FileOutputStream(passFlowHub);
            JsonWriter writer = Json.createWriter(out);
            writer.writeObject(passFlows.build());
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void transformHubToUrban(Date simTime, AppProperties cfg) {

        //check files existence
        if (cfg.getPedWalkTimeDist() == null || !cfg.getPedWalkTimeDist().exists()
                || cfg.getBmTransitNetwork() == null)
            return;

        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StringBuilder hubWalkDists = new StringBuilder();
        int hubWalkDisCnt = 0;

        Set<String> newODs = new HashSet<String>();

        try (JsonReader reader = Json.createReader(new FileInputStream(cfg.getPedWalkTimeDist()))) {
            JsonArray walkDists = reader.readArray();

            hubWalkDisCnt = walkDists.size();

            for (JsonObject wd : walkDists.getValuesAs(JsonObject.class)) {

                hubWalkDists.append("{\t" + wd.getString("o") + "\t" + wd.getString("d"));
                newODs.add(wd.getString("o") + "_" + wd.getString("d"));

                Date tmp = dateParser.parse(wd.getString("start_timestamp"));
                hubWalkDists.append("\t" + ((tmp.getTime() - simTime.getTime()) / 1000.0));

                tmp = dateParser.parse(wd.getString("end_timestamp"));
                hubWalkDists.append("\t" + ((tmp.getTime() - simTime.getTime()) / 1000.0));

                JsonArray quantiles = wd.getJsonArray("quantiles");
                hubWalkDists.append("\t" + quantiles.size() + "\t{");

                for (JsonNumber num : quantiles.getValuesAs(JsonNumber.class)) {
                    hubWalkDists.append("\t" + num.intValue());
                }
                hubWalkDists.append("\t}\t{");

                JsonArray values = wd.getJsonArray("values");
                for (JsonNumber num : values.getValuesAs(JsonNumber.class)) {
                    hubWalkDists.append("\t" + num.doubleValue());
                }
                hubWalkDists.append("\t}}\r\n");

            }

            reader.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return;
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        Path tmpFile = Paths.get(cfg.getBmTransitNetwork().getParent(), "tmp.dat");
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader bmReader = Files.newBufferedReader(cfg.getBmTransitNetwork().toPath(), charset);
             BufferedWriter writer = Files.newBufferedWriter(tmpFile, charset)) {

            //first rewrite the first part of the BM file
            String line = "";
            while (((line = bmReader.readLine()) != null) && !line.toLowerCase().contains("stops_walking_times")) {
                writer.write(line);
                writer.newLine();
            }

            //now, read the previous walking times lines and select which to keep (which do not exist in the new file)
            String tmp = "";
            while (((line = bmReader.readLine()) != null) && !line.toLowerCase().contains("lines")) {
                if (!line.isEmpty()) {
                    tmp = tmp + " " + line;
                }
            }

            ArrayList<String> walkTimesToKeep = new ArrayList<String>();
            Pattern p = Pattern.compile("(\\{.*?\\{.*?\\}.*?\\{.*?\\}.*?\\})");
            Matcher m = p.matcher(tmp);
            while (m.find()) {
                String oldWalkTimes = m.group(1).trim();
                String[] parts = oldWalkTimes.split(" ");
                if (parts.length > 2 && !newODs.contains(parts[1] + "_" + parts[2])) {
                    walkTimesToKeep.add(oldWalkTimes);
                }
            }


            //now, write the old and new walking time
            int wtCount = hubWalkDisCnt + walkTimesToKeep.size();
            if (wtCount > 0) {
                writer.write("stops_walking_times: " + wtCount);
                writer.newLine();

                //write the previous walking times which are not updated
                for (String wt : walkTimesToKeep) {
                    writer.write(wt);
                    writer.newLine();
                }

                //write the new walking times
                writer.write(hubWalkDists.toString());
                writer.newLine();
                writer.newLine();
            }

            //finally, write the rest of the file
            if (line != null) {
                writer.write(line);
                writer.newLine();
            }

            while ((line = bmReader.readLine()) != null) {
                if (!line.isEmpty()) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            bmReader.close();
            writer.close();

            //rename the tmp file
            if (cfg.getBmTransitNetwork().delete())
                tmpFile.toFile().renameTo(cfg.getBmTransitNetwork());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void transformRegionalToUrban(Date simTime, AppProperties cfg) throws FileNotFoundException {

        //get revised timetable
        if (cfg.getRevRegTimetable() == null
                || !cfg.getRevRegTimetable().exists() || cfg.getBmTransitNetwork() == null)
            return;

        if (regUrbStopMap == null)
            initialize(cfg);

        Calendar tmp = Calendar.getInstance();
        tmp.setTime(simTime);
        int refTime = tmp.get(Calendar.HOUR_OF_DAY) * 3600 + tmp.get(Calendar.MINUTE) * 60 + tmp.get(Calendar.SECOND);
        
        //read regional trips
        HashMap<String,BMTrip> updatedTrips = new HashMap<String,BMTrip>();
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader regReader = Files.newBufferedReader(cfg.getRevRegTimetable().toPath(), charset)) {
	        
        	BMTrip bmTrip = null;
	        RegBMStopMapping regMapping = null;
	        String currTripID = "";
	        String regline = "";
	        
	        while ((regline = regReader.readLine()) != null) {
	
	            //do transformation
	            String[] lineParts = regline.split(" +");
	            if (lineParts == null || lineParts.length < 2)
	                continue;
	
	            if (lineParts.length == 4) {
	
	                if (bmTrip != null) {
	                    if(regUrbStopMap.containsKey(bmTrip.tripID)) {	
	                    	updatedTrips.put(bmTrip.tripID,bmTrip);
	                    }
	                }
	                bmTrip = new BMTrip();
                    
	                //removing the last character from the regional model train number to obtain BM trip ID
	                if (lineParts[0].length() > 1)
	                    currTripID = lineParts[0].substring(0, lineParts[0].length() - 1);
	                else
	                    currTripID = "!SHORT_REG_TRAIN_NUM!";
	                //remove the last digit to obtain the line ID
	                String lineID = "!SHORT_REG_TRAIN_NUM!";
	                if (currTripID.length() > 1)
	                    lineID = currTripID.substring(1, currTripID.length());
	                float bmTime = transformRegTime(refTime, lineParts[2]);
	                
	                bmTrip.tripID = currTripID;
	                bmTrip.lineID = lineID;
	                bmTrip.dispatchTime = Float.toString(bmTime);
	                
                	regMapping = regUrbStopMap.get(currTripID);
	
	            } else if (lineParts.length == 2) {
	                String regStop = lineParts[0].trim();
	                if (regMapping != null && regMapping.stopIDs.containsKey(regStop)) {
	                    List<String> urbStops = regMapping.stopIDs.get(regStop);
	                	float bmTime = transformRegTime(refTime, lineParts[1]);
	                    for(String urbStop : urbStops) {
	                    	bmTrip.stops.put(urbStop, Float.toString(bmTime));
	                    }
	                }
	            }
	        }
	        if (bmTrip != null) {
                if(regUrbStopMap.containsKey(bmTrip.tripID)) {	
                	updatedTrips.put(bmTrip.tripID,bmTrip);
                }
            }
	        
	        regReader.close();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }

        Path tmpFile = Paths.get(cfg.getBmTransitNetwork().getParent(), "tmp.dat");
        try (BufferedReader bmReader = Files.newBufferedReader(cfg.getBmTransitNetwork().toPath(), charset);
             BufferedWriter writer = Files.newBufferedWriter(tmpFile, charset)) {

            //first rewrite the first part of the BM file
            String line = "";
            while (((line = bmReader.readLine()) != null) && !line.toLowerCase().contains("trips")) {
                writer.write(line);
                writer.newLine();
            }
            writer.write(line);
            writer.newLine();
            while ((!(line = bmReader.readLine()).startsWith("{"))) {
                writer.write(line);
                writer.newLine();
            }

            //now, update the trips
            String[] firstLineParts = line.split("\t");
            boolean firstLine = true;
            String currTrip = "";
            BMTrip regMap = updatedTrips.get(firstLineParts[1]);
            while (((line = bmReader.readLine()) != null) && !line.toLowerCase().contains("travel_time_disruptions")) {
                if (!line.trim().isEmpty()) {
                	
                	String[] parts = line.trim().split("\t");
                    if(parts.length == 5) {
                    	writer.write(firstLineParts[0]+"\t"+firstLineParts[1]+"\t"+firstLineParts[2]+"\t"+firstLineParts[3]+"\t"+firstLineParts[4]);
                    	writer.newLine();
                    	writer.write(currTrip);
                    	writer.newLine();
                    	
                    	firstLineParts = parts;
                    	firstLine = true;
                    	regMap = updatedTrips.get(firstLineParts[1]);
                    	currTrip = "";
                    }
                    else if (parts.length == 4) {
                    	if(regMap != null && regMap.stops.containsKey(parts[1])) {
                    		parts[2] = regMap.stops.get(parts[1]);
                    		if(firstLine) {
                    			firstLineParts[3] = regMap.stops.get(parts[1]);
                    		}
                    	}
                    	currTrip += parts[0]+"\t"+parts[1]+"\t"+parts[2]+"\t"+parts[3]+"\r\n";
                    	firstLine = false;
                    }
                    else {
                    	currTrip += line;
                    	firstLine = false;
                    }
                }
            }
            //write the last trip
            if(!currTrip.isEmpty()) {
	            writer.write(firstLineParts[0]+"\t"+firstLineParts[1]+"\t"+firstLineParts[2]+"\t"+firstLineParts[3]+"\t"+firstLineParts[4]);
	        	writer.newLine();
	        	writer.write(currTrip);
	        	writer.newLine();
	        	writer.newLine();
            }
        	
            //finally, write the rest of the file
            if (line != null) {
                writer.write(line);
                writer.newLine();
            }

            while ((line = bmReader.readLine()) != null) {
                if (!line.isEmpty()) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            bmReader.close();
            writer.close();

            //rename the tmp file
            if (cfg.getBmTransitNetwork().delete())
                tmpFile.toFile().renameTo(cfg.getBmTransitNetwork());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private float transformRegTime(int refTime, String t) {

        try {
            return Float.parseFloat(t) - refTime;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

}
