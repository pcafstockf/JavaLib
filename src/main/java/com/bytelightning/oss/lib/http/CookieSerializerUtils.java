/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bytelightning.oss.lib.http;

import java.net.HttpCookie;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.BitSet;
import java.util.Date;

/**
 * The set of Cookie utility methods for cookie serialization.
 *
 * @author Grizzly team
 */
public class CookieSerializerUtils {

	// TODO RFC2965 fields also need to be passed
	public static void serializeServerCookie(StringBuilder buf, HttpCookie cookie) {
		serializeServerCookie(buf, CookieUtils.COOKIE_VERSION_ONE_STRICT_COMPLIANCE, CookieUtils.RFC_6265_SUPPORT_ENABLED, CookieUtils.ALWAYS_ADD_EXPIRES, cookie);
	}

	// TODO RFC2965 fields also need to be passed
	public static void serializeServerCookie(final StringBuilder buf, final boolean versionOneStrictCompliance, final boolean rfc6265Support, final boolean alwaysAddExpires, final HttpCookie cookie) {

		serializeServerCookie(buf, versionOneStrictCompliance, rfc6265Support, alwaysAddExpires, cookie.getName(), cookie.getValue(), cookie.getVersion(), cookie.getPath(), cookie.getDomain(), cookie.getComment(), cookie.getMaxAge(), cookie.getSecure(),
						false /* uncomment when we move to jdk 1.7 cookie.isHttpOnly() */);
	}

	// TODO RFC2965 fields also need to be passed
	public static void serializeServerCookie(final StringBuilder buf, final boolean versionOneStrictCompliance, final boolean rfc6265Support, final boolean alwaysAddExpires, final String name, final String value, int version, String path,
					final String domain, final String comment, final long maxAge, final boolean isSecure, final boolean isHttpOnly) {
		// Servlet implementation checks name
		buf.append(name);
		buf.append('=');
		// Servlet implementation does not check anything else

		version = maybeQuote2(version, buf, value, true, rfc6265Support);

		// Add version 1 specific information
		if (version == 1) {
			// Version=1 ... required
			buf.append("; Version=1");

			// Comment=comment
			if (comment != null) {
				buf.append("; Comment=");
				maybeQuote2(version, buf, comment, versionOneStrictCompliance, rfc6265Support);
			}
		}

		// Add domain information, if present
		if (domain != null) {
			buf.append("; Domain=");
			maybeQuote2(version, buf, domain, versionOneStrictCompliance, rfc6265Support);
		}

		// Max-Age=secs ... or use old "Expires" format
		// TODO RFC2965 Discard
		if (maxAge >= 0) {
			if (version > 0) {
				buf.append("; Max-Age=");
				buf.append(maxAge);
			}
			// IE6, IE7 and possibly other browsers don't understand Max-Age.
			// They do understand Expires, even with V1 cookies!
			if (version == 0 || alwaysAddExpires) {
				// Wdy, DD-Mon-YY HH:MM:SS GMT ( Expires Netscape format )
				buf.append("; Expires=");
				// To expire immediately we need to set the time in past
				if (maxAge == 0) {
					buf.append(CookieUtils.ancientDate);
				}
				else {
					buf.append(CookieUtils.OLD_COOKIE_FORMAT.get().format(new Date(System.currentTimeMillis() + maxAge * 1000L)));
				}

			}
		}

		// Path=path
		if (path != null) {
			buf.append("; Path=");

			BitSet safe = CookieUtils.GetInitialSafeChars();
			safe.set('/');
			safe.set('"');
			path = urlEncode(path, safe, true);

			if (version == 0) {
				maybeQuote2(version, buf, path, versionOneStrictCompliance, rfc6265Support);
			}
			else {
				maybeQuote2(version, buf, path, CookieUtils.tspecials2NoSlash, false, versionOneStrictCompliance, rfc6265Support);
			}
		}

		// Secure
		if (isSecure) {
			buf.append("; Secure");
		}

		// httpOnly
		if (isHttpOnly) {
			buf.append("; HttpOnly");
		}
	}

	// TODO RFC2965 fields also need to be passed
	public static void serializeClientCookies(StringBuilder buf, HttpCookie... cookies) {
		serializeClientCookies(buf, CookieUtils.COOKIE_VERSION_ONE_STRICT_COMPLIANCE, CookieUtils.RFC_6265_SUPPORT_ENABLED, cookies);
	}

	// TODO RFC2965 fields also need to be passed
	public static void serializeClientCookies(StringBuilder buf, boolean versionOneStrictCompliance, boolean rfc6265Support, HttpCookie... cookies) {

		if (cookies.length == 0) {
			return;
		}

		final int version = cookies[0].getVersion();

		if (!rfc6265Support && version == 1) {
			buf.append("$Version=\"1\"; ");
		}

		for (int i = 0; i < cookies.length; i++) {
			final HttpCookie cookie = cookies[i];

			buf.append(cookie.getName());
			buf.append('=');
			// Servlet implementation does not check anything else

			maybeQuote2(version, buf, cookie.getValue(), true, rfc6265Support);

			// If version == 1 - add domain and path
			if (!rfc6265Support && version == 1) {
				// $Domain="domain"
				final String domain = cookie.getDomain();
				if (domain != null) {
					buf.append("; $Domain=");
					maybeQuote2(version, buf, domain, versionOneStrictCompliance, rfc6265Support);
				}

				// $Path="path"
				String path = cookie.getPath();
				if (path != null) {
					buf.append("; $Path=");

					BitSet safe = CookieUtils.GetInitialSafeChars();
					safe.set('/');
					safe.set('"');
					path = urlEncode(path, safe, true);

					maybeQuote2(version, buf, path, CookieUtils.tspecials2NoSlash, false, versionOneStrictCompliance, rfc6265Support);
				}
			}

			if (i < cookies.length - 1) {
				buf.append("; ");
			}
		}
	}

	/**
	 * Quotes values using rules that vary depending on Cookie version.
	 * 
	 * @param version
	 * @param buf
	 * @param value
	 */
	public static int maybeQuote2(int version, StringBuilder buf, String value, boolean versionOneStrictCompliance, boolean rfc6265Enabled) {
		return maybeQuote2(version, buf, value, false, versionOneStrictCompliance, rfc6265Enabled);
	}

	public static int maybeQuote2(int version, StringBuilder buf, String value, boolean allowVersionSwitch, boolean versionOneStrictCompliance, boolean rfc6265Enabled) {
		return maybeQuote2(version, buf, value, null, allowVersionSwitch, versionOneStrictCompliance, rfc6265Enabled);
	}

	public static int maybeQuote2(int version, StringBuilder buf, String value, String literals, boolean allowVersionSwitch, boolean versionOneStrictCompliance, boolean rfc6265Enabled) {
		if (value == null || value.length() == 0) {
			buf.append("\"\"");
		}
		else if (CookieUtils.containsCTL(value, version)) {
			throw new IllegalArgumentException("Control character in cookie value, consider BASE64 encoding your value");
		}
		else if (alreadyQuoted(value)) {
			buf.append('"');
			buf.append(escapeDoubleQuotes(value, 1, value.length() - 1));
			buf.append('"');
		}
		else if (allowVersionSwitch && versionOneStrictCompliance && version == 0 && !CookieUtils.isToken2(value, literals)) {
			buf.append('"');
			buf.append(escapeDoubleQuotes(value, 0, value.length()));
			buf.append('"');
			version = 1;
		}
		else if (version == 0 && !CookieUtils.isToken(value, literals)) {
			buf.append('"');
			buf.append(escapeDoubleQuotes(value, 0, value.length()));
			buf.append('"');
		}
		else if (version == 1 && !CookieUtils.isToken2(value, literals)) {
			buf.append('"');
			buf.append(escapeDoubleQuotes(value, 0, value.length()));
			buf.append('"');
		}
		else if (version < 0 && rfc6265Enabled) {
			buf.append('"');
			buf.append(escapeDoubleQuotes(value, 0, value.length()));
			buf.append('"');
		}
		else {
			buf.append(value);
		}
		return version;
	}

	/**
	 * Escapes any double quotes in the given string.
	 *
	 * @param s
	 *            the input string
	 * @param beginIndex
	 *            start index inclusive
	 * @param endIndex
	 *            exclusive
	 * @return The (possibly) escaped string
	 */
	private static String escapeDoubleQuotes(String s, int beginIndex, int endIndex) {

		if (s == null || s.length() == 0 || s.indexOf('"') == -1) {
			return s;
		}

		StringBuilder b = new StringBuilder();
		for (int i = beginIndex; i < endIndex; i++) {
			char c = s.charAt(i);
			if (c == '\\') {
				b.append(c);
				// ignore the character after an escape, just append it
				if (++i >= endIndex) {
					throw new IllegalArgumentException("Invalid escape character in cookie value.");
				}
				b.append(s.charAt(i));
			}
			else if (c == '"') {
				b.append('\\').append('"');
			}
			else {
				b.append(c);
			}
		}

		return b.toString();
	}

	public static boolean alreadyQuoted(String value) {
		return !(value == null || value.length() == 0) && (value.charAt(0) == '\"' && value.charAt(value.length() - 1) == '\"');
	}

	static void put(StringBuilder dstBuffer, int c) {
		dstBuffer.append((char) c);
	}

	static void putInt(StringBuilder dstBuffer, int intValue) {
		dstBuffer.append(intValue);
	}

	static void put(StringBuilder dstBuffer, String s) {
		dstBuffer.append(s);
	}

	public static String urlEncode(String s, BitSet safeChars, boolean toHexUpperCase) {
		CharsetEncoder enc = null;
		CharBuffer cb = null;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			int c = (int) s.charAt(i);
			if (safeChars.get(c)) {
				sb.append((char) c);
			}
			else {
				if (cb == null) {
					enc = UTF8.newEncoder();
					cb = CharBuffer.allocate(2);
				}
				cb.append((char)c);

				// "surrogate" - UTF is _not_ 16 bit, but 21 !!!! ( while UCS is 31 ). Amazing...
				if (c >= 0xD800 && c <= 0xDBFF) {
					if ((i + 1) < s.length()) {
						int d = (int) s.charAt(i + 1);
						if (d >= 0xDC00 && d <= 0xDFFF) {
							cb.append((char) d);
							i++;
						}
					}
				}
				try {
					byte[] bytes = enc.encode(cb).array();
					urlEncode(sb, bytes, 0, bytes.length, toHexUpperCase);
				}
				catch (CharacterCodingException e) {
					// swallow and continue
					//TODO:  Since we always encode to utf-8, will we ever get a CharacterCodingException exception ?
				}
				cb.clear();
				enc.reset();
			}
		}
		return sb.toString();
	}
	private static final Charset UTF8 = Charset.forName("utf-8");

	/**
     */
	private static void urlEncode(StringBuilder buf, byte bytes[], int off, int len, boolean toHexUpperCase) {
		for (int j = off; j < len; j++) {
			buf.append('%');
			char ch = Character.forDigit((bytes[j] >> 4) & 0xF, 16);
			if (toHexUpperCase) {
				ch = Character.toUpperCase(ch);
			}
			buf.append(ch);
			ch = Character.forDigit(bytes[j] & 0xF, 16);
			if (toHexUpperCase) {
				ch = Character.toUpperCase(ch);
			}
			buf.append(ch);
		}
	}
}
