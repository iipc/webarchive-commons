package org.archive.resource.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.archive.format.text.html.CDATALexer;
import org.archive.format.text.html.LexParser;
import org.archive.resource.MetaData;
import org.archive.resource.Resource;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceFactory;
import org.archive.resource.ResourceParseException;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;

public class HTMLResourceFactory implements ResourceFactory {

	public Resource getResource(InputStream is, MetaData parentMetaData,
			ResourceContainer container) throws ResourceParseException, IOException {
		HTMLMetaData hmd = new HTMLMetaData(parentMetaData);
		ExtractingParseObserver epo = new ExtractingParseObserver(hmd);
		LexParser parser = new LexParser(epo);
		CDATALexer lex = new CDATALexer();
		// TODO: figure out charset:
		String charset = "UTF-8";
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
