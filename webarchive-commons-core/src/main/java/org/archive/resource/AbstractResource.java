package org.archive.resource;

import java.io.IOException;
import java.io.PrintStream;

import org.archive.util.StreamCopy;

import com.google.common.io.ByteStreams;

public abstract class AbstractResource implements Resource {
	protected ResourceContainer container;
	protected MetaData metaData;
	public AbstractResource(MetaData metaData, 
			ResourceContainer container) {
		this.container = container;
		this.metaData = metaData;
	}

	public ResourceContainer getContainer() {
		return container;
	}
	public MetaData getMetaData() {
		return metaData;
	}
	
	public static void dump(PrintStream out, Resource resource) throws IOException {

		MetaData m = resource.getMetaData();

		out.println("Headers Before");
		out.print(m.toString());
		
		out.println("Resource Follows:\n===================");
		StreamCopy.copy(resource.getInputStream(),out);

		out.println("[\n]Headers After");
		out.print(m.toString());

	}
	public static void dumpShort(PrintStream out, Resource resource) throws IOException {

		MetaData m = resource.getMetaData();

//		out.println("Headers Before");
//		out.print(m.toString());
		
		long bytes = StreamCopy.copy(resource.getInputStream(), ByteStreams.nullOutputStream());
		out.println("Resource Was:"+bytes+" Long");

		out.println("[\n]Headers After");
		out.print(m.toString());

	}

}
