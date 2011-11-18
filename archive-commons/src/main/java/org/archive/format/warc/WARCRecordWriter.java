package org.archive.format.warc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

import org.archive.format.http.HttpConstants;
import org.archive.format.http.HttpHeaders;
import org.archive.util.DateUtils;
import org.archive.util.StreamCopy;

import com.google.common.io.FileBackedOutputStream;

public class WARCRecordWriter implements WARCConstants, HttpConstants {
	private static final String SCHEME = "urn:uuid";
	private static final String SCHEME_COLON = SCHEME + ":";
	private static int DEFAULT_RAM_BUFFER = 1024 * 1024;
	public int ramBuffer = DEFAULT_RAM_BUFFER;
	
//	OutputStream out;
//	public WARCRecordWriter(OutputStream out) {
//		this.out = out;
//	}

	private void writeRecord(OutputStream out, HttpHeaders headers, 
			InputStream contents, int trailingCRLFs) throws IOException {
		InputStream content2 = null;
		if(contents == null) {
			headers.add(CONTENT_LENGTH, "0");
			
		} else {
			FileBackedOutputStream fbos = new FileBackedOutputStream(ramBuffer);
			long amt = StreamCopy.copy(contents, fbos) + (2 * trailingCRLFs);
			headers.add(CONTENT_LENGTH,String.valueOf(amt));
			content2 = fbos.getSupplier().getInput();
		}

		out.write(WARC_ID.getBytes(DEFAULT_ENCODING));
		out.write(CR);
		out.write(LF);
		headers.write(out);
		if(content2 != null) {
			StreamCopy.copy(content2, out);
		}
		for(int i = 0; i < trailingCRLFs; i++) {
			out.write(CR);
			out.write(LF);
		}
	}
	public void writeWARCInfoRecord(OutputStream out, String filename, 
			InputStream contents) throws IOException {

//		WARC/1.0
//		WARC-Type: warcinfo
//		WARC-Date: 2010-10-08T07:00:26Z
//		WARC-Filename: LOC-MONTHLY-014-20101008070022-00127-crawling111.us.archive.org.warc.gz
//		WARC-Record-ID: <urn:uuid:05de9500-7047-4206-aa7f-346a0dc91b1f>
//		Content-Type: application/warc-fields
//		Content-Length: 600
		HttpHeaders headers = new HttpHeaders();
		headers.add(HEADER_KEY_TYPE, WARCINFO);
		headers.add(HEADER_KEY_DATE, DateUtils.getLog14Date());
		headers.add(HEADER_KEY_FILENAME, filename);
		headers.add(HEADER_KEY_ID, makeRecordId());
		headers.add(CONTENT_TYPE,WARC_FIELDS_TYPE);
		writeRecord(out,headers,contents,2);			
	}

	public void writeJSONMetadataRecord(OutputStream out, 
			InputStream contents, String targetURI, Date originalDate, 
			String origRecordId) throws IOException {

//		 WARC-Type 	 The type of WARC record. Set to 'metadata'
//		 WARC-Target-URI 	 The original URI of the primary content
//		 WARC-Date 	 A 14-digit timestamp that represents the instant of data capture of the primary content
//		 WARC-Record-ID 	 An identifier assigned to the current record that is globally unique for its period of intended use
//		 WARC-Refers-To 	 The WARC-Record-ID of the primary WARC record being described.
//		 In the case of ARC records, the identifier is a combination of ARC filename and file-offset (e.g. <urn:arc:foo.arc.gz:3492>)
//		 Content-Type 	 The MIME type of the information contained in the metadata record's block. Set to 'application/json'
//		 Content-Length 	 The number of octets in the metadata record's block
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HEADER_KEY_TYPE, METADATA);
		headers.add(HEADER_KEY_URI, targetURI);
		headers.add(HEADER_KEY_DATE, DateUtils.getLog14Date(originalDate));
		headers.add(HEADER_KEY_ID, makeRecordId());
		headers.add(HEADER_KEY_REFERS_TO, origRecordId);
		
		headers.add(CONTENT_TYPE,"application/json");
		writeRecord(out, headers, contents, 1);
	}

	private String makeRecordId() {
		StringBuilder recID = new StringBuilder();
		recID.append("<").append(SCHEME_COLON);
		recID.append(UUID.randomUUID().toString());
		recID.append(">");
		return recID.toString();
	}
}
