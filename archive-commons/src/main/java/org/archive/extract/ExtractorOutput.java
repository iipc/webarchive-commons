package org.archive.extract;

import java.io.IOException;

import org.archive.resource.Resource;

public interface ExtractorOutput {
	public void output(Resource resource) throws IOException;
}
