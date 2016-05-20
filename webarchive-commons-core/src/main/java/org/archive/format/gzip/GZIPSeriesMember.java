package org.archive.format.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


public class GZIPSeriesMember extends InputStream implements GZIPConstants {

	private static Logger LOG = 
		Logger.getLogger(GZIPSeriesMember.class.getName());
	
	private GZIPMemberSeries series = null;
	private GZIPHeader header = null;
	private GZIPFooter footer = null;
	private boolean gotIOError = false;
	private boolean gotGZError = false;
	private boolean gotEOR = false;

	private Inflater inflater = null;
	private CRC32 crc = null;

	public GZIPSeriesMember(GZIPMemberSeries series, GZIPHeader header) {
		this.series = series;
		this.header = header;
		this.footer = null;
		this.inflater = new Inflater(true);
		this.crc = new CRC32();
		gotIOError = false;
		gotGZError = false;
		gotEOR = false;
	}

	public GZIPFooter getFooter()        { return footer;                    }
	public GZIPHeader getHeader()        { return header;                    }
	public long getRecordStartOffset()   { return series.getCurrentMemberStartOffset();               }
	public String getRecordFileContext() { return series.getStreamContext(); }

	public boolean gotEOR()            { return gotEOR;       }
	public boolean gotIOError()        { return gotIOError;   }
	public boolean gotGZipError()      { return gotGZError;   }

	public long getUncompressedBytesRead() {
		return inflater.getBytesWritten();
	}
	public long getCompressedBytesRead() {
		long amtRead = header.getLength() + inflater.getBytesRead();
		if(gotEOR) {
			amtRead += GZIP_STATIC_FOOTER_SIZE;
		}
		return amtRead;
	}

	public void skipMember() throws IOException {
		skip(Long.MAX_VALUE);
	}

	/*
	 * 
	 *    ALL InputStream overrides below here:
	 * 
	 */

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int totalRead = 0;
		if(gotIOError) {
			throw new IOException("Repeated read() on IOException!");
		}
		if(gotGZError) {
			throw new GZIPFormatException("Repeated read() on " +
					"GZIPFormatException");
		}
		if (gotEOR) {
			return -1;
		}

		while ((totalRead < len) && !gotEOR) {
			if (inflater.needsInput()) {
				// TODO: Catch, record, re-throw IOException
				int amtRead;
				try {
					amtRead = series.fillInflater(inflater);
				} catch(IOException e) {
					gotIOError = true;
					throw e;
				}
				if (amtRead == -1) {
					LOG.warning("At end of file without inflate done...");
					gotGZError = true;
					throw new GZIPFormatException(
							"At end of file without inflate done...");
				}
			}
			int amtInflated;
			try {
				amtInflated = 
					inflater.inflate(b, off + totalRead, len - totalRead);
				
			} catch (DataFormatException e) {
				LOG.warning("GOT GZ-DATAFORMATERROR");
				gotGZError = true;
				series.noteGZError();
				// TODO: record GZError on Series
				throw new GZIPFormatException(e);
			}
			boolean finished = inflater.finished();

			crc.update(b, off + totalRead, amtInflated);
			totalRead += amtInflated;
			if (finished) {

				series.returnBytes(inflater.getRemaining());
				// read the footer:
				byte[] footerBuffer = new byte[GZIP_STATIC_FOOTER_SIZE];
				int footerBytes = series.read(footerBuffer, 0, 
						footerBuffer.length);
				if(footerBytes != GZIP_STATIC_FOOTER_SIZE) {
					gotGZError = true;
					series.noteGZError();
					throw new GZIPFormatException("short footer");
				}
				gotEOR = true;
				series.noteEndOfRecord();
				try {
					GZIPFooter tmpFooter = new GZIPFooter(footerBuffer);
					tmpFooter.verify(crc.getValue(), inflater.getTotalOut());
					footer = tmpFooter;
				} catch (GZIPFormatException e) {
					gotGZError = true;
					series.noteGZError();
					throw e;
				}
				if (totalRead == 0) {
					// zero length compressed doc...
					totalRead = -1;
				}
			}
		}
		return totalRead;
	}

	@Override
	public int available() throws IOException {
		if(gotEOR) {
			return 0;
		}
		return inflater.needsInput() ? 0 : 1;
	}

	@Override
	public void close() throws IOException {
		skipMember();
	}

	@Override
	public synchronized void mark(int readlimit) {
		// no-op
	}
	@Override
	public boolean markSupported() {
		return false;
	}
	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("reset() not supported");
	}

	@Override
	public int read() throws IOException {
		byte b[] = new byte[1];
		int amt = read(b, 0, 1);
		if (amt == -1) {
			return -1;
		}
		return b[0] & 0xff;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public long skip(long amt) throws IOException {
		long skipped = 0;
		// TODO: put somewhere more sensible
		int SKIP_LENGTH = 1024 * 4;
		byte b[] = new byte[SKIP_LENGTH];
		while(amt > 0) {
			int r = read(b,0,b.length);
			if(r == -1) {
				break;
			}
			skipped += r;
			amt -= r;
		}
		return skipped;
	}
}
