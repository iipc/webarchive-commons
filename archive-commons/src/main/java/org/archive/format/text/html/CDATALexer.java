package org.archive.format.text.html;

import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.util.ParserException;

public class CDATALexer extends Lexer {
	private static final long serialVersionUID = -8513653556979405106L;
	private Node cached;
	private boolean inCSS;
	private boolean inJS;
	private boolean cachedJS = false;

	@Override
	public Node nextNode() throws ParserException {
		inJS = false;
		inCSS = false;
		if(cached != null) {
			Node tmp = cached;
			cached = null;
			inJS = cachedJS;
			inCSS = !cachedJS;
			return tmp;
		}
		Node got = super.nextNode();
		if(NodeUtils.isNonEmptyOpenTagNodeNamed(got, "SCRIPT")) {
			cached = super.parseCDATA(true);
			cachedJS = true;
		} else if (NodeUtils.isNonEmptyOpenTagNodeNamed(got, "STYLE")) {
			cached = super.parseCDATA(true);
			cachedJS = false;
		}
		return got;
	}
	public boolean inJS() {
		return inJS;
	}
	public boolean inCSS() {
		return inCSS;
	}
}
