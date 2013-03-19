package org.archive.hadoop.pig.udf;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.apache.pig.FilterFunc;
import org.apache.pig.data.Tuple;
import org.archive.accesscontrol.AccessControlClient;
import org.archive.accesscontrol.RobotsUnavailableException;
import org.archive.accesscontrol.RuleOracleUnavailableException;
import org.archive.util.ArchiveUtils;

public class AccessControlAllowCapture extends FilterFunc {
		
	protected AccessControlClient client;
	protected String accessGroup;
	protected Date retrievalDate;
	
	public final static String BLOCK = "block";
	public final static String BLOCK_MESSAGE = "block-message";
	
	public AccessControlAllowCapture(String oracleUrl, String accessGroup)
	{
		this.client = new AccessControlClient(oracleUrl);
		this.accessGroup = accessGroup;
		
		// not really used, so just initing once
		this.retrievalDate = new Date();
	}

	@Override
	public Boolean exec(Tuple input) throws IOException {
		
		if (input == null || input.isNull() || (input.size() < 2)) {
			return false;
		}
		
		String url = input.get(0).toString();
		String date = input.get(1).toString();
		
		Date captureDate = null;
		String policy = null;

		try {
			captureDate = ArchiveUtils.getDate(date);
			policy = client.getPolicy(ArchiveUtils.addImpliedHttpIfNecessary(url), captureDate, retrievalDate, accessGroup);
			
		} catch (RobotsUnavailableException e) {
			//should never happen here, not checking robots
			throw new IOException("Oracle Failed", e);			
		} catch (RuleOracleUnavailableException e) {
			throw new IOException("Oracle Failed", e);
		} catch (ParseException e) {
			throw new IOException("Date Parse Failed", e);
		}
		
		// Blocked policies are "block" and "block-message"
		if (policy.equals(BLOCK) || policy.equals(BLOCK_MESSAGE)) {
			return false;
		}
		
		return true;
	}
}
