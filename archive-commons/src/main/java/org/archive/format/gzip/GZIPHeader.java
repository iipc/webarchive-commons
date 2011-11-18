package org.archive.format.gzip;

import java.io.IOException;
import java.io.OutputStream;

import org.archive.util.ByteOp;
import org.archive.util.io.CRCOutputStream;

public class GZIPHeader implements GZIPConstants {


	GZIPStaticHeader staticHeader = null;
	GZIPFExtraRecords records = null;
	byte fileName[] = null;
	int fileNameLength = -1;
	byte comment[] = null;
	int commentLength = -1;
	int crc = -1;

	/*
     * +----+----+----+----+----+----+----+----+----+----+----+----+
     * |ID1 |ID2 | CM |FLG |       MTIME       |XFL | OS | (more-->)
     * +----+----+----+----+----+----+----+----+----+----+----+----+
     * (if FLG.FEXTRA set)
     * +---+---+=================================+
     * | XLEN  |...XLEN bytes of "extra field"...| (more-->)
     * +---+---+=================================+
     * ("extra field" is then composed of a series of)
	 *         +---+---+---+---+==================================+
     *         |SI1|SI2|  LEN  |... LEN bytes of subfield data ...|
     *         +---+---+---+---+==================================+
     * (if FLG.FNAME set)
     * +=========================================+
     * |...original file name, zero-terminated...| (more-->)
     * +=========================================+
     * (if FLG.FCOMMENT set)
     * +===================================+
     * |...file comment, zero-terminated...| (more-->)
     * +===================================+
     * (if FLG.FHCRC set)
     * +---+---+
     * | CRC16 |
     * +---+---+
     */


	public GZIPHeader() {
		staticHeader = new GZIPStaticHeader();
		records = null;
		fileName = null;
		fileNameLength = 0;
		comment = null;
		commentLength = 0;
		crc = -1;
	}

	public GZIPHeader(GZIPStaticHeader staticHeader) {
		this.staticHeader = staticHeader;
		records = null;
		fileName = null;
		fileNameLength = 0;
		comment = null;
		commentLength = 0;
		crc = -1;
	}
	public GZIPStaticHeader getStaticHeader() {
		return staticHeader;
	}
	public byte[] getFileName() {
		return fileName;
	}
	public byte[] getComment() {
		return comment;
	}
	public long getFNameLength() {
		return fileNameLength;
	}
	public int getHeaderCRC() {
		return crc;
	}
	public void setFName(byte fileName[]) {
		if(fileName != null) {
			this.fileName = ByteOp.copy(fileName);
			staticHeader.setFNameFlag(true);
			fileNameLength = fileName.length;
		} else {
			this.fileName = null;
			staticHeader.setFNameFlag(false);
			fileNameLength = 0;
		}
		crc = -1;
	}

	public long getCommentLength() {
		return commentLength;
	}
	public void setFComment(byte comment[]) {
		if(comment != null) {
			this.comment = ByteOp.copy(comment);
			staticHeader.setFCommentFlag(true);
			commentLength = comment.length;
		} else {
			this.comment = null;
			staticHeader.setFCommentFlag(false);
			commentLength = 0;
		}
		crc = -1;
	}

	public void replaceRecord(byte name[], byte value[])
		throws GZIPFormatException {

		if(records != null) {
			removeAllRecords(name);
		}
		addRecord(name,value);
	}

	public void removeAllRecords(byte name[]) {
		int removed = 0;
		if(records != null) {
			int kept = 0;
			for(int i = 0; i < records.size(); i++) {
				if(records.get(i).matchesName(name)) {
					records.remove(i);
					removed++;
					i--;
				} else {
					kept++;
				}
			}
			if(kept == 0) {
				records = null;
				staticHeader.setFExtraFlag(false);
			}
		}
		if(removed > 0) {
			crc = -1;
		}
	}

	public void addRecord(byte name[], long intVal) throws GZIPFormatException {
		if(records == null) {
			records = new GZIPFExtraRecords();
		}
		records.add(new GZIPFExtraRecord(name,intVal));
		staticHeader.setFExtraFlag(true);
		crc = -1;
	}

	public void addRecord(byte name[], byte value[]) throws GZIPFormatException {
		if(records == null) {
			records = new GZIPFExtraRecords();
		}
		records.add(new GZIPFExtraRecord(name,value));
		staticHeader.setFExtraFlag(true);
		crc = -1;
	}

	public GZIPFExtraRecord getRecord(int i) {
		if(records == null || records.isEmpty()) {
			throw new IndexOutOfBoundsException();
		}
		return records.get(i);
	}

	public int getRecordCount() {
		return (records == null) ? 0 : records.size();
	}

	public GZIPFExtraRecord getRecord(byte[] name) {
		if(records != null) {
			for(GZIPFExtraRecord rec : records) {
				if(rec.matchesName(name)) {
					return rec;
				}
			}
		}
		return null;
	}

	public long getIntRecord(byte[] name) {
		if(records != null) {
			for(GZIPFExtraRecord rec : records) {
				if(rec.matchesName(name)) {
					return ByteOp.bytesToInt(rec.getValue());
				}
			}
		}
		return -1;
	}

	public int getLength() {
		int size = staticHeader.getLength();
		if(records != null) {
			size += records.getByteLength();
		}
		size += fileNameLength;
		size += commentLength;
		if(staticHeader.isFHCRCSet()) {
			size += 2;
		}
		return size;
	}

	public void writeBytes(OutputStream os) throws IOException {
		OutputStream origOS = os;
		if(staticHeader.isFHCRCSet()) {
			if(crc == -1) {
				os = new CRCOutputStream(origOS);
			}
		}
		staticHeader.writeTo(os);
		if(staticHeader.isFExtraSet()) {
			records.writeTo(os);
		}
		if(staticHeader.isFNameSet()) {
			os.write(fileName);
		}
		if(staticHeader.isFCommentSet()) {
			os.write(comment);
		}
		if(staticHeader.isFHCRCSet()) {

			if(crc == -1) {
				crc = (int) ((CRCOutputStream) os).getCRCValue() & 0xffff;
			}
			ByteOp.writeShort(origOS, crc);
		}
	}
	public static boolean isValidCompressionMethod(int cm) {
		return cm == GZIP_COMPRESSION_METHOD_DEFLATE;
	}

}

