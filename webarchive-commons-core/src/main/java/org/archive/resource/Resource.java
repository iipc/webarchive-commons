package org.archive.resource;

import java.io.InputStream;


public interface Resource {
	/**
	 * @return the ResourceContainer holding this Resource
	 */
	public ResourceContainer getContainer();

	/**
	 * @return an InputStream for reading data from this Resource. Use only
	 * once, and assume it is unbuffered
	 */
	public InputStream getInputStream();

	/**
	 * @return the MetaData associated with this Resource
	 */
	public MetaData getMetaData();
}
