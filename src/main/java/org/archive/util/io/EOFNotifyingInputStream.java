package org.archive.util.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EOFNotifyingInputStream extends FilterInputStream {
	EOFObserver observer;
	boolean notified = false;
	public EOFNotifyingInputStream(InputStream in, EOFObserver observer) {
		super(in);
		this.observer = observer;
	}
	private void doNotify() throws IOException {
		if(!notified) {
			notified = true;
			if(observer != null) {
				observer.notifyEOF();
			}
		}
	}
	
	@Override
	public int read() throws IOException {
		int amtRead = super.read();
		if(amtRead == -1) {
			doNotify();
		}
		return amtRead;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b,0,b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int amtRead = super.read(b, off, len);
		if(amtRead == -1) {
			doNotify();
		}
		return amtRead;
	}
}
