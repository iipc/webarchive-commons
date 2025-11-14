package org.archive.resource.warc;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.archive.format.http.HttpHeader;
import org.archive.format.http.HttpResponse;
import org.archive.resource.AbstractResource;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceParseException;
import org.archive.util.Base32;
import org.archive.util.StreamCopy;
import org.archive.util.io.EOFNotifyingInputStream;
import org.archive.util.io.EOFObserver;
import org.archive.util.io.PushBackOneByteInputStream;

import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;

public class WARCResource extends AbstractResource implements EOFObserver, ResourceConstants {
	CountingInputStream countingIS;
	private HttpResponse response;
	private DigestInputStream digIS;
	private MetaData envelope;

	public WARCResource(MetaData metaData, ResourceContainer container,
			HttpResponse response) throws ResourceParseException {

		super(metaData.createChild(PAYLOAD_METADATA),container);
		envelope = metaData;
		this.response = response;

		long length = -1;
		metaData.putString(ENVELOPE_FORMAT, ENVELOPE_FORMAT_WARC_1_0);
		metaData.putLong(WARC_HEADER_LENGTH, response.getHeaderBytes());
		MetaData fields = metaData.createChild(WARC_HEADER_METADATA);
		for(HttpHeader h : response.getHeaders()) {
			String name = h.getName();
			String value = h.getValue();
			fields.putString(name,value);
			if(name.toLowerCase(Locale.ROOT).equals("content-length")) {
				// TODO: catch formatexception
				length = Long.parseLong(value);
			}
		}

		if(length >= 0) {
			countingIS = new CountingInputStream(
					ByteStreams.limit(response, length));
		} else {
			throw new ResourceParseException(new Exception("Zero or negative length: " + length));
		}
		try {
			digIS = new DigestInputStream(countingIS, 
					MessageDigest.getInstance("sha1"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	@Override
	public InputStream getInputStream() {
		return new EOFNotifyingInputStream(digIS, this);
	}

	@Override
	public void notifyEOF() throws IOException {
		String digString = Base32.encode(digIS.getMessageDigest().digest());
		if(container.isCompressed()) {
			if (!metaData.has(PAYLOAD_LENGTH) || countingIS.getCount() != metaData.getLong(PAYLOAD_LENGTH)) {
				metaData.putLong(PAYLOAD_LENGTH, countingIS.getCount());
			}
			metaData.putLong(PAYLOAD_SLOP_BYTES, StreamCopy.readToEOF(response));
			metaData.putString(PAYLOAD_DIGEST, "sha1:"+digString);
		} else {
			// consume trailing bytes if we can...
			InputStream raw = response.getInner();
			if(raw instanceof PushBackOneByteInputStream) {
				PushBackOneByteInputStream pb1bis = 
					(PushBackOneByteInputStream) raw;
				long numNewlines = StreamCopy.skipChars(pb1bis, CR_NL_CHARS);
				if(numNewlines > 0) {
					long payloadLength = countingIS.getCount();
					if (!metaData.has(PAYLOAD_LENGTH) || payloadLength != metaData.getLong(PAYLOAD_LENGTH)) {
						metaData.putLong(PAYLOAD_LENGTH, payloadLength);
					}
					metaData.putLong(PAYLOAD_SLOP_BYTES, numNewlines);
					metaData.putString(PAYLOAD_DIGEST, "sha1:"+digString);
				}
			}
		}
	}

	public MetaData getEnvelopeMetaData() {
		return envelope;
	}
}
