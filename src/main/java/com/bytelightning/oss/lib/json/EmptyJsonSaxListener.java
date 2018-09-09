package com.bytelightning.oss.lib.json;

public class EmptyJsonSaxListener implements JsonSaxListener {
	@Override
	public void startDocument() {
	}
	@Override
	public Object endDocument(Throwable err) {
		return null;
	}
	@Override
	public void startArray() {
	}
	@Override
	public void startElem() {
	}
	@Override
	public void endElem() {
	}
	@Override
	public void endArray() {
	}
	@Override
	public void startObject() {
	}
	@Override
	public void startField(String name) {
	}
	@Override
	public void endField(String name) {
	}
	@Override
	public void endObject() {
	}
	@Override
	public void stringValue(String value) {
	}
	@Override
	public void numberValue(Number value) {
	}
	@Override
	public void booleanValue(Boolean value) {
	}
	@Override
	public void nullValue() {
	}
}
