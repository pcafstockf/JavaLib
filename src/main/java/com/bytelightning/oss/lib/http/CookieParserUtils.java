/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The set of Cookie utility methods for cookie parsing.
 *
 * There is duplication of logic within which we know to be frowned upon, however it is done with performance in mind.
 *
 * @author Grizzly team
 */

public class CookieParserUtils {
	/**
	 * Parses a cookie header after the initial "Cookie:" [WS][$]token[WS]=[WS](token|QV)[;|,] RFC 2965 JVK
	 */
	public static List<HttpCookie> parseClientCookies(String cookiesStr, boolean versionOneStrictCompliance, boolean rfc6265Enabled) {
		if (cookiesStr == null) {
			throw new IllegalArgumentException("cookieStr cannot be null.");
		}
		ArrayList<HttpCookie> retVal = new ArrayList<HttpCookie>();
		if (cookiesStr.length() == 0) {
			return retVal;
		}

		int end = cookiesStr.length();
		int pos = 0;
		int nameStart;
		int nameEnd;
		int valueStart;
		int valueEnd;
		int version = 0;

		HttpCookie cookie = null;

		boolean isSpecial;
		boolean isQuoted;

		while (pos < end) {
			isSpecial = false;
			isQuoted = false;

			// Skip whitespace and non-token characters (separators)
			while (pos < end && (CookieUtils.isSeparator(cookiesStr.charAt(pos)) || CookieUtils.isWhiteSpace(cookiesStr.charAt(pos)))) {
				pos++;
			}

			if (pos >= end) {
				return retVal;
			}

			// Detect Special cookies
			if (cookiesStr.charAt(pos) == '$') {
				isSpecial = true;
				pos++;
			}

			// Get the cookie name. This must be a token
			nameStart = pos;
			pos = nameEnd = CookieUtils.getTokenEndPosition(cookiesStr, pos, end);

			// Skip whitespace
			while (pos < end && CookieUtils.isWhiteSpace(cookiesStr.charAt(pos))) {
				pos++;
			}

			// Check for an '=' -- This could also be a name-only
			// cookie at the end of the cookie header, so if we
			// are past the end of the header, but we have a name
			// skip to the name-only part.
			if (pos < end && cookiesStr.charAt(pos) == '=') {

				// Skip whitespace
				do {
					pos++;
				}
				while (pos < end && CookieUtils.isWhiteSpace(cookiesStr.charAt(pos)));

				if (pos >= end) {
					return retVal;
				}

				// Determine what type of value this is, quoted value,
				// token, name-only with an '=', or other (bad)
				switch (cookiesStr.charAt(pos)) {
				case '"':
					// Quoted Value
					isQuoted = true;
					valueStart = pos + 1; // strip "
					// getQuotedValue returns the position before
					// at the last qoute. This must be dealt with
					// when the bytes are copied into the cookie
					valueEnd = CookieUtils.getQuotedValueEndPosition(cookiesStr, valueStart, end);
					// We need pos to advance
					pos = valueEnd;
					// Handles cases where the quoted value is
					// unterminated and at the end of the header,
					// e.g. [myname="value]
					if (pos >= end) {
						return retVal;
					}
					break;
				case ';':
				case ',':
					// Name-only cookie with an '=' after the name token
					// This may not be RFC compliant
					valueStart = valueEnd = -1;
					// The position is OK (On a delimiter)
					break;
				default:
					if (!CookieUtils.isSeparator(cookiesStr.charAt(pos), versionOneStrictCompliance)) {
						// Token
						// Token
						valueStart = pos;
						// getToken returns the position at the delimeter
						// or other non-token character
						valueEnd = CookieUtils.getTokenEndPosition(cookiesStr, valueStart, end, versionOneStrictCompliance);
						// We need pos to advance
						pos = valueEnd;
					}
					else {
						// INVALID COOKIE, advance to next delimiter
						// The starting character of the cookie value was
						// not valid.
						while (pos < end && cookiesStr.charAt(pos) != ';' && cookiesStr.charAt(pos) != ',') {
							pos++;
						}

						pos++;
						// Make sure no special avpairs can be attributed to
						// the previous cookie by setting the current cookie
						// to null
						cookie = null;
						continue;
					}
				}
			}
			else {
				// Name only cookie
				valueStart = valueEnd = -1;
				pos = nameEnd;

			}

			// We should have an avpair or name-only cookie at this
			// point. Perform some basic checks to make sure we are
			// in a good state.

			// Skip whitespace
			while (pos < end && CookieUtils.isWhiteSpace(cookiesStr.charAt(pos))) {
				pos++;
			}

			// Make sure that after the cookie we have a separator. This
			// is only important if this is not the last cookie pair
			while (pos < end && cookiesStr.charAt(pos) != ';' && cookiesStr.charAt(pos) != ',') {
				pos++;
			}

			pos++;

			// All checks passed. Add the cookie, start with the
			// special avpairs first
			if (isSpecial) {
				isSpecial = false;
				// $Version must be the first avpair in the cookie header
				// (sc must be null)
				if (CookieUtils.equals("Version", cookiesStr, nameStart, nameEnd) && cookie == null) {
					if (rfc6265Enabled) {
						continue;
					}
					// Set version
					if (cookiesStr.charAt(valueStart) == '1' && valueEnd == (valueStart + 1)) {
						version = 1;
					}
					else {
						// unknown version (Versioning is not very strict)
					}
					continue;
				}

				// We need an active cookie for Path/Port/etc.
				if (cookie == null) {
					continue;
				}

				// Domain is more common, so it goes first
				if (CookieUtils.equals("Domain", cookiesStr, nameStart, nameEnd)) {
					cookie.setDomain(cookiesStr.substring(valueStart, valueEnd));
					continue;
				}

				if (CookieUtils.equals("Path", cookiesStr, nameStart, nameEnd)) {
					cookie.setPath(cookiesStr.substring(valueStart, valueEnd));
					continue;
				}

			}
			else { // Normal Cookie

				String name = cookiesStr.substring(nameStart, nameEnd);
				String value;

				if (valueStart != -1) { // Normal AVPair
					if (isQuoted) {
						// We know this is a byte value so this is safe
						value = unescapeDoubleQuotes(cookiesStr, valueStart, valueEnd - valueStart);
					}
					else {
						value = cookiesStr.substring(valueStart, valueEnd);
					}
				}
				else {
					// Name Only
					value = "";
				}

				cookie = new HttpCookie(name, value);
				if (!rfc6265Enabled) {
					cookie.setVersion(version);
				}
				retVal.add(cookie);
			}
		}
		return retVal;
	}

	public static List<HttpCookie> parseServerCookies(String cookiesStr, boolean versionOneStrictCompliance, boolean rfc6265Enabled) {

		if (cookiesStr == null) {
			throw new IllegalArgumentException();
		}
		ArrayList<HttpCookie> retVal = new ArrayList<HttpCookie>();
		if (cookiesStr.length() == 0) {
			return retVal;
		}

		int end = cookiesStr.length();
		int pos = 0;
		int nameStart;
		int nameEnd;
		int valueStart;
		int valueEnd;

		HttpCookie cookie = null;

		boolean isQuoted;

		while (pos < end) {
			isQuoted = false;

			// Skip whitespace and non-token characters (separators)
			while (pos < end && (CookieUtils.isSeparator(cookiesStr.charAt(pos)) || CookieUtils.isWhiteSpace(cookiesStr.charAt(pos)))) {
				pos++;
			}

			if (pos >= end) {
				return retVal;
			}

			// Get the cookie name. This must be a token
			nameStart = pos;
			pos = nameEnd = CookieUtils.getTokenEndPosition(cookiesStr, pos, end);

			// Skip whitespace
			while (pos < end && CookieUtils.isWhiteSpace(cookiesStr.charAt(pos))) {
				pos++;
			}

			// Check for an '=' -- This could also be a name-only
			// cookie at the end of the cookie header, so if we
			// are past the end of the header, but we have a name
			// skip to the name-only part.
			if (pos < end && cookiesStr.charAt(pos) == '=') {

				// Skip whitespace
				do {
					pos++;
				}
				while (pos < end && CookieUtils.isWhiteSpace(cookiesStr.charAt(pos)));

				if (pos >= end) {
					return retVal;
				}

				// Determine what type of value this is, quoted value,
				// token, name-only with an '=', or other (bad)
				switch (cookiesStr.charAt(pos)) {
				case '"':
					// Quoted Value
					isQuoted = true;
					valueStart = pos + 1; // strip "
					// getQuotedValue returns the position before
					// at the last qoute. This must be dealt with
					// when the bytes are copied into the cookie
					valueEnd = CookieUtils.getQuotedValueEndPosition(cookiesStr, valueStart, end);
					// We need pos to advance
					pos = valueEnd;
					// Handles cases where the quoted value is
					// unterminated and at the end of the header,
					// e.g. [myname="value]
					if (pos >= end) {
						return retVal;
					}
					break;
				case ';':
				case ',':
					// Name-only cookie with an '=' after the name token
					// This may not be RFC compliant
					valueStart = valueEnd = -1;
					// The position is OK (On a delimiter)
					break;
				default:
					if (!CookieUtils.isSeparator(cookiesStr.charAt(pos), versionOneStrictCompliance)) {
						// Token
						// Token
						valueStart = pos;
						// getToken returns the position at the delimeter
						// or other non-token character
						valueEnd = CookieUtils.getTokenEndPosition(cookiesStr, valueStart, end, versionOneStrictCompliance);
						// We need pos to advance
						pos = valueEnd;
					}
					else {
						// INVALID COOKIE, advance to next delimiter
						// The starting character of the cookie value was
						// not valid.
						while (pos < end && cookiesStr.charAt(pos) != ';' && cookiesStr.charAt(pos) != ',') {
							pos++;
						}

						pos++;
						// Make sure no special avpairs can be attributed to
						// the previous cookie by setting the current cookie
						// to null
						cookie = null;
						continue;
					}
				}
			}
			else {
				// Name only cookie
				valueStart = valueEnd = -1;
				pos = nameEnd;

			}

			// We should have an avpair or name-only cookie at this
			// point. Perform some basic checks to make sure we are
			// in a good state.

			// Skip whitespace
			while (pos < end && CookieUtils.isWhiteSpace(cookiesStr.charAt(pos))) {
				pos++;
			}

			// Make sure that after the cookie we have a separator. This
			// is only important if this is not the last cookie pair
			while (pos < end && cookiesStr.charAt(pos) != ';' && cookiesStr.charAt(pos) != ',') {
				pos++;
			}

			pos++;

			// All checks passed. Add the cookie, start with the
			// special avpairs first
			boolean versionExplicitlySet = false;
			if (cookie != null) {
				// Domain is more common, so it goes first
				if (cookie.getDomain() == null && CookieUtils.equalsIgnoreCase("Domain", cookiesStr, nameStart, nameEnd)) {
					cookie.setDomain(cookiesStr.substring(valueStart, valueEnd));
					continue;
				}

				// Path
				if (cookie.getPath() == null && CookieUtils.equalsIgnoreCase("Path", cookiesStr, nameStart, nameEnd)) {
					cookie.setPath(cookiesStr.substring(valueStart, valueEnd));
					continue;
				}

				// Version
				if (CookieUtils.equals("Version", cookiesStr, nameStart, nameEnd)) {
					if (rfc6265Enabled) {
						continue;
					}
					// Set version
					if (cookiesStr.charAt(valueStart) == '1' && valueEnd == (valueStart + 1)) {
						cookie.setVersion(1);
						versionExplicitlySet = true;
					}
					else {
						if (!rfc6265Enabled) {
							cookie.setVersion(0);
							versionExplicitlySet = true;
						}
					}
					continue;
				}

				// Comment
				if (cookie.getComment() == null && CookieUtils.equals("Comment", cookiesStr, nameStart, nameEnd)) {
					cookie.setComment(cookiesStr.substring(valueStart, valueEnd));
					continue;
				}

				// Max-Age
				if (cookie.getMaxAge() == -1 && CookieUtils.equals("Max-Age", cookiesStr, nameStart, nameEnd)) {
					cookie.setMaxAge(Integer.parseInt(cookiesStr.substring(valueStart, valueEnd)));
					continue;
				}
				// Expires
				if ((cookie.getVersion() == 0 || versionExplicitlySet) && cookie.getMaxAge() == -1 && CookieUtils.equalsIgnoreCase("Expires", cookiesStr, nameStart, nameEnd)) {
					try {
						valueEnd = CookieUtils.getTokenEndPosition(cookiesStr, valueEnd + 1, end, false);
						pos = valueEnd + 1;
						final String expiresDate = cookiesStr.substring(valueStart, valueEnd);
						final Date date = CookieUtils.OLD_COOKIE_FORMAT.get().parse(expiresDate);
						cookie.setMaxAge(getMaxAgeDelta(date.getTime(), System.currentTimeMillis()) / 1000);
					}
					catch (ParseException ignore) {
					}

					continue;
				}

				// Secure
				if (!cookie.getSecure() && CookieUtils.equalsIgnoreCase("Secure", cookiesStr, nameStart, nameEnd)) {
					cookie.setSecure(true);
					continue;
				}

				// HttpOnly (uncomment when we move to jdk 1.7
//				if (!cookie.isHttpOnly() && CookieUtils.equals("HttpOnly", cookiesStr, nameStart, nameEnd)) {
//					cookie.setHttpOnly(true);
//					continue;
//				}

				if (CookieUtils.equals("Discard", cookiesStr, nameStart, nameEnd)) {
					continue;
				}
			}

			// Normal Cookie
			String name = cookiesStr.substring(nameStart, nameEnd);
			String value;

			if (valueStart != -1) { // Normal AVPair
				if (isQuoted) {
					// We know this is a byte value so this is safe
					value = unescapeDoubleQuotes(cookiesStr, valueStart, valueEnd - valueStart);
				}
				else {
					value = cookiesStr.substring(valueStart, valueEnd);
				}
			}
			else {
				// Name Only
				value = "";
			}

			cookie = new HttpCookie(name, value);
			if (!rfc6265Enabled && !versionExplicitlySet) {
				cookie.setVersion(0);
			}
			retVal.add(cookie);
		}
		return retVal;
	}

	/**
	 * Unescapes any double quotes in the given cookie value.
	 *
	 * @param s
	 *            The cookie value to modify
	 * @return new String
	 */
	public static String unescapeDoubleQuotes(String s, int start, int length) {

		if (s == null || s.length() == 0) {
			return s;
		}

		final StringBuilder sb = new StringBuilder(s.length());

		int src = start;
		int end = src + length;

		while (src < end) {
			if (s.charAt(src) == '\\' && src < end && s.charAt(src + 1) == '"') {
				src++;
			}

			sb.append(s.charAt(src));
			src++;
		}

		return sb.toString();
	}

	private static int getMaxAgeDelta(long date1, long date2) {
		long result = date1 - date2;
		if (result > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		else {
			return (int) result;
		}
	}

}
