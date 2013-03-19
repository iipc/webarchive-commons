package org.archive.format.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.archive.util.ByteOp;

public class GZIPFExtraRecords extends ArrayList<GZIPFExtraRecord>{
	/** */
	private static final long serialVersionUID = -6250727937366358585L;
	public GZIPFExtraRecords() {
	}

	public GZIPFExtraRecords(InputStream is) throws GZIPFormatException, IOException {
		readRecords(is);
	}
	
	public void writeTo(OutputStream os) throws IOException {
		int bytes = 0;
		for(GZIPFExtraRecord record : this) {
			bytes += record.length();
		}
		ByteOp.writeShort(os, bytes);
		for(GZIPFExtraRecord record : this) {
			record.writeTo(os);
		}
	}
	
	/**
	 * @return the number of bytes used by these records. Includes:
	 *    records-length : 2-byte short
	 *    record-data    : summation of sizes of records
	 */
	public int getByteLength() {
		int bytes = 2; // 2 for length
		for(GZIPFExtraRecord record : this) {
			bytes += record.length();
		}
		return bytes;
	}
	
	public void readRecords(InputStream is) 
	throws GZIPFormatException, IOException {

		this.clear();
		int bytesRemaining = -1;
		bytesRemaining = ByteOp.readShort(is);
		if(bytesRemaining < 0) {
			throw new GZIPFormatException("Negative FExtra length");
		}
		ArrayList<GZIPFExtraRecord> tmpList = new ArrayList<GZIPFExtraRecord>();
		while(bytesRemaining > 0) {
			GZIPFExtraRecord tmpRecord = new GZIPFExtraRecord();
			int bytesRead = tmpRecord.read(is);
			bytesRemaining -= bytesRead;
			if(bytesRemaining < 0) {
				throw new GZIPFormatException("Invalid FExtra length/records");
			}
			tmpList.add(tmpRecord);
		}
		this.addAll(tmpList);
	}
}
