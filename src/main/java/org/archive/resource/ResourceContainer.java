package org.archive.resource;

/**
 * A container for one or more Resource objects. Primarily holds context for the
 * current record
 * 
 * @author Brad
 *
 */
public interface ResourceContainer {
	/**
	 * @return the name of this container. Could be a path, url, basename...
	 */
	public String getName();
	public boolean isCompressed();
}