package org.archive.hadoop.jobs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.archive.url.DefaultIAURLCanonicalizer;
import org.archive.url.HandyURL;
import org.archive.url.NonMassagingIAURLCanonicalizer;
import org.archive.url.URLCanonicalizer;
import org.archive.url.URLParser;
import org.archive.url.URLRegexTransformer;

public class CDXTransformer {
	private final static Logger LOG = 
		Logger.getLogger(CDXTransformer.class.getCanonicalName());
	
	private PrintWriter out;
	private char delim = ' ';
	private URLCanonicalizer can = new DefaultIAURLCanonicalizer();
	public CDXTransformer(PrintWriter out) {
		this.out = out;
	}
	private final static Pattern SPACE_PATTERN = Pattern.compile(" ");
	
	public static void main(String args[]) throws IOException {
		String line;
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
		PrintWriter pw = new PrintWriter(bw);
		CDXTransformer t = new CDXTransformer(pw);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		if(args.length > 0) {
			if(args[0].equals("--no-massage")) {
				t.setCan(new NonMassagingIAURLCanonicalizer());
			}
		}
		while(true) {
			line = br.readLine();
			if(line == null) {
				break;
			}
			t.output(line);
		}
		pw.flush();
	}
	
	public void output(String cdxIn) {
		// assume fields: N b a m s k r V g
		// url-key (old)
		// timestamp
		// orig-url
		// mime
		// http-code
		// digest
		// redirect
		// (OPTIONAL robot-flag)
		// start-offset
		// filename
		String parts[] = SPACE_PATTERN.split(cdxIn, 11);
		int offsetIdx = -1;
		if(parts.length == 9) {
			offsetIdx = 7;
		} else if(parts.length == 10) {
			if(parts[7].contains("A")) {
				// NO-ARCHIVE HTML meta instruction
				return;
			}
			offsetIdx = 8;
		} else {
			LOG.warning("Bad format line:\t" + cdxIn);
			return;
		}
//		String urlKey = parts[0];
		String captureTS = parts[1];
		String originalUrl = parts[2];
		String mimeType = parts[3];
		String httpCode = parts[4];
		String digest = parts[5];
		String redirectUrl = parts[6];
		long compressedOffset = -1;
		try {
			compressedOffset = Long.parseLong(parts[offsetIdx]);
		} catch (NumberFormatException e) {
			LOG.warning("Bad compressed Offset field("+parts[offsetIdx]+") in (" +
					cdxIn +")");
			return;
		}
		String filename = parts[offsetIdx+1];
		HandyURL h;
		try {
			h = URLParser.parse(originalUrl);
		} catch (URIException e) {
			LOG.warning(String.format("Bad original URL(%s) error(%s)",
					originalUrl,e.getMessage()));
			return;
		}
		can.canonicalize(h);
		
//		StringBuilder sb = new StringBuilder(cdxIn.length() + 5);
//		sb.append(h.getPublicSuffix()).append(delim);
//		sb.append(h.getPathQuery()).append(delim);
//		sb.append(captureTS).append(delim);
//		
//		sb.append(nullToDash(h.getPublicPrefix())).append(delim);
//		sb.append(nullToDash(h.getScheme())).append(delim);
//		sb.append(originalUrl).append(delim);
//		sb.append(nullToDash(mimeType)).append(delim);
//		sb.append(nullToDash(httpCode)).append(delim);
//		sb.append(nullToDash(digest)).append(delim);
//		sb.append(nullToDash(redirectUrl)).append(delim);
//		sb.append(compressedOffset).append(delim);
//		sb.append(nullToDash(filename));

//		StringBuilder sb = new StringBuilder(cdxIn.length() + 5);
		out.print("(");
		out.print(URLRegexTransformer.hostToSURT(h.getPublicSuffix()));
		out.print(delim);

		out.print(h.getPathQuery()); out.print(delim);
		out.print(captureTS); out.print(delim);
		
		out.print(nullToDash(h.getPublicPrefix())); out.print(delim);
		out.print(nullToDash(h.getScheme())); out.print(delim);
		out.print(originalUrl); out.print(delim);
		out.print(nullToDash(mimeType)); out.print(delim);
		out.print(nullToDash(httpCode)); out.print(delim);
		out.print(nullToDash(digest)); out.print(delim);
		out.print(nullToDash(redirectUrl)); out.print(delim);
		out.print(compressedOffset); out.print(delim);
		out.print(nullToDash(filename)); ; out.println();
	}
	private static final String nullToDash(String in) {
		return ((in == null) || (in.length() == 0)) ? "-" : in;
	}

	/**
	 * @return the can
	 */
	public URLCanonicalizer getCan() {
		return can;
	}

	/**
	 * @param can the can to set
	 */
	public void setCan(URLCanonicalizer can) {
		this.can = can;
	}
}
