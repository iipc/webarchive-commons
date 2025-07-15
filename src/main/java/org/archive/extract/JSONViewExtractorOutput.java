package org.archive.extract;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.archive.format.json.JSONView;
import org.archive.resource.Resource;
import org.archive.util.StreamCopy;

public class JSONViewExtractorOutput implements ExtractorOutput {
	private PrintStream out;
	private JSONView view;
	public JSONViewExtractorOutput(OutputStream out, String filterPath) {
		view = new JSONView(filterPath.split(","));
		this.out = new PrintStream(out);
	}
	public void output(Resource resource) throws IOException {
		StreamCopy.readToEOF(resource.getInputStream());
		List<List<String>> data = 
			view.apply(resource.getMetaData().getTopMetaData());
		if(data != null) {
			for(List<String> d : data) {
				out.println(StringUtils.join(d,"\t"));
			}
		}
	}
}
