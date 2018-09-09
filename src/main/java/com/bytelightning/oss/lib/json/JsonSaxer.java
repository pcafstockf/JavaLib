package com.bytelightning.oss.lib.json;

import java.io.IOException;

/**
 * Streaming SAX event driven json parser.
 */
public class JsonSaxer implements JsonTokens {
	public JsonSaxer() {
	}
	private JsonLexer lexer;
	private JsonSaxListener listener;
	
	public Object parse(java.io.Reader in, JsonSaxListener l) throws IOException {
		if (lexer == null)
			lexer = new JsonLexer(in);
		else
			lexer.reset(in);
		listener = l;

		listener.startDocument();
		try {
			parseValue(lexer.nextToken());
		}
		catch (Error t) {
			listener.endDocument(t);
			throw t;
		}
		catch (RuntimeException t) {
			listener.endDocument(t);
			throw t;
		}
		catch (Throwable t) {
			listener.endDocument(t);
			throw new IOException("Parsing error", t);
		}
		return listener.endDocument(null);
	}
    private void parseArray() {
    	listener.startArray();
    	int tok = lexer.nextToken();
    	while (tok != RBRACKET) {
    		listener.startElem();
    		parseValue(tok);
    		listener.endElem();
    		tok = lexer.nextToken();
    		if (tok == COMMA)
    			tok = lexer.nextToken();
    		else if (tok != RBRACKET)
    			throw new RuntimeException("Illegal token at position " + lexer.getPosition());
    	}
		listener.endArray();
	}
	private void parseValue(int tok) {
		switch (tok) {
		case STRING:
			listener.stringValue(JsonUtils.UnEscapeJsonString((String)lexer.getSemantic()));
			break;
		case INTEGER:
		case FLOAT:
			listener.numberValue((Number)lexer.getSemantic());
			break;
		case TRUE:
			listener.booleanValue(Boolean.TRUE);
			break;
		case FALSE:
			listener.booleanValue(Boolean.FALSE);
			break;
		case NULL:
			listener.nullValue();
			break;
		case LBRACE:
			parseObject();
			break;
		case LBRACKET:
			parseArray();
			break;
		default:
			throw new RuntimeException("Illegal token (" + tok + ") at position " + lexer.getPosition());
		}
	}
	private void parseObject() {
		listener.startObject();
    	int tok = lexer.nextToken();
    	while (tok != RBRACE) {
    		if (tok != STRING)
    			throw new RuntimeException("Illegal token at position " + lexer.getPosition());
    		String key = (String)lexer.getSemantic();
    		tok = lexer.nextToken();
    		if (tok != COLON)
    			throw new RuntimeException("Illegal token at position " + lexer.getPosition());
    		tok = lexer.nextToken();
    		listener.startField(key);
    		parseValue(tok);
    		listener.endField(key);
    		tok = lexer.nextToken();
			if (tok == COMMA)
    			tok = lexer.nextToken();
    		else if (tok != RBRACE)
    			throw new RuntimeException("Illegal token at position " + lexer.getPosition());
    	}
		listener.endObject();
	}

	public void parse(String in, JsonSaxListener l) throws IOException {
		parse(new java.io.StringReader(in), l);
	}
}
