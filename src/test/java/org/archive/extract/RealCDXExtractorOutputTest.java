package org.archive.extract;

import java.net.URI;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class RealCDXExtractorOutputTest {

    @Test
    public void testEscapeResolvedUrl() throws Exception {
        String context = "http://www.uni-giessen.de/cms/studium/dateien/informationberatung/merkblattpdf";
        String spec = "http://fss.plone.uni-giessen.de/fß/studium/dateien/informationberatung/merkblattpdf/file/Mérkblatt zur Gestaltung von Nachteilsausgleichen.pdf?föo=bar#änchor";
        String escaped = RealCDXExtractorOutput.resolve(context, spec);
        assertTrue(escaped.indexOf(" ") < 0);
        URI parsed = new URI(escaped);
        assertEquals("änchor", parsed.getFragment());
    }

    @Test
    public void testNoDoubleEscaping() throws Exception {
        String spec = "https://www.google.com/search?q=java+escape+url+spaces&ie=utf-8&oe=utf-8";
        String resolved = RealCDXExtractorOutput.resolve(spec, spec);
        assertTrue(spec.equals(resolved));
    }
}
