package org.archive.resource;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author Brad
 *
 */
public interface ResourceFactory {
	
	/**
	 * Attempts to create a Resource from the InputStream 
	 * @param is
	 * @param metaData
	 * @param container
	 * @return
	 * @throws ResourceParseException
	 * @throws IOException
	 */
	public Resource getResource(InputStream is, MetaData parentMetaData, 
			ResourceContainer container) 
	throws ResourceParseException, IOException;
}
