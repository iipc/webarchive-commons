package org.archive.extract;

import org.archive.resource.Resource;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceFactory;

public interface ResourceFactoryMapper extends ResourceConstants {
	public ResourceFactory mapResourceToFactory(Resource resource);
}
