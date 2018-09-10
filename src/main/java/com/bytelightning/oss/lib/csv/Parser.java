/*
 * Lifted largely from apache-commons-cvs, but modified to be able to parse just one line
 */
package com.bytelightning.oss.lib.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Parser {

	public Parser() {
		this(Constants.COMMA, Constants.DISABLED, Constants.DOUBLE_QUOTE_CHAR, Constants.DISABLED, false, true);
	}

	public Parser(char delimiter, char escape, char quoteChar, char commentStart, boolean ignoreSurroundingSpaces, boolean ignoreEmptyLines) {
		this(new Lexer(delimiter, escape, quoteChar, commentStart, ignoreSurroundingSpaces, ignoreEmptyLines));
	}

	public Parser(Lexer lexer) {
		this.lexer = lexer;
		this.reusableToken = new Token();
	}

	private final Token reusableToken;
	private final Lexer lexer;

	/**
	 * Parses the next record from the current point in the stream.
	 *
	 * @return the record as an array of values, or {@code null} if the end of the stream has been reached
	 * @throws IOException on parse error or input read-failure
	 */
	public List<String> nextRecord(Reader reader, List<String> recordList) throws IOException {
		StringBuilder sb = null;
		if (recordList == null)
			recordList = new ArrayList<String>();
		else
			recordList.clear();
		String txt;
		do {
			this.reusableToken.reset();
			lexer.nextToken(reader, this.reusableToken);
			switch (this.reusableToken.type) {
				case TOKEN:
				case EORECORD:
					txt = this.reusableToken.content.toString();
					recordList.add(txt);
					break;
				case EOF:
					if (this.reusableToken.isReady) {
						txt = this.reusableToken.content.toString();
						if (txt.length() > 0)
							recordList.add(txt);
					}
					break;
				case INVALID:
					throw new IOException("invalid parse sequence");
				case COMMENT: // Ignored currently
					if (sb == null) { // first comment for this record
						sb = new StringBuilder();
					}
					else {
						sb.append(Constants.LF);
					}
					sb.append(this.reusableToken.content);
					this.reusableToken.type = Token.Type.TOKEN; // Read another token
					break;
				default:
					throw new IllegalStateException("Unexpected Token type: " + this.reusableToken.type);
			}
		} while (this.reusableToken.type == Token.Type.TOKEN);

		return recordList;
	}
}
