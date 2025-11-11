package org.archive.streamcontext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

public class HTTP11Stream extends AbstractBufferingStream {
	private URL url;
	private URLConnection conn = null;
	private InputStream is = null;

	public HTTP11Stream(URL url)
		throws IndexOutOfBoundsException, FileNotFoundException, IOException {
		this(url,0L,DEFAULT_READ_SIZE);
	}	
	public HTTP11Stream(URL url, long offset)
		throws IndexOutOfBoundsException, FileNotFoundException, IOException {
		this(url,offset,DEFAULT_READ_SIZE);
	}	
	public HTTP11Stream(URL url, long offset, int readSize) throws IOException {
		super(offset,readSize);
		this.url = url;
		doSeek(offset);
	}

	@Override
	public void doClose() throws IOException {
		if(is != null) {
			is.close();
			is = null;
		}
	}

	@Override
	public int doRead(byte[] b, int off, int len) throws IOException {
		return is.read(b, off, len);
	}

	@Override
	public void doSeek(long offset) throws IOException {
		doClose();
		conn = url.openConnection();
		conn.setRequestProperty("Range", String.format(Locale.ROOT, "bytes=%d-", offset));
		conn.connect();
		is = conn.getInputStream();
	}
}
