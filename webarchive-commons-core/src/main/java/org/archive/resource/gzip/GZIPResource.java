package org.archive.resource.gzip;

import java.io.IOException;
import java.io.InputStream;

import org.archive.format.gzip.GZIPConstants;
import org.archive.format.gzip.GZIPSeriesMember;
import org.archive.resource.AbstractResource;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceContainer;
import org.archive.util.io.EOFNotifyingInputStream;
import org.archive.util.io.EOFObserver;

public class GZIPResource extends AbstractResource 
	implements GZIPConstants, EOFObserver, ResourceConstants {

	private GZIPSeriesMember member;
	private EOFNotifyingInputStream eofStream;
	private GZIPMetaData gzMetaData;

	public GZIPResource(MetaData metaData, ResourceContainer container, 
			GZIPSeriesMember member) {
		super(metaData, container);
		this.member = member;
		this.eofStream = 
			new EOFNotifyingInputStream(member, this);

		MetaData containerMD = new MetaData(metaData, CONTAINER);

		containerMD.putString(CONTAINER_FILENAME, member.getRecordFileContext());
		containerMD.putBoolean(CONTAINER_COMPRESSED, true);
		containerMD.putLong(CONTAINER_OFFSET, member.getRecordStartOffset());

		gzMetaData = new GZIPMetaData(containerMD);
	}

	public void close() throws IOException {
		member.close();
	}

	public InputStream getInputStream() {
		return eofStream;
	}

	public void notifyEOF() throws IOException {
		gzMetaData.setData(member);
	}
}
