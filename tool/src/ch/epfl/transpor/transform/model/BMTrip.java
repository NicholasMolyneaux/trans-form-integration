package ch.epfl.transpor.transform.model;

import java.util.HashMap;

public class BMTrip {
	public String tripID = "";
	public String lineID = "";
	public String dispatchTime = "";
	public HashMap<String,String> stops = new HashMap<String,String>();
}
