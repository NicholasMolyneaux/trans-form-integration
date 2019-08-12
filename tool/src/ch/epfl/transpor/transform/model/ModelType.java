package ch.epfl.transpor.transform.model;

public enum ModelType {
	HUB("Hub"),
	URBAN("Urban"),
	REGIONAL("Regional");
	
	private final String text;
	
	ModelType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
