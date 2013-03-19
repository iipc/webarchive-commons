package org.archive.hadoop;

public class ResourceContext {
	public String name;
	public long offset;
	public ResourceContext(String name, long offset) {
		this.name = name;
		this.offset = offset;
	}
}
