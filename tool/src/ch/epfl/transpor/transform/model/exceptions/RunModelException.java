package ch.epfl.transpor.transform.model.exceptions;

import ch.epfl.transpor.transform.model.ModelType;

public class RunModelException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ModelType modelType;
	
	public RunModelException (ModelType mt) {
		modelType = mt;
	}

}
