package org.archive.extract;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import junit.framework.TestCase;


public class RealCDXExtractorOutputTest extends TestCase {

    public void testEscapeResolvedUrl() throws Exception {
	String context ="http://www.uni-giessen.de/cms/studium/dateien/informationberatung/merkblattpdf";
	String spec = "http://fss.plone.uni-giessen.de/fß/studium/dateien/informationberatung/merkblattpdf/file/Mérkblatt zur Gestaltung von Nachteilsausgleichen.pdf?föo=bar#änchor";
	String escaped = RealCDXExtractorOutput.resolve(context, spec);
	assertTrue(escaped.indexOf(" ") < 0);
	URI parsed = new URI(escaped);
	assertEquals("änchor", parsed.getFragment());
    }

    public void testNoDoubleEscaping() throws Exception {
	String spec = "https://www.google.com/search?q=java+escape+url+spaces&ie=utf-8&oe=utf-8";
	String resolved = RealCDXExtractorOutput.resolve(spec, spec);
	assertTrue(spec.equals(resolved));
    }
}
