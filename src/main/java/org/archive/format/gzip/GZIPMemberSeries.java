package org.archive.format.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;

import org.archive.streamcontext.Stream;

/**
 * Class which encapsulates all the logic in reading multiple gzip members from
 * a single stream.
 * 
 * The class supports as it's main method, "nextMember()" which returns a
 * GZIPSeriesMember.
 * 
 * The class allows configuration as to the robustness, namely the handling of
 * errors in the underlying gzip stream.
 * 
 * Regardless of robustness - an IOException from the underlying Stream will
 * cause this class to throw an IOException whenever nextMember() is called.
 * 
 * In Strict mode, any GZ error within a member will cause the object to behave
 * as if an IOException was detected - nextMember() will repeatedly throw more 
 * IOExceptions.
 * 
 * In Lax mode, a GZ error in a single member will cause this class to attempt
 * to skip that failing member, and find the next gzip member in the series.
 * 
 * This class maintains the state of the underlying gzip Stream:
 * 
 * ) Deflating - a gzip header and some amount of deflate information has
 *               been read, without errors
 * ) IOError   - an IOException has been detected on the underlying Stream
 * ) Aligned   - the gzip footer of a record has *just* been read, and it is
 *               expected that the underlying Stream is either at EOF, or at the
 *               start of another gzip member. In Strict Mode this is the 
 *               initial state.
 * ) Scanning  - The underlying Stream is in an unknown state - either because
 *               of a GZ error in the previous member, and we're now attempting
 *               to locate the next member. In Lax Mode, this is the initial
 *               state.
 * 
 * 
 * The member returned by nextMember() is an InputStream, which also allows
 * access to information about the specific record, namely information in the
 * gzip header, as well as context information about the record within the
 * series: filename and offset. The Member also provides information about
 * the amount of compressed and uncompressed data read thus far.
 * 
 * If a gzip format exception is detected while in a read() call of the gzip
 * member, it will throw an IOException.GZIPFormatException to the caller.
 * 
 * when the end of the deflate stream is found in a read() call, the member
 * will also silently read the gzip footer, and check the length and CRC. A 
 * failure to read the footer, or a bad comparison between length or CRC will
 * cause a GZIPFormatException to be thrown on that read() call.
 * 
 * if nextMember() is called and the previous member has not been completely
 * read, this class will automatically attempt to skip the previous record. If
 * an error is encountered, the class either either throw an exception, or
 * attempt to find the next member in the series.
 * 
 * @author brad
 *
 */
public class GZIPMemberSeries extends InputStream implements GZIPConstants {
	
	private static Logger LOG = 
		Logger.getLogger(GZIPMemberSeries.class.getName());
	
	public static int STATE_DEFLATING = 0;
	public static int STATE_IOERROR   = 1;
	public static int STATE_ALIGNED   = 2;
	public static int STATE_SCANNING  = 3;
	public static int STATE_START  = 4;
//	public static int STATE_EOF  = 5;

	public int state = STATE_START;
	

	private String streamContext = null;
	private GZIPDecoder decoder = null;
	private GZIPHeader header = null;
	private static int BUF_SIZE = 4096;
	private Stream stream = null;
	private GZIPSeriesMember currentMember = null;
	private long currentMemberStartOffset = 0;
	private boolean strict = false;
	
	private boolean gotEOF = false;
	private boolean gotIOError = false;
	private byte buffer[] = null;
	private byte singleByteRead[] = null;
	private int bufferPos = 0;
	private int bufferSize = 0;
	private long offset = 0;

	public GZIPMemberSeries(Stream bis) {
		this(bis,"unknown");
	}
	public GZIPMemberSeries(Stream bis, String context) {
		this(bis,context,0L,true);
	}
	public GZIPMemberSeries(Stream bis, String context, long offset) {
		this(bis,context,offset,true);
	}
	public GZIPMemberSeries(Stream bis, String context, long offset, boolean strict) {
		decoder = new GZIPDecoder();
		this.stream = bis;
		this.strict = strict;
		if(offset == 0) {
			state = strict ? STATE_ALIGNED : STATE_START;
		} else {
			state = STATE_START;
		}
		buffer = new byte[BUF_SIZE];
		singleByteRead = new byte[1];
		currentMember = null;
		gotEOF = false;
		gotIOError = false;
		header = null;
		streamContext = context;
		this.offset = offset;
	}

	public void close() throws IOException {
		stream.close();
		gotEOF = true;
	}

	public boolean gotEOF()            { return gotEOF;        }
	public boolean gotIOError()            { return gotIOError;        }
	public String getStreamContext()   { return streamContext; }
	public long getCurrentMemberStartOffset() { return currentMemberStartOffset; }
	public long getOffset()            { return offset;        }

	public void noteEndOfRecord() throws IOException {
		if(state != STATE_DEFLATING) {
			gotIOError = true;
			throw new IOException("noteEndOfRecord while not deflating at " 
					+ currentMemberStartOffset + " in " + streamContext);
		}
		state = STATE_ALIGNED;
	}

	public void noteGZError() throws IOException {
		LOG.info("noteGZError");
		if(strict) {
			gotIOError = true;
			state = STATE_IOERROR;
			throw new IOException("Internal GZIPFormatException " 
					+ currentMemberStartOffset + " in " + streamContext );
		}
		state = STATE_SCANNING;
		
//		if(state == STATE_DEFLATING) {
//			state = STATE_SCANNING;
//		} else if (state == STATE_ALIGNED) {
//			LOG.info("noteGZError - already aligned - should be CRC/LEN error");
//			// we got the error in the footer - still aligned..
//		} else {
//			gotIOError = true;
//			throw new IOException("noteGZErrror while not deflating or at EOR");
//		}
	}
	
	public GZIPSeriesMember getNextMember() throws GZIPFormatException, IOException {
		if(state == STATE_IOERROR) {
			throw new IOException("getNextMember() on IOException Stream at "
					+ currentMemberStartOffset + " in " + streamContext);
		}
		LOG.info("getNextMember");

		if(gotEOF) {
			LOG.info("getNextMember-ATEOF");
			return null;
		}
		if(state == STATE_DEFLATING) {
			LOG.info("getNextMember-without complete read - finishing current");
			// currentMember better not be null...
			try {
				currentMember.skipMember();
				LOG.info("Skipped unfinished member");
			} catch(GZIPFormatException e) {
				// TODO: log this... state should be STATE_UNALIGNED...
				LOG.info("GZIPFormatException on skipMember()");
				if(strict) {
					throw new IOException("GZIPFormatException at " + offset 
							+ " in " + streamContext);
				}
				// state is now STATE_SCANNING
			}
		} else if(state == STATE_SCANNING) {
			// We had a gzip error with the previous record:
			// Need to move the underlying Stream back to 3 bytes after the last
			// member start:
			LOG.warning("getNextMember() called when scanning - starting from "
					+ (currentMemberStartOffset + 3));
			offset = currentMemberStartOffset + 3;
			bufferSize = 0;
			bufferPos = 0;
			stream.setOffset(currentMemberStartOffset + 3);
		}
		currentMember = null;

		while(currentMember == null) {
			// scan ahead for another record start:
			long amtSkipped = decoder.alignOnMagic3(this);
			if(LOG.isLoggable(Level.INFO)) {

				LOG.info("AlignedResult:" + amtSkipped);
			}
			if(amtSkipped < 0) {
				gotEOF = true;
				if(decoder.alignedAtEOF(amtSkipped)) {
					LOG.info("CleanEOF");
					// a clean EOF when expected:
					return null;
				} else {
					if(strict) {
						throw new GZIPFormatException("Trailing bytes did not" +
								"contain a valid gzip member file: " 
								+ streamContext + " offset: " 
								+ currentMemberStartOffset);
					}
					if(LOG.isLoggable(Level.INFO)) {

						LOG.info(String.format(Locale.ROOT,
							"Got EOF after %d bytes before finding magic in %s\n",
							amtSkipped * -1, streamContext));
					}
					return null;
				}
			}
			if(amtSkipped > 0) {
				if(strict) {
					if(state == STATE_START) {
						LOG.info(String.format(Locale.ROOT,
								"Strict mode Skipped %d bytes in (%s) before finding magic at offset(%d)\n",
								amtSkipped, streamContext, offset-3));
					} else {
						throw new GZIPFormatException("Not aligned at gzip start: "
								+ streamContext + " at offset " +
								(offset-3));						
					}
				}
				if(LOG.isLoggable(Level.INFO)) {

					LOG.info(String.format(Locale.ROOT,
						"Skipped %d bytes in (%s) before finding magic at offset(%d)\n",
						amtSkipped, streamContext, offset-3));
				}
			}
			try {
				currentMemberStartOffset = offset - 3;
				header = decoder.parseHeader(this, true);
				LOG.info("Read next GZip header...");
				currentMember = new GZIPSeriesMember(this,header);
				state = STATE_DEFLATING;
				
			} catch (GZIPFormatException e) {
				if(strict) {
					gotIOError = true;
					throw new IOException(e + " at " + offset + " in " 
							+ streamContext);
				}
				offset = currentMemberStartOffset + 3;
				stream.setOffset(currentMemberStartOffset + 3);
				LOG.warning(String.format(Locale.ROOT,
						"GZIPFormatException with record around offset(%d) in (%s)\n",
						offset, streamContext));
			}
		}
		return currentMember;
	}

	public int read() throws IOException {
		int amt = read(singleByteRead, 0, 1);
		if (amt == -1) {
			return -1;
		}
		return singleByteRead[0] & 0xff;
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int amtWritten = 0;
		if(LOG.isLoggable(Level.INFO)) {
			LOG.info("read("+len+" bytes) bufferSize("+bufferSize+")");
		}
		while(len > 0) {
			if(bufferSize > 0) {
				int amtToCopy = Math.min(len, bufferSize);
				
				System.arraycopy(buffer, bufferPos, b, off, amtToCopy);
				bufferPos += amtToCopy;
				bufferSize -= amtToCopy;
				off += amtToCopy;
				len -= amtToCopy;
				amtWritten += amtToCopy;
				offset += amtToCopy;
			} else {
				if(!fillBuffer()) {
					break;
				}
			}
		}
		if(amtWritten == 0) {
			return -1;
		}
		return amtWritten;
	}

	private boolean fillBuffer() throws IOException {
		try {
			int amtRead = stream.read(buffer,0,buffer.length);
			if(LOG.isLoggable(Level.FINE)) {
				LOG.fine("Underlying Stream read("+amtRead+") bytes");
			}
			if(amtRead == -1) {
				gotEOF = true;
				return false;
			}
			bufferPos = 0;
			bufferSize += amtRead;

		} catch(IOException e) {
			gotIOError = true;
			throw e;
		}
		return true;
	}

	public void returnBytes(int bytes) {
		if((bytes > bufferPos) || (bytes < 0)) {
			throw new IndexOutOfBoundsException();
		}
		if(LOG.isLoggable(Level.INFO)) {
			LOG.info("Returned ("+bytes+")bytes");
		}
		bufferPos -= bytes;
		bufferSize += bytes;
		offset -= bytes;
	}

	public int fillInflater(Inflater inflater) throws IOException {
		// Makes sure we're expecting this call:
		if(state != STATE_DEFLATING) {
			throw new IOException("fillInflater called while not deflating!");
		}
		if(bufferSize <= 0) {
			if(!fillBuffer()) {
				return -1;
			}
		}
		inflater.setInput(buffer, bufferPos, bufferSize);
		bufferPos += bufferSize;
		offset += bufferSize;
		int oldSize = bufferSize;
		bufferSize = 0;
		return oldSize;
	}
	
	/**
	 * @return the strict
	 */
	public boolean isStrict() {
		return strict;
	}
	/**
	 * @param strict the strict to set
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}
}
