package org.archive.format.text.html;

import java.io.IOException;
import java.io.Writer;

import org.htmlparser.Node;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.ParserException;

public class LexParser extends NodeUtils {
	ParseObserver obs;
	public LexParser(ParseObserver obs) {
		this.obs = obs;
	}
	public void doParse(CDATALexer lex) throws ParserException, IOException {
		doParse(lex,null);
	}
	
	public void doParse(CDATALexer lex, Writer w) throws ParserException, IOException {
		obs.handleDocumentStart();
		Node n;
		TextNode tx;
		TagNode tn;
		while(true) {
			n = lex.nextNode();
			if(n == null) {
				break;
			}
			if(isRemarkNode(n)) {
				obs.handleRemarkNode((RemarkNode)n);
			} else if(isTextNode(n)) {
				tx = (TextNode) n;
				if(lex.inCSS()) {
					obs.handleStyleNode(tx);
				} else if(lex.inJS()) {
					obs.handleScriptNode(tx);
				} else {
					obs.handleTextNode(tx);
				}
			} else {
				tn = (TagNode) n;
				if(tn.isEmptyXmlTag()) {
					obs.handleTagEmpty(tn);
				} else if(tn.isEndTag()) {
					obs.handleTagClose(tn);
				} else {
					obs.handleTagOpen(tn);
				}
			}
			if(w != null) {
				w.write(n.toHtml(true));
			}
		}
		obs.handleDocumentComplete();
	}
}
