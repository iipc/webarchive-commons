package org.archive.access.nutch;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.Summary;

/**
 * Proxy that allows us intercept getSummary so we can change key used.
 * @author stack
 */
public class NutchwaxBean extends NutchBean {
	public NutchwaxBean(Configuration conf, Path dir) throws IOException {
		super(conf, dir);
	}

	public NutchwaxBean(Configuration conf) throws IOException {
		super(conf);
	}

	public static NutchBean get(ServletContext app, Configuration conf)
	throws IOException {
	    NutchBean bean = (NutchBean)app.getAttribute("nutchBean");
	    if (bean == null) {
	      if (LOG.isInfoEnabled()) { LOG.info("creating new bean"); }
	      // Get the NutchwaxBean in there.
	      bean = new NutchwaxBean(conf);
	      app.setAttribute("nutchBean", bean);
	    }
	    return bean;
	}
	
	public Summary[] getSummary(HitDetails[] hits, Query query)
	throws IOException {
		// Rewrite details so that URL is not just URL when we go to get Summary.
		// Its compound of collection and url. Alternative is override of
		// NutchBean so we can add in our own Summarizer. NutchBean needs to be
		// made more amenable to subclassing. Should be setters for detailers,
		// etc. so can supply alternatives (Or pass in a constructor).
		HitDetails[] amendedHits = new HitDetails[hits.length];
		for (int j = 0; j < hits.length; j++) {
			HitDetails h = hits[j];
			amendedHits[j] = getCollectionQualifiedHitDetails(h);
		}
		return super.getSummary(amendedHits, query);
	}
	
	public String[] getAnchors(HitDetails h) throws IOException {
		return super.getAnchors(getCollectionQualifiedHitDetails(h));
	}
	
	public Inlinks getInlinks(HitDetails h) throws IOException {
		return super.getInlinks(getCollectionQualifiedHitDetails(h));
	}
	
	/**
	 * TODO: Make it so I don't have to create a new HitDetails changing
	 * the key used doing lookup.
	 * @param h
	 * @return
	 */
	protected HitDetails getCollectionQualifiedHitDetails(final HitDetails h) {
		return new HitDetails(h.getValue("segment"),
	        Nutchwax.generateWaxKey(h.getValue("url"),
	            h.getValue("collection")).toString());
	}
}