package org.archive.resource;


public class ResourceParseException extends Exception {

	/** */
	private static final long serialVersionUID = 5364502969148304884L;
	public ResourceParseException(Exception e) {
		super(e);
	}
	public ResourceParseException(Exception e, MetaData metaData) {
		super(e);
	}
	
}
