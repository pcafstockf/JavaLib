package com.bytelightning.oss.lib.json;

/**
 * Listener/Callback for the streaming JsonSaxer.
 */
public interface JsonSaxListener {
    void startDocument();
    Object endDocument(Throwable err);

    void startArray();
    void startElem();
    void endElem();
    void endArray();

    void startObject();
    void startField(String name);
    void endField(String name);
    void endObject();

    void stringValue(String value);
    void numberValue(Number value);
    void booleanValue(Boolean value);
    void nullValue();
}