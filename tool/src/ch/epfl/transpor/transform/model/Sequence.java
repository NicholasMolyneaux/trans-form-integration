package ch.epfl.transpor.transform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Sequence implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3015978323656308641L;
	
	public String name = "";
	public List<ModelType> steps = new ArrayList<ModelType>();
	public AppProperties appProps = null;
	
	public Sequence() {}
	public Sequence(ModelType mt) {
		steps.add(mt);
	}


}
