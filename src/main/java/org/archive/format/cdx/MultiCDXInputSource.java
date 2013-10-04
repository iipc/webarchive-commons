package org.archive.format.cdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.archive.format.gzip.zipnum.ZipNumIndex;
import org.archive.format.gzip.zipnum.ZipNumParams;
import org.archive.util.iterator.CloseableCompositeIterator;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.SortedCompositeIterator;

public class MultiCDXInputSource implements CDXInputSource {

	private final static Logger LOGGER = Logger.getLogger(MultiCDXInputSource.class.getName());
	
	protected List<CDXInputSource> cdx;
	
	public List<CDXInputSource> getCdx() {
		return cdx;
	}

	public void setCdx(List<CDXInputSource> cdx) {
		this.cdx = cdx;
	}
	
	public void setCdxUris(List<String> cdxUris) throws IOException {
		cdx = new ArrayList<CDXInputSource>();
		
		for (String uri : cdxUris) {
			if (uri.endsWith(".cdx") || uri.endsWith(".cdx.gz")) {
				cdx.add(new CDXFile(uri));
			} else if (uri.endsWith("ALL.summary") && uri.contains("/")) {
				cdx.add(ZipNumIndex.createIndexWithSummaryPath(uri));
			} else {
				//Skipping?
			}
		}
	}


	Comparator<String> comparator = new Comparator<String>() {
		public int compare(String s1, String s2) {
			return s1.compareTo(s2);
		}
	};
	
	Comparator<String> reverseComparator = new Comparator<String>() {
		public int compare(String s1, String s2) {
			return -s1.compareTo(s2);
		}
	};
	
	public CloseableIterator<String> getCDXIterator(String key, String prefix, boolean exact, ZipNumParams params) throws IOException {
		
		SortedCompositeIterator<String> scitr = new SortedCompositeIterator<String>(cdx.size(), params.isReverse() ? reverseComparator : comparator);
		
		CloseableIterator<String> iter = null;
		
		for (CDXInputSource cdxReader : cdx) {
			try {
				iter = cdxReader.getCDXIterator(key, prefix, exact, params);
				scitr.addIterator(iter);
			} catch (IOException io) {
				LOGGER.warning(io.toString());
			}
		}
		
		return scitr;
	}
	
	public CloseableIterator<String> createSeqIterator(String key, String start, String end, ZipNumParams params)
	{
		CloseableCompositeIterator<String> composite = new CloseableCompositeIterator<String>();
		CloseableIterator<String> iter = null;
		
		for (CDXInputSource cdxReader : cdx) {
			try {
				iter = cdxReader.getCDXIterator(key, start, end, params);
				
				if (!params.isReverse()) {
					composite.addLast(iter);
				} else {
					composite.addFirst(iter);
				}
				
			} catch (IOException io) {
				LOGGER.warning(io.toString());
			}
		}
		
		return composite;
	}
	
	
	public CloseableIterator<String> getCDXIterator(String key, String start, String end, ZipNumParams params) throws IOException {
		
		if (params.isSequential()) {
			return this.createSeqIterator(key, start, end, params);
		}
		
		SortedCompositeIterator<String> scitr = new SortedCompositeIterator<String>(cdx.size(), params.isReverse() ? reverseComparator : comparator);
		
		CloseableIterator<String> iter = null;
		
		for (CDXInputSource cdxReader : cdx) {
			try {
				iter = cdxReader.getCDXIterator(key, start, end, params);
				scitr.addIterator(iter);
			} catch (IOException io) {
				LOGGER.warning(io.toString());
			}
		}
		
		return scitr;
	}
}
