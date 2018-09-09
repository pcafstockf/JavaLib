package com.bytelightning.oss.lib.json;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Stack;

@SuppressWarnings("unused")
%%
// Generate this with: 
// 		java -cp /Developer/JavaLibs/jflex.de/jflex-1.4.3/jflex-1.4.3/lib/JFlex.jar JFlex.Main -d src/main/java/com/bytelightning/opensource/json src/main/java/com/bytelightning/opensource/json/JsonLexer.lex
//
%{
public int getPosition() {
	return yychar;
}
public void reset(java.io.Reader in) {
	yyreset(in);
	currentToken = -1;
}
public int nextToken() {
	try {
		return (currentToken = yylex());
	}
	catch (IOException e) {
		// Some parsers do not expect us to *declare* any exceptions, so we will have to wrap and throw.
		throw new RuntimeException(e.getMessage(), e);
	}
}
public int currentToken() {
	if (currentToken == -1)
		return nextToken();
	return currentToken;
}
public String getString() {
	return new String(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}
public Object getSemantic() {
	Object retVal;
	switch (currentToken()) {
		case STRING:
			retVal = new String(zzBuffer, zzStartRead + 1, zzMarkedPos-zzStartRead - 2);
			break;
		case INTEGER:
			retVal = makeIntegerSem(getString());
			break;
		case FLOAT:
			retVal = makeFloatSem(getString());
			break;
		case TRUE:
			retVal = Boolean.TRUE;
			break;
		case FALSE:
			retVal = Boolean.FALSE;
			break;
		default:
			retVal = null;
			break;
	}
	return retVal;
}
protected Object makeIntegerSem(String txt) {
	return Long.valueOf(txt);
}
protected Object makeFloatSem(String txt) {
	return Double.valueOf(txt);
}
private int currentToken = -1;
%}

%class JsonLexer
%implements com.bytelightning.oss.lib.json.JsonTokens
%public
%integer
%apiprivate
%table
%unicode
%char

DIGIT		= [0-9]
DIGIT1		= [1-9]
INTNUM		= -?{DIGIT1}{DIGIT}*
FRACT		= \.{DIGIT}+
FLOAT		= ({INTNUM}|-?0){FRACT}?
EXP			= [eE][+-]?{DIGIT}+
NUMBER		= {FLOAT}{EXP}?

UNICODE		= \\u[A-Fa-f0-9]{4}
ESCAPECHAR	= \\[\"\\/bfnrt]
CHAR		= [^\"\\]|{ESCAPECHAR}|{UNICODE}
STRING 		= \"{CHAR}*\"

WHITESPACE	= [ \t\n\r\f\u00A0\uFEFF\u2028\u2029]+

%%

\{              {return LBRACE;}
\}              {return RBRACE;}
\[              {return LBRACKET;}
\]              {return RBRACKET;}
,               {return COMMA;}
:               {return COLON;}
true            {return TRUE;}
false           {return FALSE;}
null            {return NULL;}
{STRING}        {return STRING;}
{INTNUM}        {return INTEGER;}
{NUMBER}        {return FLOAT;}

{WHITESPACE}    {}


<<EOF>>			{return ENDINPUT;}
