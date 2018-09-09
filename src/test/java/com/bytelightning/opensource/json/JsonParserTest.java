package com.bytelightning.opensource.json;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.bytelightning.oss.lib.json.JsonParser;

import org.junit.Assert;

public class JsonParserTest extends AbsJsonParserTest {	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Override
	void parseError(String source, final String message) {
		JsonParser jp = new JsonParser() {
			protected void yyerror(String msg) {
				throw new AssertionError(message);
			}
		};
		try {
			jp.parse(source);
			Assert.fail(message);
		}
		catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		catch (Error e) {
			Assert.assertTrue(message, true);
		}
	}

	@Override
	void parses(Object expected, String source, final String message) {
		JsonParser jp = new JsonParser() {
			protected void yyerror(String msg) {
				throw new AssertionError(message);
			}

			protected Map<String, Object> makeMap() {
				return new LinkedHashMap<String, Object>();
			}
		};
		try {
			Object result = jp.parse(source);
			if (expected != null)
				Assert.assertEquals(message, expected, result);
			else
				Assert.assertTrue(message, true);
		}
		catch (Throwable e) {
			throw new AssertionError(message);
		}
	}
	
	@After
	public void tearDown() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
}
