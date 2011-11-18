package org.archive.format.text.html;

import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;

public interface ParseObserver {
	public void handleDocumentStart();
	public void handleDocumentComplete();
	
	public void handleTagEmpty(TagNode tag);
	public void handleTagOpen(TagNode tag);
	public void handleTagClose(TagNode tag);

	public void handleTextNode(TextNode text);
	public void handleScriptNode(TextNode text);
	public void handleStyleNode(TextNode text);

	public void handleRemarkNode(RemarkNode remark);
}
