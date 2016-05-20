package org.archive.format.warc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

import org.archive.format.http.HttpConstants;
import org.archive.format.http.HttpHeaders;
import org.archive.util.DateUtils;

public class WARCRecordWriter implements WARCConstants, HttpConstants 
{
  private static final String SCHEME = "urn:uuid";
  private static final String SCHEME_COLON = SCHEME + ":";
	
  /** 
   * Write the headers and contents as a WARC record to the given
   * output stream.
   *
   * WARC record format:
   * <pre>warc-file = 1*warc-record
   * warc-record = header CRLF block CRLF CRLF
   * header = version warc-fields
   * version = "WARC/0.18" CRLF
   * warc-fields = *named-field CRLF
   * block = *OCTET</pre>
   */
  private void writeRecord( OutputStream out, 
                            HttpHeaders headers, 
                            byte[] contents) throws IOException
  {
    if ( contents == null ) 
      {
        headers.add(CONTENT_LENGTH, "0");
      }
    else
      {
        headers.add(CONTENT_LENGTH,String.valueOf(contents.length));
      }
    
    out.write(WARC_ID.getBytes(DEFAULT_ENCODING));
    out.write(CR);
    out.write(LF);

    // NOTE: HttpHeaders.write() method includes the trailing CRLF.
    //       So we don't need to write it out here.
    headers.write(out);
  
    if ( contents != null ) 
      {
        out.write( contents );
      }

    // Emit the 2 trailing CRLF sequences.
    out.write(CR);
    out.write(LF);
    out.write(CR);
    out.write(LF);
  }

  public void writeWARCInfoRecord(OutputStream out, 
                                  String filename, 
                                  byte[] contents ) throws IOException
  {
    // WARC/1.0
    // WARC-Type: warcinfo
    // WARC-Date: 2010-10-08T07:00:26Z
    // WARC-Filename: LOC-MONTHLY-014-20101008070022-00127-crawling111.us.archive.org.warc.gz
    // WARC-Record-ID: <urn:uuid:05de9500-7047-4206-aa7f-346a0dc91b1f>
    // Content-Type: application/warc-fields
    // Content-Length: 600

    HttpHeaders headers = new HttpHeaders();
    headers.add(HEADER_KEY_TYPE, WARCRecordType.warcinfo.name());
    headers.add(HEADER_KEY_DATE, DateUtils.getLog14Date());
    headers.add(HEADER_KEY_FILENAME, filename);
    headers.add(HEADER_KEY_ID, makeRecordId());
    headers.add(CONTENT_TYPE,WARC_FIELDS_TYPE);
    writeRecord(out,headers,contents);
  }

  public void writeJSONMetadataRecord( OutputStream out,
                                       byte[] contents,
                                       String targetURI,
                                       Date originalDate,
                                       String origRecordId ) throws IOException
  {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HEADER_KEY_TYPE, WARCRecordType.metadata.name());
    headers.add(HEADER_KEY_URI, targetURI);
    headers.add(HEADER_KEY_DATE, DateUtils.getLog14Date(originalDate));
    headers.add(HEADER_KEY_ID, makeRecordId());
    headers.add(HEADER_KEY_REFERS_TO, origRecordId);
    
    headers.add(CONTENT_TYPE,"application/json");
    writeRecord(out, headers, contents);
  }

  private String makeRecordId() 
  {
    StringBuilder recID = new StringBuilder();
    recID.append("<").append(SCHEME_COLON);
    recID.append(UUID.randomUUID().toString());
    recID.append(">");
    return recID.toString();
  }

}
