package org.archive.format.dns;

import java.util.ArrayList;

public class DNSResponse extends ArrayList<DNSRecord> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -10624236867791758L;
	private String date;
	public void setDate(String date) {
		this.date = date;
	}

	public String getDate() {
		return date;
	}
	
}
