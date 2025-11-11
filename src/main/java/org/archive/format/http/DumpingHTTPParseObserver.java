package org.archive.format.http;

import java.io.PrintStream;
import java.util.Locale;


public class DumpingHTTPParseObserver implements HttpHeaderObserver {
	private PrintStream ps = null;
	public DumpingHTTPParseObserver() {
		ps = System.out;
	}
	public DumpingHTTPParseObserver(PrintStream ps) {
		this.ps = ps;
	}

	public void headerParsed(byte[] name, int ns, int nl, byte[] value, int vs,
			int vl) {
		ps.format(Locale.ROOT,"headerParsed:(%d:%d)(%s)(%d:%d)(%s)\n", 
				ns,nl,new String(name,0,nl,UTF8),
				vs,vl,new String(value,0,vl,UTF8));
	}

	public void headersComplete(int bytesRead) {
		ps.format(Locale.ROOT,"headersComplete(%d)\n",bytesRead);
	}
	public void headersCorrupt() {
		ps.println("headersCorrupted\n");
	}

}
