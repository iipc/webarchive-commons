package org.archive.url;

import java.net.IDN;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.net.InetAddresses;

/**
 * Canonicalizer that does more or less basic fixup. Based initially on rules
 * specified at <a href=
 * "https://web.archive.org/web/20130306015559/https://developers.google.com/safe-browsing/developers_guide_v2#Canonicalization"
 * >https://developers.google.com/safe-browsing/developers_guide_v2#
 * Canonicalization</a>. These rules are designed for clients of Google's
 * "experimental" Safe Browsing API to "check URLs against Google's
 * constantly-updated blacklists of suspected phishing and malware pages".
 * 
 * <p>
 * This class differs from Google in treatment of non-ascii input. Google's
 * rules don't really address this except with one example test case, which
 * seems to suggest taking raw input bytes and pct-encoding them byte for byte.
 * Since the input to this class consists of java strings, not raw bytes, that
 * wouldn't be possible, even if deemed preferable. Instead,
 * BasicURLCanonicalizer expresses non-ascii characters pct-encoded UTF-8.
 */
public class BasicURLCanonicalizer implements URLCanonicalizer {
	Pattern OCTAL_IP = Pattern
			.compile("^(0[0-7]*)(\\.[0-7]+)?(\\.[0-7]+)?(\\.[0-7]+)?$");
	Pattern DECIMAL_IP = Pattern
			.compile("^([1-9][0-9]*)(\\.[0-9]+)?(\\.[0-9]+)?(\\.[0-9]+)?$");
	Pattern MULTIDOT = Pattern.compile("\\.{2,}");

	@Override
	public void canonicalize(HandyURL url) {
		url.setHash(null);
		url.setAuthUser(minimalEscape(url.getAuthUser()));
		url.setAuthPass(minimalEscape(url.getAuthPass()));

		url.setQuery(minimalEscape(url.getQuery()));
		String hostE = unescapeRepeatedly(url.getHost());
		String host = null;
		if (hostE != null) {
			try {
				host = IDN.toASCII(hostE);
			} catch (IllegalArgumentException e) {
				if (!e.getMessage().contains(
						"A prohibited code point was found")) {
					// TODO: What to do???
					// throw e;
				}
				host = hostE;

			}
			host = normalizeDots(host);
		}

		String ip = null;
		ip = attemptIPFormats(host);
		if (ip != null) {
			host = ip;
		} else if (host != null) {
			host = escapeOnce(host.toLowerCase(Locale.ROOT));
		}
		url.setHost(host);
		// now the path:

		String path = unescapeRepeatedly(url.getPath());

		url.setPath(escapeOnce(normalizePath(path)));
	}

	/**
	 * Normalize dots in the host name.
	 *
	 * @param host
	 * @return host name with all sequences of dots replaced with a single dot,
	 *         and all leading and trailing dots removed
	 */
	private String normalizeDots(String host) {
		if (host.indexOf('.') == -1) {
			return host;
		}
		int start = 0, end = host.length();
		boolean changed = false;
		while (start < end && host.charAt(start) == '.') {
			start++;
			changed = true;
		}
		while (end > start && host.charAt(end - 1) == '.') {
			end--;
			changed = true;
		}
		if (changed) {
			host = host.substring(start, end);
		}
		if (host.contains("..")) {
			host = MULTIDOT.matcher(host).replaceAll(".");
		}
		return host;
	}

	private static final Pattern SINGLE_FORWARDSLASH_PATTERN = Pattern
			.compile("/");

	public String normalizePath(String path) {
		if (path == null) {
			path = "/";
		} else {
			// -1 gives an empty trailing element if path ends with '/':
			String[] paths = SINGLE_FORWARDSLASH_PATTERN.split(path, -1);
			ArrayList<String> keptPaths = new ArrayList<String>();
			boolean first = true;
			for (String p : paths) {
				if (first) {
					first = false;
					continue;
				} else if (p.compareTo(".") == 0) {
					// skip
					continue;
				} else if (p.compareTo("..") == 0) {
					// pop the last path, if present:
					if (keptPaths.size() > 0) {
						keptPaths.remove(keptPaths.size() - 1);
					} else {
						// TODO: leave it? let's do for now...
						keptPaths.add(p);
					}
				} else {
					keptPaths.add(p);
				}
			}
			int numKept = keptPaths.size();
			if (numKept == 0) {
				path = "/";
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("/");
				for (int i = 0; i < numKept - 1; i++) {
					String p = keptPaths.get(i);
					if (p.length() > 0) {
						// this will omit multiple slashes:
						sb.append(p).append("/");
					}
				}
				sb.append(keptPaths.get(numKept - 1));
				path = sb.toString();
			}
		}
		return path;
	}

	public String attemptIPFormats(String host) { // throws URIException {
		if (host == null) {
			return null;
		}
		if (host.matches("^\\d+$")) {
			try {
				Long l = Long.parseLong(host);
				return InetAddresses.fromInteger(l.intValue()).getHostAddress();
			} catch (NumberFormatException e) {
			}
		} else {
			// check for octal:
			Matcher m = OCTAL_IP.matcher(host);
			if (m.matches()) {
				int parts = m.groupCount();
				if (parts > 4) {
					// WHAT TO DO?
					return null;
					// throw new URIException("Bad Host("+host+")");
				}
				int[] ip = new int[] { 0, 0, 0, 0 };
				for (int i = 0; i < parts; i++) {
					int octet;
					try {
						octet = Integer.parseInt(
								m.group(i + 1).substring((i == 0) ? 0 : 1), 8);
					} catch (Exception e) {
						return null;
					}
					if ((octet < 0) || (octet > 255)) {
						return null;
						// throw new URIException("Bad Host("+host+")");
					}
					ip[i] = octet;
				}
				return String.format(Locale.ROOT, "%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]);
			} else {
				Matcher m2 = DECIMAL_IP.matcher(host);
				if (m2.matches()) {
					int parts = m2.groupCount();
					if (parts > 4) {
						// WHAT TO DO?
						return null;
						// throw new URIException("Bad Host("+host+")");
					}
					int[] ip = new int[] { 0, 0, 0, 0 };
					for (int i = 0; i < parts; i++) {

						String m2Group = m2.group(i + 1);
						if (m2Group == null)
							return null;
						// int octet =
						// Integer.parseInt(m2.group(i+1).substring((i==0)?0:1));
						int octet;
						try {
							octet = Integer.parseInt(m2Group
									.substring((i == 0) ? 0 : 1));
						} catch (Exception e) {
							return null;
						}
						if ((octet < 0) || (octet > 255)) {
							return null;
							// throw new URIException("Bad Host("+host+")");
						}
						ip[i] = octet;
					}
					return String.format(Locale.ROOT, "%d.%d.%d.%d", ip[0], ip[1], ip[2],
							ip[3]);

				}
			}
		}
		return null;
	}

	public String minimalEscape(String input) {
		return escapeOnce(unescapeRepeatedly(input));
	}

	protected static Charset _UTF8 = StandardCharsets.UTF_8;

	protected static Charset UTF8() {
		return _UTF8;
	}

	/**
	 * @param input String to be percent-encoded. Assumed to be fully unescaped.
	 * @return percent-encoded string
	 */
	public String escapeOnce(String input) {
		if (input == null) {
			return null;
		}

		byte[] utf8bytes = input.getBytes(UTF8());
		StringBuilder sb = null;
		boolean ok = false;

		for (int i = 0; i < utf8bytes.length; i++) {
			int b = utf8bytes[i] & 0xff;
			ok = false;
			if (b > 32) {
				if (b < 128) {
					if (b != '#') {
						ok = (b != '%');
					}
				}
			}
			if (ok) {
				if (sb != null) {
					sb.append((char) b);
				}
			} else {
				if (sb == null) {
					/*
					 * everything up to this point has been an ascii character
					 * not needing escaping
					 */
					sb = new StringBuilder(input.substring(0, i));
				}
				if (b == '%' && i < utf8bytes.length - 2) {
					// Any hex escapes left at this point represent non-UTF-8 encoded characters
					// Unescape them, so they don't get double escaped
					int hex1 = getHex(utf8bytes[i + 1]);
					if (hex1 >= 0) {
						int hex2 = getHex(utf8bytes[i + 2]);
						if (hex2 >= 0) {
							i = i+2;
							b = hex1 * 16 + hex2;
						}
					}

				}
				sb.append("%");
				String hex = Integer.toHexString(b).toUpperCase(Locale.ROOT);
				if (hex.length() == 1) {
					sb.append('0');
				}
				sb.append(hex);
			}
		}
		if (sb == null) {
			return input;
		}
		return sb.toString();
	}

	public String unescapeRepeatedly(String input) {
		if (input == null) {
			return null;
		}
		while (true) {
			String un = decode(input);
			if (un.compareTo(input) == 0) {
				return input;
			}
			input = un;
		}
	}

	public String decode(String input) {
		StringBuilder sb = null;
		int pctUtf8SeqStart = -1;
		ByteBuffer bbuf = null;
		CharsetDecoder utf8decoder = null;
		int i = 0;
		int h1, h2;
		while (i < input.length()) {
			char c = input.charAt(i);
			if (i <= input.length() - 3 && c == '%'
					&& (h1 = getHex(input.charAt(i + 1))) >= 0
					&& (h2 = getHex(input.charAt(i + 2))) >= 0) {
				if (sb == null) {
					sb = new StringBuilder(input.length());
					if (i > 0) {
						sb.append(input.substring(0, i));
					}
				}
				int b = ((h1 << 4) + h2) & 0xff;
				if (pctUtf8SeqStart < 0 && b < 0x80) { // plain ascii
					sb.append((char) b);
				} else {
					if (pctUtf8SeqStart < 0) {
						pctUtf8SeqStart = i;
						if (bbuf == null) {
							bbuf = ByteBuffer
									.allocate((input.length() - i) / 3);
						}
					}
					bbuf.put((byte) b);
				}
				i += 3;
			} else {
				if (pctUtf8SeqStart >= 0) {
					if (utf8decoder == null) {
						utf8decoder = UTF8().newDecoder();
					}
					appendDecodedPctUtf8(sb, bbuf, input, pctUtf8SeqStart, i,
							utf8decoder);
					pctUtf8SeqStart = -1;
					bbuf.clear();
				}
				if (sb != null) {
					sb.append(c);
				}
				i++;
			}
		}
		if (pctUtf8SeqStart >= 0) {
			if (utf8decoder == null) {
				utf8decoder = UTF8().newDecoder();
			}
			appendDecodedPctUtf8(sb, bbuf, input, pctUtf8SeqStart, i,
					utf8decoder);
		}

		if (sb != null) {
			return sb.toString();
		} else {
			return input;
		}
	}

	/**
	 * Decodes bytes in bbuf as utf-8 and appends decoded characters to sb. If
	 * decoding of any portion fails, appends the un-decodable %xx%xx sequence
	 * extracted from inputStr instead of decoded characters. See "bad unicode"
	 * tests in BasicURLCanonicalizerTest#testDecode(). Variables only make sense
	 * within context of {@link #decode(String)}.
	 * 
	 * @param sb
	 *            StringBuilder to append to
	 * @param bbuf
	 *            raw bytes decoded from %-encoded input
	 * @param inputStr
	 *            full input string
	 * @param seqStart
	 *            start index inclusive within inputStr of %-encoded sequence
	 * @param seqEnd
	 *            end index exclusive within inputStr of %-encoded sequence
	 * @param utf8decoder
	 */
	private void appendDecodedPctUtf8(StringBuilder sb, ByteBuffer bbuf,
			String inputStr, int seqStart, int seqEnd,
			CharsetDecoder utf8decoder) {
		// assert bbuf.position() * 3 == seqEnd - seqStart;
		utf8decoder.reset();
		CharBuffer cbuf = CharBuffer.allocate(bbuf.position());
		bbuf.flip();
		while (bbuf.position() < bbuf.limit()) {
			CoderResult coderResult = utf8decoder.decode(bbuf, cbuf, true);
			sb.append(cbuf.flip());
			if (coderResult.isMalformed()) {
				// put the malformed %xx%xx into the result un-decoded
				CharSequence undecodablePctHex = inputStr.subSequence(seqStart
						+ 3 * bbuf.position(), seqStart + 3 * bbuf.position()
						+ 3 * coderResult.length());
				sb.append(undecodablePctHex);

				// there could be more good stuff after the bad
				bbuf.position(bbuf.position() + coderResult.length());
			}
			cbuf.clear();
		}
	}

	public int getHex(final int b) {
		if (b < '0') {
			return -1;
		}
		if (b <= '9') {
			return b - '0';
		}
		if (b < 'A') {
			return -1;
		}
		if (b <= 'F') {
			return 10 + (b - 'A');
		}
		if (b < 'a') {
			return -1;
		}
		if (b <= 'f') {
			return 10 + (b - 'a');
		}
		return -1;
	}

}
