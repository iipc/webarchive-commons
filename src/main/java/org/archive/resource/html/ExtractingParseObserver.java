package org.archive.resource.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.format.text.html.ParseObserver;
import org.htmlparser.Attribute;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;

public class ExtractingParseObserver implements ParseObserver {

	HTMLMetaData data;
	Stack<ArrayList<String>> openAnchors;
	Stack<StringBuilder> openAnchorTexts;
	String title = null;
	boolean inTitle = false;

	protected static String cssUrlPatString = 
		"url\\s*\\(\\s*([^)\\s]{1,8000}?)\\s*\\)";
	protected static String cssUrlTrimPatString =
			"^(?:\\\\?[\"'])+|(?:\\\\?[\"'])+$";
	protected static String cssImportNoUrlPatString = 
			"@import\\s+((?:'[^']+')|(?:\"[^\"]+\")|(?:\\('[^']+'\\))|(?:\\(\"[^\"]+\"\\))|(?:\\([^)]+\\))|(?:[a-z0-9_.:/\\\\-]+))\\s*;";

	protected static Pattern cssImportNoUrlPattern = Pattern
			.compile(cssImportNoUrlPatString);

	protected static Pattern cssUrlPattern = Pattern.compile(cssUrlPatString);

	protected static Pattern cssUrlTrimPattern = Pattern.compile(cssUrlTrimPatString);

	protected static String jsOnClickUrl1PatString = 
			"(?i)^(?:javascript:)?(?:(?:window|top|document|self|parent)\\.)?location(?:\\.href)?\\s*=\\s*('|&#39;)([^'\"]{3,256})\\1$";
	protected static String jsOnClickUrl2PatString = 
			"(?i)^(?:javascript:)?(?:window|parent)\\.open\\((['\"]|&#39;)([^\"']{3,256}?)\\1[,)]";
	protected static Pattern[] jsOnClickUrlPatterns = {
			Pattern.compile(jsOnClickUrl1PatString),
			Pattern.compile(jsOnClickUrl2PatString)
	};

	private final static int MAX_TEXT_LEN = 100;

	private static final String PATH = "path";
	private static final String PATH_SEPARATOR = "@/";
	private static final Map<String, TagExtractor> extractors;
	private static final Set<String> globalHrefAttributes;
	static {
		extractors = new HashMap<String,ExtractingParseObserver.TagExtractor>();
		extractors.put("A", new AnchorTagExtractor());
		extractors.put("APPLET", new AppletTagExtractor());
		extractors.put("AREA", new AreaTagExtractor());
		extractors.put("BASE", new BaseTagExtractor());
		extractors.put("DIV", new DivTagExtractor());
		extractors.put("EMBED", new EmbedTagExtractor());
		extractors.put("FORM", new FormTagExtractor());
		extractors.put("FRAME", new FrameTagExtractor());
		extractors.put("IFRAME", new IFrameTagExtractor());
		extractors.put("IMG", new ImgTagExtractor());
		extractors.put("INPUT", new InputTagExtractor());
		extractors.put("LINK", new LinkTagExtractor());
		extractors.put("META", new MetaTagExtractor());
		extractors.put("OBJECT", new ObjectTagExtractor());
		extractors.put("SCRIPT", new ScriptTagExtractor());
		extractors.put("Q", new QuotationLinkTagExtractor());
		extractors.put("BLOCKQUOTE", new QuotationLinkTagExtractor());
		extractors.put("DEL", new QuotationLinkTagExtractor());
		extractors.put("INS", new QuotationLinkTagExtractor());
		// HTML5:
		extractors.put("BUTTON", new ButtonTagExtractor());
		extractors.put("MENUITEM", new MenuitemTagExtractor());
		extractors.put("VIDEO", new EmbedVideoTagExtractor());
		extractors.put("AUDIO", new EmbedTagExtractor());
		extractors.put("TRACK", new EmbedTagExtractor());
		extractors.put("SOURCE", new EmbedTagExtractor());

		globalHrefAttributes = new HashSet<String>();
		globalHrefAttributes.add("background");
		globalHrefAttributes.add("data-href");
		globalHrefAttributes.add("data-uri");
	}

	
	public ExtractingParseObserver(HTMLMetaData data) {
		this.data = data;
		openAnchors = new Stack<ArrayList<String>>();
		openAnchorTexts = new Stack<StringBuilder>();
	}
	
	public void handleDocumentStart() {
		// no-op
	}

	public void handleDocumentComplete() {
		// no-op
	}

	public void handleTagEmpty(TagNode tag) {
		handleTagOpen(tag);
	}
		
	public void handleTagOpen(TagNode tag) {
		String name = tag.getTagName();
		if(name.equals("TITLE")) {
			inTitle = !tag.isEmptyXmlTag();
			return;
		}

		// first the global attributes:
		Vector<Attribute> attributes = tag.getAttributesEx();
		for (Attribute a : attributes) {
			String attrName = a.getName();
			String attrValue = a.getValue();
			if (attrName == null || attrValue == null) {
				continue;
			}
			attrName = attrName.toLowerCase(Locale.ROOT);
			if (globalHrefAttributes.contains(attrName)) {
				data.addHref(PATH,makePath(name,attrName),"url",attrValue);
			}
		}
		// TODO: style attribute, BASE(href) tag, Resolve URLs
		
		TagExtractor extractor = extractors.get(name);
		if(extractor != null) {
			extractor.extract(data, tag, this);
		}
	}

	public void handleTagClose(TagNode tag) {
		if(inTitle) {
			inTitle = false;
			data.setTitle(title);
			title = null;
			// probably the right thing..
			return;
		}
		// Only interesting if it's a </a>:
		if(tag.getTagName().equals("A")) {
			if(openAnchors.size() > 0) {
				// TODO: what happens here when we get unaligned (extra </a>'s?)
				ArrayList<String> vals = openAnchors.pop();
				StringBuilder text = openAnchorTexts.pop();
				if((vals != null) && (vals.size() > 0)) {
					if(text != null) {
						// contained an href - we want to ignore <a name="X"></a>:
						String trimmed = text.toString().trim().replaceAll("\\s+", " ");
						if(trimmed.length() > MAX_TEXT_LEN) {
							trimmed = trimmed.substring(0,MAX_TEXT_LEN);
						}
						if(trimmed.length() > 0) {
							vals.add("text");
							vals.add(trimmed);
						}
					}
					data.addHref(vals);
				}
			}
		}
	}

	public void handleTextNode(TextNode text) {
		// TODO: OPTIMIZ: This can be a lot smarter, if StringBuilders are full,
		//                this result is thrown away.
		String t = text.getText().replaceAll("\\s+", " ");

		if(t.length() > MAX_TEXT_LEN) {
			t = t.substring(0,MAX_TEXT_LEN);
		}
		if(inTitle) {
			title = t;

		} else {
			
			for(StringBuilder s : openAnchorTexts) {
				if(s.length() >= MAX_TEXT_LEN) {
					// if we are full, parents enclosing us should be too..
					break;
				}
				if(s.length() + t.length() < MAX_TEXT_LEN) {
					s.append(t);
				} else {
					// only add as much as we can:
					s.append(t.substring(0,MAX_TEXT_LEN - s.length()));
				}
				// BUGBUG: check now for multiple trailing spaces, and strip:
			}
		}
	}

	public void handleScriptNode(TextNode text) {
		// TODO: Find (semi) obvious URLs in JS:
	}

	public void handleStyleNode(TextNode text) {
		patternCSSExtract(data, cssUrlPattern, text.getText());
		patternCSSExtract(data, cssImportNoUrlPattern, text.getText());
	}

	public void handleRemarkNode(RemarkNode remark) {
		// TODO no-op, right??
	}
	
	/*
	 * =========================================
	 * 
	 *  ALL ASSIST METHODS/CLASSES BELOW HERE:
	 * 
	 * =========================================
	 */
	
	
	
	private static String makePath(String tag, String attr) {
		StringBuilder sb = new StringBuilder(tag.length() + 
				PATH_SEPARATOR.length() + attr.length());
		return sb.append(tag).append(PATH_SEPARATOR).append(attr).toString();
	}
	
	private static void addBasicHrefs(HTMLMetaData data, TagNode node, String... attrs) {
		for(String attr : attrs) {
			String val = node.getAttribute(attr);
			if(val != null) {
				data.addHref(PATH,makePath(node.getTagName(),attr),"url",val);
			}
		}
	}
	
	private static ArrayList<String> getAttrList(TagNode node, String... attrs) {
		ArrayList<String> l = new ArrayList<String>();
		for(String attr : attrs) {
			String val = node.getAttribute(attr);
			if(val != null) {
				l.add(attr);
				l.add(val);
			}
		}
		if(l.size() == 0) {
			return null;
		}
		return l;
	}

	private static ArrayList<String> getAttrListUrl(TagNode node, 
			String urlAttr, String... optionalAttrs) {
		String url = node.getAttribute(urlAttr);
		ArrayList<String> l = null;
		if(url != null) {
			l = new ArrayList<String>();
			l.add(PATH);
			l.add(makePath(node.getTagName(),urlAttr));
			l.add("url");
			l.add(url);
			// what else goes with it?
			for(String attr : optionalAttrs) {
				String val = node.getAttribute(attr);
				if(val != null) {
					l.add(attr);
					l.add(val);
				}
			}
		}
		return l;
	}
	
	private static void addHrefWithAttrs(HTMLMetaData data, TagNode node, 
			String hrefAttr, String... optionalAttrs) {
		ArrayList<String> l = getAttrListUrl(node,hrefAttr,optionalAttrs);
		if(l != null) {
			data.addHref(l);
		}
	}

	private static void addHrefsOnclick(HTMLMetaData data, TagNode node) {
		String onclick = node.getAttribute("onclick");
		if (onclick != null) {
			String path = makePath(node.getTagName(), "onclick");
			for (Pattern pattern : jsOnClickUrlPatterns) {
				String url = patternJSExtract(pattern, onclick);
				if (url != null) {
					data.addHref(PATH, path, "url", url);
				}
			}
		}
	}

	private interface TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs);
	}

	private static class AnchorTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			ArrayList<String> l = new ArrayList<String>();
			String url = node.getAttribute("href");
			if(url != null) {
				// got data:
				l.add(PATH);
				l.add(makePath("A","href"));
				l.add("url");
				l.add(url);
				for(String a : new String[] {"target","alt","title","rel","hreflang","type"}) {
					String v = node.getAttribute(a);
					if(v != null) {
						l.add(a);
						l.add(v);
					}
				}
			}

			if(node.isEmptyXmlTag()) {
				data.addHref(l);
			} else {
				obs.openAnchors.push(l);
				obs.openAnchorTexts.push(new StringBuilder());
			
			}
		}
	}

	private static class AppletTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			addBasicHrefs(data,node,"codebase","cdata");
		}
	}

	private static class AreaTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			String url = node.getAttribute("href");
			if(url != null) {
				ArrayList<String> l = new ArrayList<String>();
				l.add(PATH);
				l.add(makePath("AREA","href"));
				l.add("url");
				l.add(url);
				for(String a : new String[] {"rel"}) {
					String v = node.getAttribute(a);
					if(v != null) {
						l.add(a);
						l.add(v);
					}
				}
				data.addHref(l);
			}
		}
	}

	private static class BaseTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			String url = node.getAttribute("href");
			if(url != null) {
				data.setBaseHref(url);
			}
		}
	}
	
	private static class ButtonTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			addBasicHrefs(data,node,"formaction");
		}
	}

	private static class DivTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			addHrefsOnclick(data,node);
		}
	}

	private static class EmbedTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			addBasicHrefs(data,node,"src");
		}
	}
	
	private static class EmbedVideoTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			addBasicHrefs(data,node,"src","poster");
		}
	}

	private static class FormTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			ArrayList<String> l = new ArrayList<String>();
			String url = node.getAttribute("action");
			if(url != null) {
				// got data:
				l.add(PATH);
				l.add(makePath("FORM","action"));
				l.add("url");
				l.add(url);
				for(String a : new String[] {"target","method"}) {
					String v = node.getAttribute(a);
					if(v != null) {
						l.add(a);
						l.add(v);
					}
				}
				data.addHref(l);
			}
		}
	}

	private static class FrameTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			addBasicHrefs(data,node,"src");
		}
	}

	private static class IFrameTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			addBasicHrefs(data,node,"src");
		}
	}

	private static class ImgTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			addHrefWithAttrs(data,node,"src","alt","title");
			addBasicHrefs(data,node,"longdesc");
		}
	}

	private static class InputTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			addBasicHrefs(data,node,"src","formaction");
			addHrefsOnclick(data,node);
		}
	}

	private static class LinkTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			ArrayList<String> l = getAttrListUrl(node,"href","rel","type");
			if(l != null) {
				data.addLink(l);
			}
		}
	}

	private static class MenuitemTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			addBasicHrefs(data,node,"icon");
		}
	}

	private static class MetaTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			ArrayList<String> l = getAttrList(node,"name","rel","content","http-equiv","property");
			if(l != null) {
				data.addMeta(l);
			}
		}
	}

	private static class ObjectTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			addBasicHrefs(data,node,"codebase","cdata","data");
		}
	}

	private static class QuotationLinkTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			addBasicHrefs(data,node,"cite");
		}
	}

	private static class ScriptTagExtractor implements TagExtractor {
		public void extract(HTMLMetaData data, TagNode node, ExtractingParseObserver obs) {
			ArrayList<String> l = getAttrListUrl(node,"src","type");
			if(l != null) {
				data.addScript(l);
			}
		}
	}

	private void patternCSSExtract(HTMLMetaData data, Pattern pattern, String content) {
		Matcher m = pattern.matcher(content);
		int idx = 0;
		int contentLen = content.length();
		if (contentLen > 100000)
			// extract URLs only from the first 100 kB
			contentLen = 100000;
		while((idx < contentLen) && m.find()) {
			idx = m.end();
			String url = m.group(1);
			url = cssUrlTrimPattern.matcher(url).replaceAll("");
			if (!url.isEmpty()) {
				data.addHref("path","STYLE/#text","href", url);
			}
		}
	}

	private static String patternJSExtract(Pattern pattern, String content) {
		Matcher m = pattern.matcher(content);
		if (m.find()) {
			return m.group(2);
		}
		return null;
	}
}
