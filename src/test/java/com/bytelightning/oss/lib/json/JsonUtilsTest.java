package com.bytelightning.oss.lib.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bytelightning.oss.lib.json.DefaultJsonSaxListener;
import com.bytelightning.oss.lib.json.JSON;
import com.bytelightning.oss.lib.json.JsonSaxListener;

public class JsonUtilsTest extends AbsJsonParserTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Override
	void parseError(String source, String message) {
		parseError(source, message, new DefaultJsonSaxListener());
	}

	void parseError(String source, String message, JsonSaxListener l) {
		try {
			JSON.parse(source, l);
			Assert.fail(message);
		}
		catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		catch (Error e) {
			Assert.assertTrue(message, true);
		}
		catch (RuntimeException e) {
			Assert.assertTrue(message, true);
		}
	}

	@Override
	void parses(Object expected, String source, String message) {
		parses(expected, source, message, new DefaultJsonSaxListener());
	}

	void parses(Object expected, String source, final String message, JsonSaxListener l) {
		try {
			Object result = JSON.parse(source, l);
			if (expected != null)
				Assert.assertEquals(message, expected, result);
			else
				Assert.assertTrue(message, true);
		}
		catch (Throwable e) {
			throw new AssertionError(message);
		}
	}

	void serializes(String expected, Object value, String message) {
		serializes(expected, value, message, null, null);
	}

	void serializes(String expected, Object value, String message, Object replacer, Object space) {
		try {
			String result = JSON.stringify(value, replacer, space);
			if (expected != null)
				Assert.assertEquals(message, expected, result);
			else
				Assert.assertTrue(message, true);
		}
		catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Ensures that `JSON.stringify` throws a `TypeError` if the given object contains a circular reference.
	 * 
	 * @param value
	 * @param message
	 */
	void cyclicError(Object value, String message) {
		try {
			JSON.stringify(value);
			Assert.fail(message);
		}
		catch (RuntimeException e) {
			if (e.getMessage().equals("Cyclic TypeError"))
				Assert.assertTrue(message, true);
			else
				throw e;
		}
	}

	@Test
	public void testOptionalArguments() {
		Map<String, Object> a = new LinkedHashMap<String, Object>();
		a.put("a", 1l);
		a.put("b", 16);
		this.parses(a, "{\"a\": 1, \"b\": \"10000\"}", "Callback function provided", new DefaultJsonSaxListener() {
			@Override
			public void stringValue(String value) {
				this.numberValue(Integer.parseInt(value, 2));
			}
		});

		a = new LinkedHashMap<String, Object>();
		a.put("foo", 123);
		a.put("bar", 456);
		Object[] replacer = { "bar" };
		this.serializes("{\n  \"bar\": 456\n}", a, "Object; optional `filter` and `whitespace` arguments", replacer, 2);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testStringify() {
		// Special values.
		this.serializes("null", null, "`null` is represented literally");
		this.serializes("null", Double.POSITIVE_INFINITY, "`Infinity` is serialized as `null`");
		this.serializes("null", Double.NaN, "`NaN` is serialized as `null`");
		this.serializes("null", Double.POSITIVE_INFINITY, "`-Infinity` is serialized as `null`");
		this.serializes("true", true, "Boolean primitives are represented literally");
		this.serializes("false", new Boolean(false), "Boolean objects are represented literally");
		this.serializes("\"\\\\\\\"How\\bquickly\\tdaft\\njumping\\fzebras\\rvex\\\"\"", "\\\"How\bquickly\tdaft\njumping\fzebras\rvex\"", "All control characters in strings are escaped");

		Object[] a = { false, 1d, "Kit" };
		this.serializes("[false,1,\"Kit\"]", a, "Arrays are serialized recursively");
		Object[] b = { null };
		this.serializes("[null]", b, "`[undefined]` is serialized as `[null]`");

		// Property enumeration is implementation-dependent.
		Map<String, Object> z = new LinkedHashMap<String, Object>();
		Object[] y = { "John-David", 29l };
		Object[] x = { "Kit", 18l };
		Object[] w = { "Mathias", 23l };
		z.put("jdalton", Arrays.asList(y));
		z.put("kitcambridge", Arrays.asList(x));
		z.put("mathias", Arrays.asList(w));
		this.parses(z, JSON.stringify(z), "Objects are serialized recursively");

		List<Object> m = new ArrayList<Object>();
		Map<String, Object> n = new LinkedHashMap<String, Object>();
		m.add(n);
		m.add(n);
		this.serializes("[{},{}]", m, "Objects containing duplicate references should not throw a `TypeError`");

		// Complex cyclic structures.
		z = new LinkedHashMap<String, Object>();
		Map<String, Object> v = new LinkedHashMap<String, Object>();
		Map<String, Object> u = new LinkedHashMap<String, Object>();
		Map<String, Object> t = new LinkedHashMap<String, Object>();
		Map<String, Object> s = new LinkedHashMap<String, Object>();
		s.put("foo", null);
		t.put("c", s);
		u.put("foo", t);
		v.put("b", u);
		z.put("foo", v);
		this.serializes("{\"foo\":{\"b\":{\"foo\":{\"c\":{\"foo\":null}}}}}", z, "Nested objects containing identically-named properties should serialize correctly");

		s.put("foo", z);
		this.cyclicError(z, "Objects containing complex circular references should throw a `TypeError`");

		// Dates.
		this.serializes("\"1994-07-03T00:00:00.000Z\"", new Date(Date.UTC(1994 - 1900, 6, 3, 0, 0, 0)), "Dates should be serialized according to the simplified date time string format");
		this.serializes("\"1993-06-02T02:10:28.224Z\"", new Date(Date.UTC(1993 - 1900, 5, 2, 2, 10, 28) + 224), "The date time string should conform to the format outlined in the spec");
		// this.serializes("\"-271821-04-20T00:00:00.000Z\"", new Date(new Double(-8.64e15).longValue()), "The minimum valid date value should serialize correctly");
		// this.serializes("\"+275760-09-13T00:00:00.000Z\"", new Date(new Double(8.64e15).longValue()), "The maximum valid date value should serialize correctly");
		// this.serializes("\"+010000-01-01T00:00:00.000Z\"", new Date(Date.UTC(10000 - 1900, 0, 1, 0, 0, 0)), "https://bugs.ecmascript.org/show_bug.cgi?id=119");

		// Tests based on research by @Yaffle. See kriskowal/es5-shim#111.
		this.serializes("\"1969-12-31T23:59:59.999Z\"", new Date(-1), "Millisecond values < 1000 should be serialized correctly");
		// this.serializes("\"-000001-01-01T00:00:00.000Z\"", new Date(new Double(-621987552e5).longValue()), "Years prior to 0 should be serialized as extended years");
		// this.serializes("\"+010000-01-01T00:00:00.000Z\"", new Date(new Double(2534023008e5).longValue()), "Years after 9999 should be serialized as extended years");
		// this.serializes("\"-109252-01-01T10:37:06.708Z\"", new Date(-3509827334573292L), "Issue #4: Opera > 9.64 should correctly serialize a date with a year of `-109252`");

		// Additional arguments.
		Object[] c = { 4, 5 };
		Object[] d = { 1, 2, 3, c };
		this.serializes("[\n  1,\n  2,\n  3,\n  [\n    4,\n    5\n  ]\n]", d, "Nested arrays; optional `whitespace` argument", null, "  ");
		Object[] e = {};
		this.serializes("[]", e, "Empty array; optional string `whitespace` argument", null, "  ");
		this.serializes("{}", new LinkedHashMap<String, Object>(), "Empty object; optional numeric `whitespace` argument", null, 2);
		Object[] f = { 1 };
		this.serializes("[\n  1\n]", f, "Single-element array; optional numeric `whitespace` argument", null, 2);
		Map<String, Object> g = new LinkedHashMap<String, Object>();
		g.put("foo", 123);
		this.serializes("{\n  \"foo\": 123\n}", g, "Single-member object; optional string `whitespace` argument", null, "  ");
		Map<String, Object> h = new LinkedHashMap<String, Object>();
		Object[] i = { 123 };
		h.put("bar", i);
		g = new LinkedHashMap<String, Object>();
		g.put("foo", h);
		this.serializes("{\n  \"foo\": {\n    \"bar\": [\n      123\n    ]\n  }\n}", g, "Nested objects; optional numeric `whitespace` argument", null, 2);
	}

	@Test
	public void testSerializeECMAScript5Conformance() {
		// Test 15.12.3-11-1 thru 5.12.3-11-15.
		this.serializes("\"a string\"", "a string", "`JSON.stringify` should serialize top-level string primitives");
		this.serializes("123", 123, "`JSON.stringify` should serialize top-level number primitives");
		this.serializes("true", true, "`JSON.stringify` should serialize top-level Boolean primitives");
		this.serializes("null", null, "`JSON.stringify` should serialize top-level `null` values");
		this.serializes("42", new Long(42), "`JSON.stringify` should serialize top-level number objects");
		this.serializes("\"wrapped\"", new String("wrapped"), "`JSON.stringify` should serialize top-level string objects");
		this.serializes("false", new Boolean(false), "`JSON.stringify` should serialize top-level Boolean objects");
		this.serializes("[4,2]", 42, "The `JSON.stringify` callback function may return an array when called on a top-level number primitive", new JSON.Replacer() {
			public Object replace(Object target, String key, Object value) {
				if (value instanceof Number)
					if (((Number) value).intValue() == 42) {
						Object[] retVal = { 4, 2 };
						return retVal;
					}
				return value;
			}
		}, null);
		this.serializes("{\"forty\":2}", 42, "The `JSON.stringify` callback function may return an object literal when called on a top-level number primitive", new JSON.Replacer() {
			public Object replace(Object target, String key, Object value) {
				if (value instanceof Number)
					if (((Number) value).intValue() == 42) {
						Map<String, Object> retVal = new LinkedHashMap<String, Object>();
						retVal.put("forty", 2l);
						return retVal;
					}
				return value;
			}
		}, null);

		// Test 15.12.3-4-1.
		Object[] a = { 42l };
		this.serializes("[42]", a, "`JSON.stringify` should ignore `filter` arguments that are not functions or arrays", new String("fail me"), null);

		// Test 15.12.3-5-a-i-1 and 15.12.3-5-b-i-1.
		Map<String, Object> value = new LinkedHashMap<String, Object>();
		Object[] z = { 1l, 2l, 3l, 4l };
		Map<String, Object> y = new LinkedHashMap<String, Object>();
		y.put("c1", 1l);
		y.put("c2", 2l);
		Map<String, Object> x = new LinkedHashMap<String, Object>();
		x.put("b1", z);
		x.put("b2", y);
		value.put("a1", x);
		value.put("a2", "a2");
		// var value = { "a1": { "b1": [1, 2, 3, 4], "b2": { "c1": 1, "c2": 2 } }, "a2": "a2" };
		Assert.assertEquals("Optional `width` argument: Number object and primitive width values should produce identical results", JSON.stringify(value, null, new Long(5)), JSON.stringify(value, null, 5));
		Assert.assertEquals("Optional `width` argument: String object and primitive width values should produce identical results", JSON.stringify(value, null, new String("xxx")), JSON.stringify(value, null, "xxx"));

		// Test 15.12.3-6-a-1 and 15.12.3-6-a-2.
		Assert.assertEquals("Optional `width` argument: The maximum numeric width value should be 10", JSON.stringify(value, null, 10), JSON.stringify(value, null, 100));
		Assert.assertEquals("Optional `width` argument: Numeric values should be converted to integers", JSON.stringify(value, null, 5.99999), JSON.stringify(value, null, 5));

		// Test 15.12.3-6-b-1 and 15.12.3-6-b-4.
		Assert.assertEquals("Optional `width` argument: Numeric width values between 0 and 1 should be ignored", JSON.stringify(value, null, 0.999999), JSON.stringify(value));
		Assert.assertEquals("Optional `width` argument: Zero should be ignored", JSON.stringify(value, null, 0), JSON.stringify(value));
		Assert.assertEquals("Optional `width` argument: Negative numeric values should be ignored", JSON.stringify(value, null, -5), JSON.stringify(value));
		Assert.assertEquals("Optional `width` argument: Numeric width values in the range [1, 10] should produce identical results to that of string values containing `width` spaces", JSON.stringify(value, null, 5), JSON.stringify(value, null, "     "));

		// Test 15.12.3-7-a-1.
		Assert.assertEquals("Optional `width` argument: String width values longer than 10 characters should be truncated", JSON.stringify(value, null, "0123456789xxxxxxxxx"), JSON.stringify(value, null, "0123456789"));

		// Test 15.12.3-8-a-1 thru 15.12.3-8-a-5.
		Assert.assertEquals("Empty string `width` arguments should be ignored", JSON.stringify(value, null, ""), JSON.stringify(value));
		Assert.assertEquals("Boolean primitive `width` arguments should be ignored", JSON.stringify(value, null, true), JSON.stringify(value));
		Assert.assertEquals("`null` `width` arguments should be ignored", JSON.stringify(value, null, null), JSON.stringify(value));
		Assert.assertEquals("Boolean object `width` arguments should be ignored", JSON.stringify(value, null, new Boolean(false)), JSON.stringify(value));
		Assert.assertEquals("Object literal `width` arguments should be ignored", JSON.stringify(value, null, value), JSON.stringify(value));

		// Test 15.12.3@2-3-a-1.
		this.serializes("[\"fortytwo\"]", a, "The `JSON.stringify` callback function may return a string object when called on an array", new JSON.Replacer() {
			public Object replace(Object target, String key, Object value) {
				if (value instanceof Number)
					if (((Number) value).intValue() == 42)
						return new String("fortytwo");
				return value;
			}
		}, null);

		// Test 15.12.3@2-3-a-2.
		Object[] b = { 84 };
		this.serializes("[84]", b, "The `JSON.stringify` callback function may return a number object when called on an array", new JSON.Replacer() {
			public Object replace(Object target, String key, Object value) {
				if (value instanceof Number)
					if (((Number) value).intValue() == 42)
						return new Long(84);
				return value;
			}
		}, null);

		// Test 15.12.3@2-3-a-3.
		this.serializes("[false]", a, "The `JSON.stringify` callback function may return a Boolean object when called on an array", new JSON.Replacer() {
			public Object replace(Object target, String key, Object value) {
				if (value instanceof Number)
					if (((Number) value).intValue() == 42)
						return new Boolean(false);
				return value;
			}
		}, null);

		// Test 15.12.3@4-1-2. 15.12.3@4-1-1 only tests whether an exception is thrown; the type of the exception is not checked.
		Map<String, Object> c = new LinkedHashMap<String, Object>();
		c.put("prop", c);
		this.cyclicError(c, "An object containing a circular reference should throw a `TypeError`");

		// Test 15.12.3@4-1-3, modified to ensure that a `TypeError` is thrown.
		Map<String, Object> d = new LinkedHashMap<String, Object>();
		Map<String, Object> e = new LinkedHashMap<String, Object>();
		Map<String, Object> f = new LinkedHashMap<String, Object>();
		e.put("p2", f);
		d.put("p1", e);
		f.put("prop", d);
		this.cyclicError(d, "A nested cyclic structure should throw a `TypeError`");
	}

	@Test
	public void testStringifyMisc() throws IOException {
		String innervalue = "don\t even";
		Map<String, Object> inner = new LinkedHashMap<String, Object>();
		inner.put("inner", innervalue);
		String outervalue = JSON.stringify(inner);
		Map<String, Object> outer = new LinkedHashMap<String, Object>();
		outer.put("outer", outervalue);
		String expectedStr = "{\"outer\":\"{\\\"inner\\\":\\\"don\\\\t even\\\"}\"}";
		String actualStr = JSON.stringify(outer);
		Assert.assertEquals("Stringify Embedded", expectedStr, actualStr);
		Object actualObj = JSON.parse(expectedStr);
		Assert.assertEquals("Parse Embedded", outer, actualObj);
	}

	@After
	public void tearDown() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
}
