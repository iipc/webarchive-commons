package org.archive.util.io;

import java.io.IOException;
import java.io.InputStream;

public class NotifyingInputStream extends InputStream {
	InputStream wrapped;
	BytesReadObserver observer;
	public NotifyingInputStream(InputStream wrapped, 
			BytesReadObserver observer) {
		this.wrapped = wrapped;
		this.observer = observer;
	}
	private int notifyRead(int amt) {
		if(amt > 0) {
			observer.notifyBytesRead(amt);
		}
		return amt;
	}
	@Override
	public int read() throws IOException {
		return notifyRead(wrapped.read());
	}
	@Override
	public int read(byte[] b) throws IOException {
		return notifyRead(wrapped.read(b));
	}
	@Override
	public int read(byte[] b, int o, int l) throws IOException {
		return notifyRead(wrapped.read(b,o,l));
	}
}
