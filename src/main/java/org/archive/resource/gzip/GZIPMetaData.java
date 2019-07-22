package org.archive.resource.gzip;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.archive.format.gzip.GZIPConstants;
import org.archive.format.gzip.GZIPFExtraRecord;
import org.archive.format.gzip.GZIPFooter;
import org.archive.format.gzip.GZIPHeader;
import org.archive.format.gzip.GZIPSeriesMember;
import org.archive.format.gzip.GZIPStaticHeader;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.util.ByteOp;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

public class GZIPMetaData extends MetaData implements ResourceConstants {
	private static final Logger LOG = Logger.getLogger(GZIPMetaData.class.getName());
	
	public GZIPMetaData(MetaData parentMetaData) {
		super(parentMetaData,GZIP);
	}
	public void setData(GZIPSeriesMember member) {
		try {
			GZIPHeader header = member.getHeader();
			GZIPStaticHeader staticH = header.getStaticHeader();
			if(staticH.isFNameSet()) {
				putString(GZIP_FILENAME,new String(header.getFileName(),"UTF-8"));
			}
			if(staticH.isFCommentSet()) {
				putLong(GZIP_COMMENT_LENGTH,header.getCommentLength());				
			}
			if(staticH.isFHCRCSet()) {
				putLong(GZIP_HEADER_CRC,header.getHeaderCRC());
			}
			
			int records = header.getRecordCount();
			for(int i = 0; i < records; i++) {
				GZIPFExtraRecord rec = header.getRecord(i);
				JSONObject recJO = new JSONObject();
				String name = new String(rec.getName(),"UTF-8");
				recJO.put(GZIP_FEXTRA_NAME, name);
				if(name.equals("SL") || name.equals("LX")) {
					recJO.put(GZIP_FEXTRA_VALUE, ByteOp.bytesToInt(rec.getValue()));
				} else {
					recJO.put(GZIP_FEXTRA_VALUE, ByteOp.drawHex(rec.getValue()));					
				}
				appendChild(GZIP_FEXTRA,recJO);
			}
			putLong(GZIP_DEFLATE_LENGTH,member.getCompressedBytesRead());
			putLong(GZIP_HEADER_LENGTH, header.getLength());
			putLong(GZIP_FOOTER_LENGTH, GZIPConstants.GZIP_FOOTER_BYTES);
			GZIPFooter footer = member.getFooter();
			putLong(GZIP_INFLATED_CRC,footer.getCRC());
			putLong(GZIP_INFLATED_LENGTH,footer.getLength());

		} catch (UnsupportedEncodingException e) {
			LOG.warning(e.getMessage());
		} catch (JSONException e) {
			LOG.warning(e.getMessage());
		}
		
	}
}
