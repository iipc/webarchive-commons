package org.archive.extract;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.archive.format.json.JSONView;
import org.archive.resource.Resource;
import org.archive.util.StreamCopy;

public class CDXExtractorOutput implements ExtractorOutput {
	// CANON DATE URL MIME HTTP-CODE SHA1 REDIR OFFSET FILE
	private static String URL_SPEC = "Envelope.ARC-Header-Metadata.Target-URI|Envelope.WARC-Header-Metadata.Target-URI";
	private static String DATE_SPEC = "Envelope.ARC-Header-Metadata.Date";
	private static String MIME_SPEC = "Envelope.ARC-Header-Metadata.Content-Type";
	private static String HTTP_CODE_SPEC = "Envelope.Payload-Metadata.HTTP-Response-Metadata.Response-Message.Status";
	private static String SHA1_SPEC = "Envelope.Payload-Metadata.HTTP-Response-Metadata.Entity-Digest";
	private static String REDIR_SPEC = "Envelope.Payload-Metadata.HTTP-Response-Metadata.Headers.Content-Location";
	private static String OFFSET_SPEC = "Container.Offset";
	private static String FILENAME_SPEC = "Container.Filename";
	private static String SPECS[] = {
		URL_SPEC, DATE_SPEC, URL_SPEC, MIME_SPEC, HTTP_CODE_SPEC, SHA1_SPEC, 
		REDIR_SPEC, OFFSET_SPEC, FILENAME_SPEC
	};
	private static char EMPTY = '-';
	private static char DELIM = ' ';
	JSONView view;
	private PrintStream out;
	public CDXExtractorOutput(PrintStream out) {
		view = new JSONView(SPECS);
		this.out = out;
	}
	public void output(Resource resource) throws IOException {
		StreamCopy.readToEOF(resource.getInputStream());
		List<List<String>> res = view.apply(resource.getMetaData().getTopMetaData());
		StringBuilder sb = new StringBuilder();
		for(List<String> actual : res) {
			sb.setLength(0);
//			boolean first = true;
			for(int i = 0; i < actual.size(); i++) {
//			actual.set(5, actual.get(5).substring(5));
//			for(String f : actual) {
				if(i > 0) {
					sb.append(DELIM);
				}
				String f = actual.get(i);
				if((f == null) || (f.length() == 0)) {
					sb.append(EMPTY);
				} else {
					if(i == 5) {
						sb.append(f.substring(5));
					} else {
						sb.append(f);						
					}
				}
			}
			out.println(sb.toString());
		}
	}
}
