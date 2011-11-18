package org.archive.util.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class CommitedOutputStream extends FilterOutputStream {
	public CommitedOutputStream(OutputStream arg0) {
		super(arg0);
	}
	public abstract void commit() throws IOException;
}
