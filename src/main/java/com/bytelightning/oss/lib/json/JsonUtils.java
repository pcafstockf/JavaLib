package com.bytelightning.oss.lib.json;

import java.io.IOException;

/**
 * JSON processing utilities used by the other parser's and saxer's in this package.
 *
 */
public class JsonUtils {

	protected JsonUtils() {
	}

	public static String UnEscapeJsonString(final String s) {
		final StringBuilder sb = new StringBuilder();
		final int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c == '\\') {
				if (i + 1 < len) {
					switch (s.charAt(i + 1)) {
					case '"':
						sb.append('"');
						i++;
						break;
					case '\\':
						sb.append('\\');
						i++;
						break;
					case '/':
						sb.append('/');
						i++;
						break;
					case 'b':
						sb.append('\b');
						i++;
						break;
					case 'f':
						sb.append('\f');
						i++;
						break;
					case 'n':
						sb.append('\n');
						i++;
						break;
					case 'r':
						sb.append('\r');
						i++;
						break;
					case 't':
						sb.append('\t');
						i++;
						break;
					case 'u':
						final int g1s = i + 2;
						int g1e = g1s;
						do {
							g1e++;
							if (g1e > len)
								return null;
							if (g1e > g1s + 8)
								return null;
							if (g1e == len)
								break;
							if (!Character.isDigit(s.charAt(g1e)))
								break;
						}
						while (true);
						if (g1e - g1s < 2)
							return null;
						String g = s.substring(g1s, g1e);
						i = g1e - 1;
						if (g1e < len && s.charAt(g1e) == '\\') {
							if (g1e + 1 >= len)
								return null;
							if (s.charAt(g1e + 1) == 'u') {
								if (g.length() > 4)
									return null;
								final int g2s = g1e + 2;
								int g2e = g2s;
								do {
									g2e++;
									if (g2e > len)
										return null;
									if (g2e > g2s + 8)
										return null;
									if (g2e == len)
										break;
									if (!Character.isDigit(s.charAt(g2e)))
										break;
								}
								while (true);
								if (g2e - g2s < 2)
									return null;
								g = g + s.substring(g2s, g2e);
								i = g2e - 1;
							}
						}
						int cp = Integer.parseInt(g, 16);
						sb.append(Character.toChars(cp));
						break;
					default:
						return null;
					}
				}
				else
					return null;
			}
			else
				sb.append(c);
		}
		return sb.toString();
	}
    
	public static String EscapeJsonString(String s) {
		final StringBuilder sb = new StringBuilder();
		final int len = s.length();
		char[] chars = null;
		for (int i = 0; i < len; i++) {
			final char c = s.charAt(i);
			switch (c) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				if (c <= 0x001F || c > 0x00FF) { // Spec doesn't say we have to encode, but by doing so, we dis-ambiguate the char encoding of the document.
					if (chars == null)
						chars = s.toCharArray();
					int cp = Character.codePointAt(chars, i);
					if (cp > 0x0FFFF) {
						sb.append("\\u");
						sb.append(Integer.toHexString((cp & 0xFFFF0000) >> 16));
						sb.append("\\u");
						sb.append(Integer.toHexString(cp & 0x0000FFFF));
					}
					else {
						sb.append("\\u");
						sb.append(Integer.toHexString(cp));
					}
				}
				else
					sb.append(c);
				break;
			}
		}
		return sb.toString();
	}
    
    public static void main(String[] args) throws IOException {
//    	EscapeJsonString("\\\"How\bquickly\tdaft\njumping\fzebras\rvex\"");
    	UnEscapeJsonString("\"\\u2028\"");
		String expectedStr = "{\"outer\":\"{\\\"inner\\\":\\\"don\\\\t even\\\"}\"}";
		Object o = JSON.parse(expectedStr);
		System.out.println(JSON.stringify(o));
    }
}
