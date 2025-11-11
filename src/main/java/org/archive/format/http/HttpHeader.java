package org.archive.format.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public class HttpHeader implements HttpConstants {
	private String name = null;
	private String value = null;

	public HttpHeader() {}

	public HttpHeader(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName()              { return name;        }
	public void   setName(String name)   { this.name = name;   }
	public String getValue()             { return value;       }
	public void   setValue(String value) { this.value = value; }

	public void write(OutputStream out) throws IOException {
		out.write(name.getBytes(UTF8));  out.write(COLON); out.write(SP);

		out.write(value.getBytes(UTF8)); out.write(CR);    out.write(LF);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(name.length() + value.length()+20);
		sb.append(String.format(Locale.ROOT, "HttpHeader(%s)(%s)",name,value));
		return sb.toString();
	}
}
