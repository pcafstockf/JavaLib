package com.bytelightning.oss.lib.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Default SAX listener simply writes compressed json to the specified Writer.
 */
public class DefaultJsonSaxListener implements JsonSaxListener {

	public DefaultJsonSaxListener() {
	}
	protected Stack<Object> cursor;
	protected Stack<String> fieldNames;
	
	@Override
	public void startDocument() {
		this.cursor = new Stack<Object>();
		this.fieldNames = new Stack<String>();
	}
	
	@Override
	public void startArray() {
		this.cursor.push(this.makeArray());
	}
	protected Object makeArray() {
		return new ArrayList<Object>();
	}
	@Override
	public void startElem() {
	}
	@SuppressWarnings("unchecked")
	@Override
	public void endElem() {
		Object val = this.cursor.pop();
		((List<Object>)this.cursor.peek()).add(val);
	}
	@Override
	public void endArray() {
	}
	
	@Override
	public void startObject() {
		this.cursor.push(this.makeMap());
	}
	protected Map<String,Object> makeMap() {
		return new LinkedHashMap<String, Object>();
	}
	@Override
	public void startField(String name) {
		this.fieldNames.push(name);
	}
	@SuppressWarnings("unchecked")
	@Override
	public void endField(String name) {
		Object val = this.cursor.pop();
		((Map<String, Object>)this.cursor.peek()).put(this.fieldNames.pop(), val);
	}
	@Override
	public void endObject() {
	}

	@Override
	public void stringValue(String value) {
		this.cursor.push(value);
	}
	@Override
	public void numberValue(Number value) {
		this.cursor.push(value);
	}
	@Override
	public void booleanValue(Boolean value) {
		this.cursor.push(value);
	}
	@Override
	public void nullValue() {
		this.cursor.push(null);
	}

	@Override
	public Object endDocument(Throwable err) {
		if (err == null) {
			if (this.cursor.size() == 1)
				return this.cursor.pop();
			else
				throw new RuntimeException("Unexpected EOF");
		}
		return null;
	}
}
