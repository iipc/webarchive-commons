package org.archive.format.text.html;

import org.htmlparser.Node;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.ParserException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CDATALexerTest {
	CDATALexer l;
	Node n;
	private CDATALexer makeLexer(String html) {
		CDATALexer t = new CDATALexer();
		t.setPage(new Page(html));
		return t;
	}

	@Test
	public void testNextNode() throws ParserException {
		l = makeLexer("<a href=\"foo\">blem</a>");
		n = l.nextNode();
		assertFalse(l.inCSS());
		assertFalse(l.inJS());
		assertTrue(NodeUtils.isNonEmptyOpenTagNodeNamed(n, "A"));
		assertEquals("foo",((TagNode)n).getAttribute("HREF"));
		n = l.nextNode();
		assertTrue(NodeUtils.isTextNode(n));
		assertEquals("blem",((TextNode)n).getText());
		n = l.nextNode();
		assertTrue(NodeUtils.isCloseTagNodeNamed(n, "A"));
		assertNull(l.nextNode());
	}

	@Test
	public void testInJS() throws ParserException {
		l = makeLexer("<script>foo bar baz</script>");
		assertFalse(l.inCSS());
		assertFalse(l.inJS());
		n = l.nextNode();
		assertFalse(l.inCSS());
		assertFalse(l.inJS());
		assertTrue(NodeUtils.isNonEmptyOpenTagNodeNamed(n, "SCRIPT"));
		n = l.nextNode();
		assertFalse(l.inCSS());
		assertTrue(l.inJS());
		assertTrue(NodeUtils.isTextNode(n));
		assertEquals("foo bar baz",((TextNode)n).getText());
		n = l.nextNode();
		assertFalse(l.inCSS());
		assertFalse(l.inJS());
		assertTrue(NodeUtils.isCloseTagNodeNamed(n, "SCRIPT"));
	}

	@Test
	public void testInCSS() throws ParserException {
		l = makeLexer("<style>foo bar baz</style>");
		assertFalse(l.inCSS());
		assertFalse(l.inJS());
		n = l.nextNode();
		assertFalse(l.inCSS());
		assertFalse(l.inJS());
		assertTrue(NodeUtils.isNonEmptyOpenTagNodeNamed(n, "STYLE"));
		n = l.nextNode();
		assertTrue(l.inCSS());
		assertFalse(l.inJS());
		assertTrue(NodeUtils.isTextNode(n));
		assertEquals("foo bar baz",((TextNode)n).getText());
		n = l.nextNode();
		assertFalse(l.inCSS());
		assertFalse(l.inJS());
		assertTrue(NodeUtils.isCloseTagNodeNamed(n, "STYLE"));
	}
	
	public void testInJSComment() throws ParserException {
		
//		dumpParse("<script>//<!--\n foo bar baz\n //--></script>");
//		dumpParse("<script><!-- foo bar baz --></script>");
//		dumpParse("<script>//<!-- foo bar baz --></script>");
//		dumpParse("<script><!-- foo bar baz //--></script>");
//		dumpParse("<script>\n//<!-- foo bar baz\n //--></script>");
//		dumpParse("<script> if(1 < 2) { foo(); } </script>");
//		dumpParse("<script> if(1 <n) { foo(); } </script>");
//		dumpParse("<script> document.write(\"<b>bold</b>\"); </script>");
//		dumpParse("<script> document.write(\"<script>bold</script>\"); </script>");
//		dumpParse("<script> <![CDATA[\n if(i<n) { foo() } // content of your Javascript goes here \n ]]> </script>");

		assertJSContentWorks("//<!--\n foo bar baz\n //-->");
		assertJSContentWorks("<!-- foo bar baz -->");
		assertJSContentWorks("//<!-- foo bar baz -->");
		assertJSContentWorks("<!-- foo bar baz //-->");
		assertJSContentWorks("\n//<!-- foo bar baz\n //-->");
		assertJSContentWorks("if(1 < 2) { foo(); } ");
		assertJSContentWorks("if(1 <n) { foo(); } ");
		assertJSContentWorks("document.write(\"<b>bold</b>\"); ");
		assertJSContentWorks("document.write(\"<script>bold</script>\"); ");
		assertJSContentWorks("<![CDATA[\n if(i<n) { foo() } // a comment \n ]]> ");

	}
	
	private void assertJSContentWorks(String js) throws ParserException {
		String html = String.format("<script>%s</script>",js);
		l = makeLexer(html);
		assertFalse(l.inCSS());
		assertFalse(l.inJS());
		n = l.nextNode();
		assertFalse(l.inCSS());
		assertFalse(l.inJS());
		assertTrue(NodeUtils.isNonEmptyOpenTagNodeNamed(n, "SCRIPT"));
		n = l.nextNode();
		assertFalse(l.inCSS());
		assertTrue(l.inJS());
		assertTrue(NodeUtils.isTextNode(n));
		assertEquals(js,((TextNode)n).getText());
		n = l.nextNode();
		assertFalse(l.inCSS());
		assertFalse(l.inJS());
		assertTrue(NodeUtils.isCloseTagNodeNamed(n, "SCRIPT"));
	}
	
	
//	private void dumpParse(String html) throws ParserException {
//		System.out.println("SOPARSE("+html+")");
//		l = makeLexer(html);
//		while(true) {
//			n = l.nextNode();
//			if(n == null) {
//				break;
//			}
//			String state = String.format("%s%s", 
//					l.inCSS() ? "C" : "", l.inJS() ? "J" : "");
//			if(NodeUtils.isRemarkNode(n)) {
//				System.out.format("---COMMENT(%s)(%s)\n", state, ((RemarkNode)n).getText());
//			} else if(NodeUtils.isTextNode(n)) {
//				System.out.format("---TEXT(%s)(%s)\n", state, ((TextNode)n).getText());				
//			} else {
//				TagNode tn = (TagNode) n;
//				if(tn.isEmptyXmlTag()) {
//					System.out.format("---EMPTY(%s)(%s)\n", state, tn.getTagName());
//				} else if(tn.isEndTag()) {
//					System.out.format("---END(%s)(%s)\n", state, tn.getTagName());					
//				} else {
//					System.out.format("---OPEN(%s)(%s)\n", state, tn.getTagName());					
//				}
//			}
//		}
//		System.out.println("EOPARSE");
//	}
	
	
}
