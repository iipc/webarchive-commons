package org.archive.format.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

import org.archive.util.ByteOp;
import org.archive.util.io.CRCInputStream;


public class GZIPDecoder implements GZIPConstants {
	public static final long SEARCH_EOF_AT_START = Long.MIN_VALUE;
	private int maxNameSize = 1024;
	private int maxCommentSize = 1024;
	public GZIPDecoder() {
	}
	public GZIPDecoder(int max) {
		maxNameSize = max;
		maxCommentSize = max;
	}
	public GZIPDecoder(int maxNameSize, int maxCommentSize) {
		this.maxNameSize = maxNameSize;
		this.maxCommentSize = maxCommentSize;
	}
	public boolean alignedAtEOF(long skipped) {
		return skipped == SEARCH_EOF_AT_START;
	}

	/**
	 * Read bytes from InputStream argument until 3 bytes are found that appear
	 * to be the start of a GZIPHeader. leave the stream on the 4th byte,
	 * and return the number of bytes skipped before finding the 3 bytes.
	 * 
	 * @param is InputStream to read from
	 * @return number of bytes skipped before finding the gzip magic: 0 if the
	 * first 3 bytes matched. If no magic was found before an EOF, returns the
	 * -1 * the number of bytes skipped before hitting the EOF. As a special
	 * case, if the stream was at EOF when the method is called, returns
	 * GZIPHeaderParser.SEARCH_EOF_AT_START (which is Long.MIN_VALUE)
	 * 
	 * @throws IOException
	 */
	public long alignOnMagic3(InputStream is) throws IOException {
		
		long bytesSkipped = 0;
		byte lookahead[] = new byte[3];
		int keep = 0;
		while(true) {
			if(keep == 2) {
				lookahead[0] = lookahead[1];
				lookahead[1] = lookahead[2];
			} else if(keep == 1) {
				lookahead[0] = lookahead[2];
			}
			
			int amt = is.read(lookahead, keep, 3 - keep);
			if(amt == -1) {
				long skippedBeforeEOF = bytesSkipped + keep;
				if(skippedBeforeEOF == 0) {
					return SEARCH_EOF_AT_START;
				}
				return -1 * skippedBeforeEOF;
			}
			// TODO: handle read < # of bytes wanted...

			// we have 3 bytes, can it be a gzipmember?
			// Legend:
			//   ? = uninspected byte
			//   1 = gzip magic 1
			//   2 = gzip magic 2
			//   ! = wrong byte value
			
			// ???
			if(lookahead[0] != GZIP_MAGIC_ONE) {
				// !??
				// nope. are the next 2 possibilities?
				if((lookahead[1] == GZIP_MAGIC_ONE) &&
					(lookahead[2] == GZIP_MAGIC_TWO)) {
					// !12
					keep = 2;
				} else if(lookahead[2] == GZIP_MAGIC_ONE) {
					// !!1
					keep = 1;

				} else {
					// !!!
					keep = 0;
				}
				bytesSkipped += (3-keep);
				continue;
			}
			// 1??
			if((lookahead[1] & 0xff) != GZIP_MAGIC_TWO) {
				// 1!?
				// nope. is the last a possible start?
				if(lookahead[2] == GZIP_MAGIC_ONE) {
					// 1!1
					keep = 1;
				} else {
					// 1!!
					// just keep lookin, no backtrack
					keep = 0;
				}
				bytesSkipped += (3-keep);
				continue;
			}
			// 12?
			if(!GZIPHeader.isValidCompressionMethod(lookahead[2])) {
				if(lookahead[2] == GZIP_MAGIC_ONE) {
					// 121
					keep = 1;
				} else {
					// 12!
					// just keep lookin, no backtrack
				}
				bytesSkipped += (3-keep);
				continue;
			}
			// found it!
			return bytesSkipped;
		}
	}

	public GZIPHeader parseHeader(InputStream origIn) 
	throws GZIPFormatException, IOException {
		return parseHeader(origIn,false);
	}

	public GZIPHeader parseHeader(InputStream origIn, boolean assume1st3) 
		throws GZIPFormatException, IOException {
		
		GZIPHeader header = null;
		CRCInputStream is = null;
		GZIPStaticHeader staticHeader = null;
		// wrap in CRC in case header has crc flag:
		if(assume1st3) {

			CRC32 crc = new CRC32();
			crc.update(GZIPStaticHeader.DEFAULT_HEADER_DATA,0,3);
			is = new CRCInputStream(origIn,crc);
			staticHeader = new GZIPStaticHeader(is,true);

		} else {

			is = new CRCInputStream(origIn);
			staticHeader = new GZIPStaticHeader(is);
		}		
		header = new GZIPHeader(staticHeader);

		if(staticHeader.isFExtraSet()) {
			header.records = new GZIPFExtraRecords(is);
		}
		// FNAME:
		if(staticHeader.isFNameSet()) {
			if(maxNameSize > 0) {
				header.fileName = ByteOp.readToNull(is,maxNameSize);
				header.fileNameLength = header.fileName.length;
			} else {
				header.fileName = null;
				header.fileNameLength = ByteOp.discardToNull(is);
			}
		}
		// FCOMMENT:
		if(staticHeader.isFCommentSet()) {
			if(maxCommentSize > 0) {
				// TODO: if maxsize is too small, this throws IOException
				//       which will do bad things to our parse up the foodchain
				header.comment = ByteOp.readToNull(is,maxCommentSize);
				header.commentLength = header.comment.length;
			} else {
				header.comment = null;
				header.commentLength = ByteOp.discardToNull(is);
			}
		}
		if(staticHeader.isFHCRCSet()) {
			header.crc = ByteOp.readShort(is);
			int wantCRC16 = (int) (is.getCRCValue() & 0xffff);
			if(wantCRC16 != header.crc) {
				throw new GZIPFormatException("HEADER CRC ERROR");
			}
		}
		return header;
	}
}
