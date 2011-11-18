package org.archive.resource.http;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.archive.format.http.HttpHeader;
import org.archive.format.http.HttpRequest;
import org.archive.resource.AbstractResource;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceContainer;
import org.archive.util.Base32;
import org.archive.util.StreamCopy;
import org.archive.util.io.EOFNotifyingInputStream;
import org.archive.util.io.EOFObserver;

import com.google.common.io.CountingInputStream;

public class HTTPRequestResource extends AbstractResource implements ResourceConstants, EOFObserver {
	CountingInputStream countingIS;
	private HttpRequest request;

	DigestInputStream digIS;
	
	public HTTPRequestResource(MetaData metaData, 
			ResourceContainer container, HttpRequest request) {
		this(metaData,container,request,false);
	}

	public HTTPRequestResource(MetaData metaData, 
			ResourceContainer container, HttpRequest request,
			boolean forceCheck) {
		super(metaData,container);
		this.request = request;

		MetaData message = metaData.createChild(HTTP_REQUEST_MESSAGE);

		message.putString(HTTP_MESSAGE_METHOD,request.getMessage().getMethodString());
		message.putString(HTTP_MESSAGE_PATH,request.getMessage().getPath());
		message.putString(HTTP_MESSAGE_VERSION,request.getMessage().getVersionString());

		metaData.putLong(HTTP_HEADERS_LENGTH,request.getHeaderBytes());

		if(request.getHeaders().isCorrupt()) {
			metaData.putBoolean(HTTP_HEADERS_CORRUPT,true);
		}

		MetaData headers = metaData.createChild(HTTP_HEADERS_LIST);
		for(HttpHeader h : request.getHeaders()) {
			headers.putString(h.getName(),h.getValue());
			// TODO: handle non-empty request entity (put/post)
		}

		countingIS = new CountingInputStream(request);
		try {
			digIS = 
				new DigestInputStream(countingIS,
						MessageDigest.getInstance("sha1"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public HttpRequest getHttpResponse() {
		return request;
	}

	public InputStream getInputStream() {
		return new EOFNotifyingInputStream(digIS, this);
	}

	public void notifyEOF() throws IOException {

		metaData.putLong(HTTP_ENTITY_LENGTH, countingIS.getCount());
		String digString = Base32.encode(digIS.getMessageDigest().digest());
		metaData.putString(HTTP_ENTITY_DIGEST, "sha1:"+digString);

		metaData.putLong(HTTP_ENTITY_TRAILING_SLOP, 
				StreamCopy.readToEOF(request));		
	}

}
