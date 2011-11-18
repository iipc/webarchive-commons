package org.archive.resource.producer;

import org.archive.resource.warc.WARCResourceFactory;

public class WARCFile extends EnvelopedResourceFile {
	public WARCFile() {
		super(new WARCResourceFactory());
	}
}
