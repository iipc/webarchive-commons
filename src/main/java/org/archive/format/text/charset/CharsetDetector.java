/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.format.text.charset;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.format.http.HttpHeader;
import org.archive.format.http.HttpHeaders;
import org.mozilla.universalchardet.UniversalDetector;

/**
 * Abstract class containing common methods for determining the character 
 * encoding of a text Resource, most of which should be refactored into a
 * Util package.
 * @author brad
 *
 */
public abstract class CharsetDetector {
	private final static String META_TAGNAME = "META";
	private final static String META_CONTENT_ATTRIBUTE = "content";
	private final static String META_HTTP_EQUIV_ATTRIBUTE = "http-equiv";
	private final static String META_CONTENT_TYPE = "Content-Type";
	
	
	private final static String QUOTED_ATTR_VALUE = "(?:\"[^\">]*\")";

	private final static String ESC_QUOTED_ATTR_VALUE = "(?:\\\\\"[^>\\\\]*\\\\\")";

	private final static String APOSED_ATTR_VALUE = "(?:'[^'>]*')";

//	private final static String RAW_ATTR_VALUE = "(?:[^ \\t\\n\\x0B\\f\\r>\"']+)";

	
	private final static String ANY_ATTR_VALUE = QUOTED_ATTR_VALUE + "|"
			+ APOSED_ATTR_VALUE + "|" + ESC_QUOTED_ATTR_VALUE + "|";
	
	private final static String META_TAG_PATTERN_STRING = "<\\s*" + META_TAGNAME 
			+ "((>)|(\\s+[^>]*>))";
	private final static String META_CONTENT_ATTR_PATTERN_STRING = "\\b" + 
		META_CONTENT_ATTRIBUTE + "\\s*=\\s*(" + ANY_ATTR_VALUE + ")(?:\\s|>)?";
	private final static String META_HTTP_EQUIV_ATTR_PATTERN_STRING = "\\b" + 
		META_HTTP_EQUIV_ATTRIBUTE + "\\s*=\\s*(" + META_CONTENT_TYPE + "|" +
		ANY_ATTR_VALUE + ")(?:\\s|>)?";

	
	
	private final static Pattern META_TAG_PATTERN;
	private final static Pattern META_CONTENT_ATTR_PATTERN;
	private final static Pattern META_HTTP_EQUIV_ATTR_PATTERN;
	
//	String metaContentType = TagMagix.getTagAttrWhere(sb, "META",
//			"content", "http-equiv", "Content-Type");


	static {
		META_TAG_PATTERN = Pattern.compile(META_TAG_PATTERN_STRING,
				Pattern.CASE_INSENSITIVE);
		META_CONTENT_ATTR_PATTERN = 
			Pattern.compile(META_CONTENT_ATTR_PATTERN_STRING, 
					Pattern.CASE_INSENSITIVE);
		META_HTTP_EQUIV_ATTR_PATTERN = 
			Pattern.compile(META_HTTP_EQUIV_ATTR_PATTERN_STRING, 
					Pattern.CASE_INSENSITIVE);
	};

	// hand off this many bytes to the chardet library
	protected final static int MAX_CHARSET_READAHEAD = 65536;
	// ...if it also includes "charset="
	protected final static String CHARSET_TOKEN = "charset=";
	// ...and if the chardet library fails, use the Content-Type header
	protected final static String HTTP_CONTENT_TYPE_HEADER = "CONTENT-TYPE";
	/** the default charset name to use when giving up */
	public final static String DEFAULT_CHARSET = "UTF-8";
	
	protected boolean isCharsetSupported(String charsetName) {
		// can you believe that this throws a runtime? Just asking if it's
		// supported!!?! They coulda just said "no"...
		if(charsetName == null) {
			return false;
		}
		try {
			return Charset.isSupported(charsetName);
		} catch(IllegalCharsetNameException e) {
			return false;
		}
	}
	protected String mapCharset(String orig) {
		String lc = orig.toLowerCase();
		if(lc.contains("iso8859-1") || lc.contains("iso-8859-1")) {
			return "cp1252";
		}
		return orig;
	}
	protected String contentTypeToCharset(final String contentType) {
		int offset = 
			contentType.toUpperCase().indexOf(CHARSET_TOKEN.toUpperCase());
		
		if (offset != -1) {
			String cs = contentType.substring(offset + CHARSET_TOKEN.length());
			if(isCharsetSupported(cs)) {
				return mapCharset(cs);
			}
			// test for extra spaces... there's at least one page out there that
			// indicates it's charset with:

//  <meta http-equiv="Content-type" content="text/html; charset=i so-8859-1">

			// bad web page!
			String alternate = cs.replace(" ", "");
			if(isCharsetSupported(alternate)) {
				return mapCharset(alternate);
			}
		}
		return null;
	}
	
	/**
	 * Attempt to divine the character encoding of the document from the 
	 * Content-Type HTTP header (with a "charset=")
	 * 
	 * @return String character set found or null if the header was not present
	 * @throws IOException 
	 */
	protected String getCharsetFromHeaders(HttpHeaders headers) 
	throws IOException {
		if(headers == null) {
			return null;
		}
		for(HttpHeader header : headers) {
			if(header.getName().toUpperCase().trim().equals(
					HTTP_CONTENT_TYPE_HEADER)) {
				return contentTypeToCharset(header.getValue());
			}
		}
		return null;
	}

	/**
	 * Attempt to find a META tag in the HTML that hints at the character set
	 * used to write the document.
	 * 
	 * @return String character set found from META tags in the HTML
	 * @throws IOException
	 */
	protected String getCharsetFromMeta(byte buffer[],int len) throws IOException {
		String charsetName = null;

		// convert to UTF-8 String -- which hopefully will not mess up the
		// characters we're interested in...
		String sample = new String(buffer,0,len,DEFAULT_CHARSET);
		String metaContentType = findMetaContentType(sample);
		if(metaContentType != null) {
			charsetName = contentTypeToCharset(metaContentType);
		}
		return charsetName;
	}

	private static String trimAttrValue(String value) {
		if (value.isEmpty()) {
			return value;
		}
		String result = value;
		if (result.charAt(0) == '"') {
			result = result.substring(1, result.length() - 1);
		} else if (result.charAt(0) == '\'') {
			result = result.substring(1, result.length() - 1);
		}
		return result;
	}

	public static String findMetaContentType(String pageSample) {

		Matcher tagMatcher = META_TAG_PATTERN.matcher(pageSample);

		while (tagMatcher.find()) {
			String wholeTag = tagMatcher.group();
			Matcher whereAttrMatcher = META_HTTP_EQUIV_ATTR_PATTERN.matcher(wholeTag);
			if (whereAttrMatcher.find()) {
				String attrValue = whereAttrMatcher.group(1);
				attrValue = trimAttrValue(attrValue);
				if (attrValue.compareToIgnoreCase(META_CONTENT_TYPE) == 0) {
					// this tag contains the right set, return the value for
					// the attribute findAttr:
					Matcher findAttrMatcher = META_CONTENT_ATTR_PATTERN.matcher(wholeTag);
					String value = null;
					if (findAttrMatcher.find()) {
						value = findAttrMatcher.group(1);
						value = trimAttrValue(value);
					}
					return value;
				}
				// not the tag we want... maybe there is another: loop
			}
		}

		return null;
	}
	
	
	/**
	 * Attempts to figure out the character set of the document using
	 * the excellent juniversalchardet library.
	 * 
	 * @return String character encoding found, or null if nothing looked good.
	 */
	protected String getCharsetFromBytes(byte buffer[], int len) 
	throws IOException {
		String charsetName = null;
	    UniversalDetector detector = new UniversalDetector(null);
		detector.handleData(buffer, 0, len);
		detector.dataEnd();
	    charsetName = detector.getDetectedCharset();
	    detector.reset();
	    if(isCharsetSupported(charsetName)) {
	    	return mapCharset(charsetName);
	    }
	    return null;
	}
	/**
	 * @return String charset name for the Resource
	 * @throws IOException if there are problems reading the Resource
	 */
	public abstract String getCharset(byte buffer[],int len, HttpHeaders headers)
		throws IOException;
}
