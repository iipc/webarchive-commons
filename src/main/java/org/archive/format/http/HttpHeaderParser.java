package org.archive.format.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HttpHeaderParser implements HttpConstants {
	private static final int DEFAULT_MAX_NAME_LENGTH = 1024 * 100;
	private static final int DEFAULT_MAX_VALUE_LENGTH = 1024 * 1024 * 10;
	private HttpHeaderObserver obs = null;
	private ParseState state = null;
	public boolean isStrict = false;
	
	private int nameStartIdx = 0;
	private int nameLength = 0;
	private byte name[] = null;

	private int valueStartIdx = 0;
	private int valueLength = 0;
	private byte value[] = null;
	
	private int bufferIdx = 0;
	
	private ParseState startState = new StartParseState();
	private ParseState endState = new EndParseState();
	private ParseState lineStartState = new LineStartParseState();
	private ParseState nameState = new NameParseState();
	private ParseState postNameState = new PostNameParseState();
	private ParseState postColonState = new PostColonParseState();
	private ParseState valueState = new ValueParseState();
	private ParseState valuePostLWSPState = new ValuePostLWSPParseState();
	private ParseState valuePostCRState = new ValuePostCRParseState();
	private ParseState postBlankCRState = new PostBlankCRParseState();
	private ParseState laxLineEatParseState = new LAXLineEatParseState();
	private ParseState valuePreCRState = null;
	
	public HttpHeaderParser() {
		this(null,DEFAULT_MAX_NAME_LENGTH, DEFAULT_MAX_VALUE_LENGTH);
	}

	public HttpHeaderParser(HttpHeaderObserver obs) {
		this(obs,DEFAULT_MAX_NAME_LENGTH, DEFAULT_MAX_VALUE_LENGTH);
	}
	
	public HttpHeaderParser(HttpHeaderObserver obs, int maxName, int maxValue) {
		name = new byte[maxName];
		value = new byte[maxValue];
		this.obs = obs;
		reset();
	}
	public void setObserver(HttpHeaderObserver obs) {
		this.obs = obs;
	}
	private void reset() {
		state = startState;
		bufferIdx = 0;

		nameStartIdx = 0;
		nameLength = 0;
		
		valueStartIdx = 0;
		valueLength = 0;
	}
	
	public int doParse(InputStream is, HttpHeaderObserver obs) 
	throws HttpParseException, IOException {
		this.obs = obs;
		return doParse(is);
	}
	
	public HttpHeaders parseHeaders(InputStream is) 
	throws HttpParseException, IOException {
		HttpHeaders headers = new HttpHeaders();
		obs = headers;
		doParse(is);
		return headers;
	}

	public int doParse(InputStream is) 
		throws HttpParseException, IOException {

		int bytesRead = 0;

		reset();
		while(!isDone()) {
			int i = is.read();
			if(i == -1) {
				if(isStrict) {
					throw new HttpParseException("EOF before CRLFCRLF");
				}
				headersCorrupted();
				return bytesRead;
			}
			bytesRead++;
			if(i > 127) {
				if(isStrict) {
					throw new HttpParseException("Non ASCII byte in headers");
				}
				headersCorrupted();
				continue;
			}
			byte b = (byte) (i & 0xff);
			parseByte(b);
		}

		return bytesRead;
	}
	
	public boolean isDone() {
		return state instanceof EndParseState;
	}

	public void parseByte(byte b) throws HttpParseException {
		state = state.handleByte(b,this);
		bufferIdx++;
	}

	private void headerFinished() {
		// skip empty:
		if(nameLength == 0) {
			return;
		}
		if(valueLength > 0) {
			if(value[valueLength-1] == SP) {
				valueLength--;
			}
		}
		if(obs != null) {
			obs.headerParsed(name, nameStartIdx, nameLength, 
					value, valueStartIdx, valueLength);
		}
	}

	private void parseFinished() {
		if(obs != null) {
			obs.headersComplete(bufferIdx+1);
		}
	}
	private void headersCorrupted() {
		if(obs != null) {
			obs.headersCorrupt();
		}
	}

	private void setNameStartPos() {
		nameStartIdx = bufferIdx;
		nameLength = 0;
	}

	private void addNameByte(byte b) throws HttpParseException {
		if(nameLength >= name.length) {
			throw new HttpParseException("Name too long");
		}
		name[nameLength] = b;
		nameLength++;
	}

	private void setValueStartIdx() {
		valueStartIdx = bufferIdx;
		valueLength = 0;
	}

	private void addValueByte(byte b) throws HttpParseException {
		// ignore leading SP:
		if(b == SP) {
			if(valueLength == 0) {
				return;
			}
			if(value[valueLength-1] == SP) {
				return;
			}
		}
		if(valueLength >= value.length) {
			throw new HttpParseException("Value too long");
		}
		value[valueLength] = b;
		valueLength++;
	}

	private interface ParseState {
		public ParseState handleByte(byte b, HttpHeaderParser parser)
		throws HttpParseException;
	}

	private class EndParseState implements ParseState {
		public ParseState handleByte(byte b, HttpHeaderParser parser)
		throws HttpParseException {
			throw new HttpParseException("Parse already completed");
		}
	}

	private class StartParseState implements ParseState {
		public ParseState handleByte(byte b, HttpHeaderParser parser)
		throws HttpParseException {
			if(isLWSP(b)) {
				if(parser.isStrict) {
					throw new HttpParseException("Space at start of headers");
				}
				// skip i guess...
				parser.headersCorrupted();
				return parser.startState;
			}
			if(isLegalNameByte(b)) {
				parser.setNameStartPos();
				parser.addNameByte(b);
				return parser.nameState;
			}
			if(parser.isStrict) {
				throw new HttpParseException("Bad character at start of headers");
			}
			parser.headersCorrupted();
			return parser.laxLineEatParseState;
		}
	}

	private class LineStartParseState implements ParseState {
		public ParseState handleByte(byte b, HttpHeaderParser parser)
		throws HttpParseException {
			if(isLWSP(b)) {
				parser.addValueByte(SP);
				return parser.valuePostLWSPState;
			}
			if(isLegalNameByte(b)) {
				parser.headerFinished();
				parser.setNameStartPos();
				parser.addNameByte(b);
				return parser.nameState;
			}
			if(b == CR) {
				return parser.postBlankCRState;
			}
			if(b == LF) {
				// TODO: this is lax, is LFLF an OK terminator?
				// that's all folks!
				parser.headerFinished();
				parser.parseFinished();
				return parser.endState;
			}
			if(parser.isStrict) {
				throw new HttpParseException("Bad character at start of line");
			}
			parser.headersCorrupted();
			return parser.laxLineEatParseState;
		}
	}
	
	private class LAXLineEatParseState implements ParseState {
		public ParseState handleByte(byte b, HttpHeaderParser parser)
		throws HttpParseException {
			if(b == CR) {
				return parser.valuePostCRState;
			}
			if(b == LF) {
				return parser.lineStartState;
			}
			return parser.laxLineEatParseState;
		}
	}

	private class NameParseState implements ParseState {
		public ParseState handleByte(byte b, HttpHeaderParser parser)
		throws HttpParseException {
			if(isLegalNameByte(b)) {
				parser.addNameByte(b);
				return this;
			}
			if(isLWSP(b)) {
				return parser.postNameState;
			}
			if(b == COLON) {
				return parser.postColonState;
			}
			if(parser.isStrict) {
				throw new HttpParseException("Illegal name char");
			}
			parser.headersCorrupted();
			return parser.laxLineEatParseState;
		}
	}
	
	private class PostNameParseState implements ParseState {
		public ParseState handleByte(byte b, HttpHeaderParser parser)
		throws HttpParseException {
			if(isLWSP(b)) {
				// ignore more spaces..
				return parser.postNameState;
			}
			if(b == COLON) {
				return parser.postColonState;
			}
			if(parser.isStrict) {
				throw new HttpParseException("Illegal char after name("
						+ new String(name, 0, nameLength, StandardCharsets.ISO_8859_1) + ")");
			}
			parser.headersCorrupted();
			return parser.laxLineEatParseState;
		}
	}
	

	private class PostColonParseState implements ParseState {
		public ParseState handleByte(byte b, HttpHeaderParser parser) throws HttpParseException {
			if(isLWSP(b)) {
				return parser.postColonState;
			}
			// reset previous value also in case the header value is empty
			parser.setValueStartIdx();
			if(b == CR) {
				parser.valuePreCRState = parser.postColonState;
				return parser.valuePostCRState;
			}
			if(b == LF) {
				// TODO: this is lax, is LFLF an OK terminator?
				return parser.lineStartState;
			}
			parser.addValueByte(b);
			return parser.valueState;
		}
	}
	
	private class ValueParseState implements ParseState {
		public ParseState handleByte(byte b, HttpHeaderParser parser) throws HttpParseException {
			if(isLWSP(b)) {
				parser.addValueByte(SP);
				return parser.valuePostLWSPState;
			}
			if(b == CR) {
				parser.valuePreCRState = this;
				return parser.valuePostCRState;
			}
			if(b == LF) {
				// TODO: this is lax, is LFLF an OK terminator?
				return parser.lineStartState;
			}
			parser.addValueByte(b);
			return this;
		}
	}
	
	private class ValuePostLWSPParseState implements ParseState {
		public ParseState handleByte(byte b, HttpHeaderParser parser) throws HttpParseException {
			if(isLWSP(b)) {
				// skip, already added a space:
				return parser.valuePostLWSPState;
			}
			if(b == CR) {
				parser.valuePreCRState = this;
				return parser.valuePostCRState;
			}
			if(b == LF) {
				// TODO: this is lax, is LFLF an OK terminator?
				return parser.lineStartState;
			}
			parser.addValueByte(b);
			return parser.valueState;
		}
	}
	
	private class ValuePostCRParseState implements ParseState {
		public ParseState handleByte(byte b, HttpHeaderParser parser) throws HttpParseException {
			if(isLWSP(b)) {
				// ignore last CR. lax?
				return parser.valuePreCRState;
			}
			if(b == CR) {
				// TODO: this is lax, is LFLF an OK terminator?
				return parser.valuePostCRState;
			}
			if(b == LF) {
				return parser.lineStartState;
			}
			parser.addValueByte(b);
			return parser.valueState;
		}
	}
	private class PostBlankCRParseState implements ParseState {
		public ParseState handleByte(byte b, HttpHeaderParser parser) throws HttpParseException {
			if(b == LF) {
				parser.headerFinished();
				// that's all folks!
				parser.parseFinished();
				return parser.endState;
			}
			if(parser.isStrict) {
				throw new HttpParseException("NON LF after blank CR");
			}
			parser.headersCorrupted();
			// TODO: is this the right state?
			return parser.laxLineEatParseState;
		}
	}

//	private boolean isTEXT(int b) {
//		if((b > 31) && (b < 256)) {
//			// anything but 127
//			return b != 127;
//		}
//		if(b == 10) {
//			return true;
//		}
//		return (b == 13);
//	}
	
	
	private static boolean isLWSP(byte b) {
		return (b == SP) || (b == HTAB);
	}
	/**
	 * any CHAR, excluding CTLs, SPACE, and ":" 
	 * @param b
	 * @return
	 */
	private static boolean isLegalNameByte(byte b) {
		if(b > 31) {
			if(b < 128) {
				return b == SP ? false : b != COLON;
			}
		}
		return false;
	}
}
