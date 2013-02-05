package org.archive.format.cdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.archive.format.gzip.zipnum.ZipNumCluster;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.SortedCompositeIterator;

public class MultiCDXInputSource implements CDXInputSource {

	
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
				cdx.add(new ZipNumCluster(uri));
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
	
	
	public CloseableIterator<String> getCDXLineIterator(String key, String prefix) throws IOException {
		
		SortedCompositeIterator<String> scitr = new SortedCompositeIterator<String>(cdx.size(), comparator);
		
		for (CDXInputSource cdxReader : cdx) {
			scitr.addIterator(cdxReader.getCDXLineIterator(key, prefix));
		}
		
		return scitr;
	}
}
