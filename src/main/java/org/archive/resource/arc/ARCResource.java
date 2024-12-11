package org.archive.resource.arc;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.archive.format.arc.ARCConstants;
import org.archive.format.arc.ARCMetaData;
import org.archive.resource.AbstractResource;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceContainer;
import org.archive.util.Base32;
import org.archive.util.StreamCopy;
import org.archive.util.io.EOFNotifyingInputStream;
import org.archive.util.io.EOFObserver;
import org.archive.util.io.PushBackOneByteInputStream;

import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;

public class ARCResource extends AbstractResource

implements ResourceConstants, ARCConstants, EOFObserver {

	CountingInputStream countingIS;
	InputStream raw;
	DigestInputStream digIS;
	MetaData envelope;
	ARCMetaData arcMetaData;
	
	public ARCResource(MetaData metaData, ResourceContainer container, 
			ARCMetaData arcMetaData, InputStream raw) {

		super(metaData.createChild(PAYLOAD_METADATA),container);
		envelope = metaData;
		this.arcMetaData = arcMetaData;
		this.raw = raw;

		metaData.putString(ENVELOPE_FORMAT, ENVELOPE_FORMAT_ARC);
		metaData.putLong(ARC_HEADER_LENGTH, arcMetaData.getHeaderLength());
		long leadingNL = arcMetaData.getLeadingNL();
		if(leadingNL > 0) {
			metaData.putLong(PAYLOAD_LEADING_SLOP_BYTES, leadingNL);
		}
		MetaData fields = metaData.createChild(ARC_HEADER_METADATA);

		fields.putString(URL_KEY, arcMetaData.getUrl());
		fields.putString(IP_KEY, arcMetaData.getIP());
		fields.putString(DATE_STRING_KEY, arcMetaData.getDateString());
		fields.putString(MIME_KEY, arcMetaData.getMime());
		fields.putLong(DECLARED_LENGTH_KEY, arcMetaData.getLength());

		countingIS = new CountingInputStream(
				ByteStreams.limit(raw, arcMetaData.getLength()));

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
		metaData.putLong(PAYLOAD_LENGTH, countingIS.getCount());
		String digString = Base32.encode(digIS.getMessageDigest().digest());
		metaData.putString(PAYLOAD_DIGEST, "sha1:"+digString);

		if(container.isCompressed()) {
			metaData.putLong(PAYLOAD_SLOP_BYTES, StreamCopy.readToEOF(raw));
		} else {
			if(raw instanceof PushBackOneByteInputStream) {
				PushBackOneByteInputStream pb1bis = 
					(PushBackOneByteInputStream) raw;
				long numNewlines = StreamCopy.skipChars(pb1bis, CR_NL_CHARS);
				if(numNewlines > 0) {
					metaData.putLong(PAYLOAD_SLOP_BYTES, numNewlines);
				}
			}
		}
	}

	public MetaData getEnvelopeMetaData() {
		return envelope;
	}
}
