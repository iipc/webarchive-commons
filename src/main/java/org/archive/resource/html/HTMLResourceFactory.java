package org.archive.resource.html;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.archive.format.http.HttpHeaders;
import org.archive.format.json.JSONUtils;
import org.archive.format.text.charset.CharsetDetector;
import org.archive.format.text.charset.StandardCharsetDetector;
import org.archive.format.text.html.CDATALexer;
import org.archive.format.text.html.LexParser;
import org.archive.resource.MetaData;
import org.archive.resource.Resource;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceFactory;
import org.archive.resource.ResourceParseException;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;
import org.json.JSONException;
import org.json.JSONObject;

public class HTMLResourceFactory implements ResourceFactory {

	protected static final int CHARSET_GUESS_CHUNK_SIZE = 8192;
	protected static final String HTTP_HEADER_PATH = "Envelope.Payload-Metadata.HTTP-Response-Metadata.Headers";

	protected CharsetDetector charSetDetector = new StandardCharsetDetector();


	public Resource getResource(InputStream is, MetaData parentMetaData,
			ResourceContainer container) throws ResourceParseException, IOException {
		HTMLMetaData hmd = new HTMLMetaData(parentMetaData);
		ExtractingParseObserver epo = new ExtractingParseObserver(hmd);
		LexParser parser = new LexParser(epo);
		CDATALexer lex = new CDATALexer();

		// guess charset based on HTTP header and sniffed content chunk
		is = new BufferedInputStream(is, CHARSET_GUESS_CHUNK_SIZE);
		byte[] chunk = new byte[CHARSET_GUESS_CHUNK_SIZE];
		is.mark(0);
		int chunkSize = is.read(chunk, 0, CHARSET_GUESS_CHUNK_SIZE);
		is.reset();
		JSONObject headers = JSONUtils.extractObject(hmd.getTopMetaData(), HTTP_HEADER_PATH);
		HttpHeaders httpHeaders = new HttpHeaders();
		if (headers.has("Content-Type")) {
			try {
				httpHeaders.add("Content-Type", headers.getString("Content-Type"));
			} catch (JSONException e) { }
		}

		String charset = charSetDetector.getCharset(chunk, chunkSize, httpHeaders);

		Page page;
		try {
			page = new Page(is, charset);
			lex.setPage(page);
			parser.doParse(lex);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new ResourceParseException(e);
		} catch (ParserException e) {
			e.printStackTrace();
			throw new ResourceParseException(e);
		} catch(OutOfMemoryError e) {
			throw new ResourceParseException(null);
		}

		return new HTMLResource(hmd,container);
	}
}
