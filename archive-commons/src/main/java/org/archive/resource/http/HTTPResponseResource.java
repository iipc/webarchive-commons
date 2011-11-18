package org.archive.resource.http;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;


import org.archive.format.http.HttpHeader;
import org.archive.format.http.HttpResponse;
import org.archive.format.http.HttpResponseMessage;
import org.archive.resource.AbstractResource;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceContainer;
import org.archive.util.Base32;
import org.archive.util.StreamCopy;
import org.archive.util.io.EOFNotifyingInputStream;
import org.archive.util.io.EOFObserver;

import com.google.common.io.CountingInputStream;
import com.google.common.io.LimitInputStream;



public class HTTPResponseResource extends AbstractResource 
implements ResourceConstants, EOFObserver {

	private static final Logger LOG =
		Logger.getLogger(HTTPResponseResource.class.getName());
	
	CountingInputStream countingIS;
	private HttpResponse response;
	DigestInputStream digIS;
	
	public HTTPResponseResource(MetaData metaData, 
			ResourceContainer container, HttpResponse response) {
		this(metaData,container,response,false);
	}
	public HTTPResponseResource(MetaData metaData, 
			ResourceContainer container, HttpResponse response,
			boolean forceCheck) {
		super(metaData,container);
		this.response = response;

		MetaData message = metaData.createChild(HTTP_RESPONSE_MESSAGE);

		HttpResponseMessage httpMess = response.getMessage();

		message.putLong(HTTP_MESSAGE_STATUS,httpMess.getStatus());
		message.putString(HTTP_MESSAGE_VERSION,httpMess.getVersionString());
		message.putString(HTTP_MESSAGE_REASON,httpMess.getReason());

		metaData.putLong(HTTP_HEADERS_LENGTH,response.getHeaderBytes());

		if(response.getHeaders().isCorrupt()) {
			metaData.putBoolean(HTTP_HEADERS_CORRUPT,true);
		}

		MetaData headers = metaData.createChild(HTTP_HEADERS_LIST);
		long length = response.getHeaders().getContentLength();
		for(HttpHeader h : response.getHeaders()) {
			headers.putString(h.getName(),h.getValue());
		}
		if(forceCheck && (length != -1)) {
			LimitInputStream lis = new LimitInputStream(response, length);
			countingIS = new CountingInputStream(lis);
		} else {
			countingIS = new CountingInputStream(response);
		}
		try {
			digIS = 
				new DigestInputStream(countingIS,
						MessageDigest.getInstance("sha1"));
		} catch (NoSuchAlgorithmException e) {
			LOG.severe(e.getMessage());
		}
	}

	public HttpResponse getHttpResponse() {
		return response;
	}

	public InputStream getInputStream() {
		return new EOFNotifyingInputStream(digIS, this);
	}

	public void notifyEOF() throws IOException {

		metaData.putLong(HTTP_ENTITY_LENGTH, countingIS.getCount());
		String digString = Base32.encode(digIS.getMessageDigest().digest());
		metaData.putString(HTTP_ENTITY_DIGEST, "sha1:"+digString);
		metaData.putLong(HTTP_ENTITY_TRAILING_SLOP, 
				StreamCopy.readToEOF(response));		
	}
}
