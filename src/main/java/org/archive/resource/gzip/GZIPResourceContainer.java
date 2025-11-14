package org.archive.resource.gzip;

import java.io.IOException;
import java.util.Locale;

import org.archive.format.gzip.GZIPMemberSeries;
import org.archive.format.gzip.GZIPSeriesMember;
import org.archive.resource.MetaData;
import org.archive.resource.Resource;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceParseException;
import org.archive.resource.ResourceProducer;

public class GZIPResourceContainer implements ResourceContainer, ResourceProducer {
	private static long UNLIMITED = -1;
	private long endOffset;

	private GZIPMemberSeries series;
	
	public GZIPResourceContainer(GZIPMemberSeries series) {
		this(series,UNLIMITED);
	}
	public GZIPResourceContainer(GZIPMemberSeries series, long endOffset) {
		this.series = series;
		this.endOffset = endOffset;
	}

	public String getName() {
		return series.getStreamContext();
	}

	public boolean isCompressed() {
		return true;
	}

	public Resource getNext() throws ResourceParseException, IOException {
		if(series.gotEOF()) return null;
		if(endOffset != UNLIMITED) {
			if(series.getOffset() > endOffset) {
//				System.err.format("At end of region off(%d) - startoff(%d) end(%d)\n",
//						series.getOffset(), 
//						series.getCurrentMemberStartOffset(), endOffset);
				return null;
			}
		}
		GZIPSeriesMember member = series.getNextMember();
		if(member == null) {
			return null;
		}
		MetaData top = new MetaData();
		return new GZIPResource(top,this,member);
	}

	public void close() throws IOException {
		series.close();
	}
	public String getContext() {
		return String.format(Locale.ROOT, "Context(%s)(%d)", series.getStreamContext(), series.getCurrentMemberStartOffset());
	}
}
