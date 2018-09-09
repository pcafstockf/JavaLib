package com.bytelightning.oss.lib.json;

import java.io.IOException;
import java.io.Writer;

/**
 * SAX listener which simply writes compressed json to the specified Writer.
 */
public class CompressingSaxListener implements JsonSaxListener {

	public CompressingSaxListener(Writer out) {
		this.out = out;
	}
	protected Writer out;
	
	@Override
	public void startDocument() {
	}
	@Override
	public void startArray() {
		try {
			out.write('[');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		firstElem = true;
	}
	
	@Override
	public void startElem() {
		if (! firstElem) {
			try {
				out.write(',');
			} catch (IOException e) {
				throw new RuntimeException(e);
			}		
		}
	}
	private boolean firstElem;
	@Override
	public void endElem() {
		firstElem = false;
	}

	@Override
	public void endArray() {
		try {
			out.write(']');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public void startObject() {
		try {
			out.write('{');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		firstField = true;
	}
	@Override
	public void startField(String name) {
		try {
			if (! firstField)
				out.write(',');
			out.write('"');
			out.write(name);
			out.write("\":");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private boolean firstField;
	@Override
	public void endField(String name) {
		firstField = false;
	}
	@Override
	public void endObject() {
		try {
			out.write('}');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stringValue(String value) {
		try {
			out.write('"');
			out.write(value.replace("\"", "\\\""));
			out.write('"');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public void numberValue(Number value) {
		try {
			out.write(value.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public void booleanValue(Boolean value) {
		try {
			out.write(value.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public void nullValue() {
		try {
			out.write("null");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object endDocument(Throwable err) {
		try {
			if (err != null)
				out.write(err.getLocalizedMessage());
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return out;
	}
}
