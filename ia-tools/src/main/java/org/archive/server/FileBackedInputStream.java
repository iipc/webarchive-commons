package org.archive.server;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.FileBackedOutputStream;

public class FileBackedInputStream extends FilterInputStream {

	FileBackedOutputStream backer;
	
	public static int MAX_RAM = 1024 * 1024 * 2;

	protected FileBackedInputStream(InputStream in) {
		super(in);
		backer = new FileBackedOutputStream(MAX_RAM,false);
	}
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {
		return false;
	}
	public InputStream getInputStream() throws IOException {
		return backer.getSupplier().getInput();
	}
	
	public void resetBacker() throws IOException {
		backer.reset();
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int amt = super.read(b, off, len);
		if(amt != -1) {
			backer.write(b,off,amt);
		}
		return amt;
	}

	@Override
	public int read() throws IOException {
		int r = super.read();
		if(r != -1) {
			backer.write(r);
		}
		return r;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b,0,b.length);
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new UnsupportedOperationException("no reset!");
	}
}
