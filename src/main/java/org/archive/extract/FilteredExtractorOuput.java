package org.archive.extract;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.archive.format.json.JSONUtils;
import org.archive.resource.Resource;
import org.archive.util.StreamCopy;

public class FilteredExtractorOuput implements ExtractorOutput {
	private String filterPath;
	private PrintStream out;
	public FilteredExtractorOuput(PrintStream out, String filterPath) {
		this.filterPath = filterPath;
		this.out = out;
	}
	public void output(Resource resource) throws IOException {
		StreamCopy.readToEOF(resource.getInputStream());
		List<String> results = JSONUtils.extractFancy(resource.getMetaData().getTopMetaData(), filterPath);
		if(results != null) {
			for(String result: results) {
				out.println("Result: " + result);
			}
		}
	}
	public void output2(Resource resource) throws IOException {
		String result = JSONUtils.extractSingle(resource.getMetaData().getTopMetaData(), filterPath);
		if(result != null) {
			out.println("Result:" + result);
		}
	}

}
