package org.archive.resource.producer;

import org.archive.resource.arc.ARCResourceFactory;

public class ARCFile extends EnvelopedResourceFile {
	public ARCFile() {
		super(new ARCResourceFactory());
	}
}
