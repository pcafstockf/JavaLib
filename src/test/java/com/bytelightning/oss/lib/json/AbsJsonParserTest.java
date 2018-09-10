package com.bytelightning.oss.lib.json;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/*
https://github.com/Microsoft/ChakraCore/blob/master/test/JSON/syntaxError.js
https://github.com/tc39/test262/blob/master/test/built-ins/JSON/parse/15.12.1.1-0-1.js
https://github.com/bestiejs/json3/tree/master/test
http://bestiejs.github.io/json3/test/test_browser.html
*/
public abstract class AbsJsonParserTest {

	public AbsJsonParserTest() {
		super();
	}
	
	/**
	 * Ensures that `JSON.parse` throws an exception when parsing the given `source` string.
	 * 
	 * @param source
	 * @param message
	 */
	abstract void parseError(String source, final String message);
	
	/**
	 * Ensures that `JSON.parse` parses the given source string correctly.
	 * 
	 * @param expected
	 * @param source
	 * @param message
	 */
	abstract void parses(Object expected, String source, final String message);
	
	@Test
	public void testParseEmptySource() {
		parseError("", "Empty JSON source string");
		parseError("\n\n\r\n", "Source string containing only line terminators");
		parseError(" ", "Source string containing a single space character");
		parseError(" ", "Source string containing multiple space characters");
	}

	@Test
	public void testParseWhitespace() {
		// The only valid JSON whitespace characters are tabs, spaces, and line terminators.
		// All other Unicode category `Z` (`Zs`, `Zl`, and `Zp`) characters are invalid (note that the `Zs` category includes the space character).
		String[] characters = { 
			"{\u00a0}", "{\u1680}", "{\u180e}", "{\u2000}", "{\u2001}", "{\u2002}", "{\u2003}", "{\u2004}", "{\u2005}", "{\u2006}", "{\u2007}", "{\u2008}", "{\u2009}", "{\u200a}", "{\u202f}", "{\u205f}", "{\u3000}", "{\u2028}", "{\u2029}" 
		};
		for (int i = 0; i < characters.length; i++) {
			this.parseError(characters[i], "Source string containing an invalid Unicode whitespace character (" + i + ")");
		}
	
		this.parseError("{\u000b}", "Source string containing a vertical tab");
		this.parseError("{\u000c}", "Source string containing a form feed");
		this.parseError("{\ufeff}", "Source string containing a byte-order mark");
	
		this.parses(new LinkedHashMap<String, Object>(), "{\r\n}", "Source string containing a CRLF line ending");
		this.parses(new LinkedHashMap<String, Object>(), "{\n\n\r\n}", "Source string containing multiple line terminators");
		this.parses(new LinkedHashMap<String, Object>(), "{\t}", "Source string containing a tab character");
		this.parses(new LinkedHashMap<String, Object>(), "{ }", "Source string containing a space character");
	}


	@Test
	public void testParseOctalValues() {
		// `08` and `018` are invalid octal values.
		String[] octals = { "00", "01", "02", "03", "04", "05", "06", "07", "010", "011", "08", "018" };
		for (int i = 0; i < octals.length; i++) {
			parseError(octals[i], "Octal literal");
			parseError("-" + octals[i], "Negative octal literal");
			parseError("\"\\" + octals[i] + "\"", "Octal escape sequence in a string");
			parseError("\"\\x" + octals[i] + "\"", "Hex escape sequence in a string");
		}
	}

	@Test
	public void testParseNumericLiterals() {
		this.parses(100l, "100", "Integer");
		this.parses(-100l, "-100", "Negative integer");
		this.parses(10.5d, "10.5", "Float");
		this.parses(-3.141d, "-3.141", "Negative float");
		this.parses(0.625d, "0.625", "Decimal");
		this.parses(-0.03125d, "-0.03125", "Negative decimal");
		this.parses(1000d, "1e3", "Exponential");
		this.parses(100d, "1e+2", "Positive exponential");
		this.parses(-0.01d, "-1e-2", "Negative exponential");
		this.parses(3125d, "0.03125e+5", "Decimalized exponential");
		this.parses(100d, "1E2", "Case-insensitive exponential delimiter");

		this.parseError("+1", "Leading `+`");
		this.parseError("1.", "Trailing decimal point");
		this.parseError(".1", "Leading decimal point");
		this.parseError("1e", "Missing exponent");
		this.parseError("1e-", "Missing signed exponent");
		this.parseError("--1", "Leading `--`");
		this.parseError("1-+", "Trailing `-+`");
		this.parseError("0xaf", "Hex literal");

		// The native `JSON.parse` implementation in IE 9 allows this syntax, but the feature tests should detect the broken implementation.
		this.parseError("- 5", "Invalid negative sign");
	}

	@Test
	public void testParseStringLiterals() {
		String[] controlCharacters = { 
			"\u0001", "\u0002", "\u0003", "\u0004", "\u0005", "\u0006", "\u0007", "\b", "\t", "\n", "\u000b", "\f", "\r", "\u000e", "\u000f", "\u0010", "\u0011", 
			"\u0012", "\u0013", "\u0014", "\u0015", "\u0016", "\u0017", "\u0018", "\u0019", "\u001a", "\u001b", "\u001c", "\u001d", "\u001e", "\u001f" };

		this.parses("value", "\"value\"", "Double-quoted string literal");
		this.parses("", "\"\"", "Empty string literal");

		this.parses("\u2028", "\"\\u2028\"", "String containing an escaped Unicode line separator");
		this.parses("\u2029", "\"\\u2029\"", "String containing an escaped Unicode paragraph separator");
		// ExtendScript doesn't handle surrogate pairs correctly; attempting to parse `"\ud834\udf06"` will throw an uncatchable error (issue #29).
		this.parses("\ud834\udf06", "\"\ud834\udf06\"", "String containing an unescaped Unicode surrogate pair");
		this.parses("\u0001", "\"\\u0001\"", "String containing an escaped ASCII control character");
		this.parses("\b", "\"\\b\"", "String containing an escaped backspace");
		this.parses("\f", "\"\\f\"", "String containing an escaped form feed");
		this.parses("\n", "\"\\n\"", "String containing an escaped line feed");
		this.parses("\r", "\"\\r\"", "String containing an escaped carriage return");
		this.parses("\t", "\"\\t\"", "String containing an escaped tab");

		this.parses("hello/world", "\"hello\\/world\"", "String containing an escaped solidus");
		this.parses("hello\\world", "\"hello\\\\world\"", "String containing an escaped reverse solidus");
		this.parses("hello\"world", "\"hello\\\"world\"", "String containing an escaped double-quote character");

		this.parseError("'hello'", "Single-quoted string literal");
		this.parseError("\"\\x61\"", "String containing a hex escape sequence");
		this.parseError("\"hello \r\n world\"", "String containing an unescaped CRLF line ending");

		for (int i = 0; i < controlCharacters.length; i++) {
			this.parseError('"' + controlCharacters[i] + '"', "String containing an unescaped ASCII control character");
		}
	}

	@Test
	public void testParseArrayLiterals() {
		this.parseError("[1, 2, 3,]", "Trailing comma in array literal");
		Object[] a = { 4l, 5l };
		Object[] b = { -3l, Arrays.asList(a) };
		Object[] c = { true, false };
		Object[] d = { null };
		Object[] e = {};
		Object[] f = { Arrays.asList(e) };
		Object[] nested = { 1l, 2l, Arrays.asList(b), 6l, Arrays.asList(c), Arrays.asList(d), Arrays.asList(f) };
		this.parses(Arrays.asList(nested), "[1, 2, [-3, [4, 5]], 6, [true, false], [null], [[]]]", "Nested arrays");

		Object[] aa = { new LinkedHashMap<String, Object>() };
		this.parses(Arrays.asList(aa), "[{}]", "Array containing empty object literal");

		Object[] z = { 0.01d };
		Object[] y = { "world" };
		Object[] x = { "hello" };
		Map<String, Object> w = new LinkedHashMap<String, Object>();
		w.put("a", Arrays.asList(x));
		w.put("b", Arrays.asList(y));
		Object[] v = { 100d, true, false, null, w, Arrays.asList(z) };
		this.parses(Arrays.asList(v), "[1e2, true, false, null, {\"a\": [\"hello\"], \"b\": [\"world\"]}, [1e-2]]", "Mixed array");
	}

	@Test
	public void testParseObjectLiterals() {
		Map<String, Object> a = new LinkedHashMap<String, Object>();
		a.put("hello", "world");
		this.parses(a, "{\"hello\": \"world\"}", "Object literal containing one member");

		Map<String, Object> b = new LinkedHashMap<String, Object>();
		b.put("hello", "world");
		Object[] c = { "bar", true };
		b.put("foo", Arrays.asList(c));
		Map<String, Object> d = new LinkedHashMap<String, Object>();
		b.put("fox", d);
		d.put("quick", true);
		d.put("purple", false);

		this.parses(b, "{\"hello\": \"world\", \"foo\": [\"bar\", true], \"fox\": {\"quick\": true, \"purple\": false}}", "Object literal containing multiple members");

		this.parseError("{key: 1}", "Unquoted identifier used as a property name");
		this.parseError("{false: 1}", "`false` used as a property name");
		this.parseError("{true: 1}", "`true` used as a property name");
		this.parseError("{null: 1}", "`null` used as a property name");
		this.parseError("{'key': 1}", "Single-quoted string used as a property name");
		this.parseError("{1: 2, 3: 4}", "Number used as a property name");

		this.parseError("{\"hello\": \"world\", \"foo\": \"bar\",}", "Trailing comma in object literal");
	}

	@Test
	public void testParseInvalidExpressions() {
		String[] expresssions = { "1 + 1", "1 * 2", "var value = 123;", "{});value = 123;({}", "call()", "1, 2, 3, \"value\"" };
		for (int i = 0; i < expresssions.length; i++) {
			this.parseError(expresssions[i], "Source string containing a JavaScript expression");
		}
	}

	@Test
	public void testParseECMAScript5Conformance() {
		// Tests 15.12.1.1-0-1 thru 15.12.1.1-0-8.
		this.parseError("12\t\r\n 34", "Valid whitespace characters may not separate two discrete tokens");
		this.parseError("\u000b1234", "The vertical tab is not a valid whitespace character");
		this.parseError("\u000c1234", "The form feed is not a valid whitespace character");
		this.parseError("\u00a01234", "The non-breaking space is not a valid whitespace character");
		this.parseError("\u200b1234", "The zero-width space is not a valid whitespace character");
		this.parseError("\ufeff1234", "The byte order mark (zero-width non-breaking space) is not a valid whitespace character");
		this.parseError("\u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u202f\u205f\u30001234", "Other Unicode category `Z` characters are not valid whitespace characters");
		this.parseError("\u2028\u20291234", "The line (U+2028) and paragraph (U+2029) separators are not valid whitespace characters");

		// Test 15.12.1.1-0-9.
		Map<String, Object> a = new LinkedHashMap<String, Object>();
		a.put("property", new LinkedHashMap<String, Object>());
		Object[] b = { true, null, 123.456 };
		a.put("prop2", Arrays.asList(b));
		this.parses(a, "\t\r \n{\t\r \n" + "\"property\"\t\r \n:\t\r \n{\t\r \n}\t\r \n,\t\r \n" + "\"prop2\"\t\r \n:\t\r \n" + "[\t\r \ntrue\t\r \n,\t\r \nnull\t\r \n,123.456\t\r \n]" + "\t\r \n}\t\r \n", "Valid whitespace characters may precede and follow all tokens");

		// Tests 15.12.1.1-g1-1 thru 15.12.1.1-g1-4.
		this.parses(1234l, "\t1234", "Leading tab characters should be ignored");
		this.parseError("12\t34", "A tab character may not separate two disparate tokens");
		this.parses(1234l, "\r1234", "Leading carriage returns should be ignored");
		this.parseError("12\r34", "A carriage return may not separate two disparate tokens");
		this.parses(1234l, "\n1234", "Leading line feeds should be ignored");
		this.parseError("12\n34", "A line feed may not separate two disparate tokens");
		this.parses(1234l, " 1234", "Leading space characters should be ignored");
		this.parseError("12 34", "A space character may not separate two disparate tokens");

		// Tests 15.12.1.1-g2-1 thru 15.12.1.1-g2-5.
		this.parses("abc", "\"abc\"", "Strings must be enclosed in double quotes");
		this.parseError("'abc'", "Single-quoted strings are not permitted");
		// Note: the original test 15.12.1.1-g2-3 (`"\u0022abc\u0022"`) is incorrect, as the JavaScript interpreter will always convert `\u0022` to `"`.
		this.parseError("\\u0022abc\\u0022", "Unicode-escaped double quote delimiters are not permitted");
		this.parseError("\"ab" + "c'", "Strings must terminate with a double quote character");
		this.parses("", "\"\"", "Strings may be empty");

		// Tests 15.12.1.1-g4-1 thru 15.12.1.1-g4-4.
		this.parseError("\"\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\"", "Unescaped control characters in the range [U+0000, U+0007] are not permitted within strings");
		this.parseError("\"\u0008\u0009\n\u000b\u000c\r\u000e\u000f\"", "Unescaped control characters in the range [U+0008, U+000F] are not permitted within strings");
		this.parseError("\"\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\"", "Unescaped control characters in the range [U+0010, U+0017] are not permitted within strings");
		this.parseError("\"\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f\"", "Unescaped control characters in the range [U+0018, U+001F] are not permitted within strings");

		// Tests 15.12.1.1-g5-1 thru 15.12.1.1-g5-3.
		this.parses("X", "\"\\u0058\"", "Unicode escape sequences are permitted within strings");
		this.parseError("\"\\u005\"", "Unicode escape sequences may not comprise fewer than four hexdigits");
		this.parseError("\"\\u0X50\"", "Unicode escape sequences may not contain non-hex characters");

		// Tests 15.12.1.1-g6-1 thru 15.12.1.1-g6-7.
		this.parses("/", "\"\\/\"", "Escaped solidus");
		this.parses("\\", "\"\\\\\"", "Escaped reverse solidus");
		this.parses("\b", "\"\\b\"", "Escaped backspace");
		this.parses("\f", "\"\\f\"", "Escaped form feed");
		this.parses("\n", "\"\\n\"", "Escaped line feed");
		this.parses("\r", "\"\\r\"", "Escaped carriage return");
		this.parses("\t", "\"\\t\"", "Escaped tab");
	}

	@Test
	public void testParseMisc() {
		String val = "{\"v1\":{\"tools\":[{\"version\":1,\"name\":\"Math Help\",\"href\":\"https://www/social/math\",\"description\":\"Math Help can help\",\"isHidden\":null,\"sortOrder\":0}]}}";
		Map<String, Object> a = new LinkedHashMap<String, Object>();
		a.put("value", val);
		String in = "{\"value\": \"{\\\"v1\\\":{\\\"tools\\\":[{\\\"version\\\":1,\\\"name\\\":\\\"Math Help\\\",\\\"href\\\":\\\"https://www/social/math\\\",\\\"description\\\":\\\"Math Help can help\\\",\\\"isHidden\\\":null,\\\"sortOrder\\\":0}]}}\"}";
		this.parses(a, in, "Embedded JSON");
	}
}
