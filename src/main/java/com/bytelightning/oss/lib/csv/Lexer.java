/*
 * Lifted largely from apache-commons-cvs, but modified to be able to parse just one line
 */
package com.bytelightning.oss.lib.csv;

import java.io.IOException;

/**
 * Lexical analyzer.
 */
public class Lexer {

	public Lexer(char delimiter, char escape, char quoteChar, char commentStart, boolean ignoreSurroundingSpaces, boolean ignoreEmptyLines) {
		this.delimiter = delimiter;
		this.escape = escape;
		this.quoteChar = quoteChar;
		this.commentStart = commentStart;
		this.ignoreSurroundingSpaces = ignoreSurroundingSpaces;
		this.ignoreEmptyLines = ignoreEmptyLines;
	}

	protected static final String CR_STRING = Character.toString(Constants.CR);
	protected static final String LF_STRING = Character.toString(Constants.LF);

	protected final char delimiter;
	protected final char escape;
	protected final char quoteChar;
	protected final char commentStart;
	protected final boolean ignoreSurroundingSpaces;
	protected final boolean ignoreEmptyLines;

	protected String firstEol;

	/**
	 * Returns the next token.
	 * <p>
	 * A token corresponds to a term, a record change or an end-of-file indicator.
	 * </p>
	 *
	 * @param token an existing Token object to reuse. The caller is responsible to initialize the Token.
	 * @return the next token found
	 * @throws java.io.IOException on stream access error
	 */
	public Token nextToken(Reader reader, Token token) throws IOException {
		if (token == null)
			token = new Token();

		// get the last read char (required for empty line detection)
		int lastChar = reader.current();

		// read the next char and set eol
		int c = reader.read();
		/*
		 * Note: The following call will swallow LF if c == CR. But we don't need to know if the last char was CR or LF
		 * - they are equivalent here.
		 */
		boolean eol = readEndOfLine(c, reader);

		// empty line detection: eol AND (last char was EOL or beginning)
		if (ignoreEmptyLines) {
			while (eol && isStartOfLine(lastChar)) {
				// go on char ahead ...
				lastChar = c;
				c = reader.read();
				eol = readEndOfLine(c, reader);
				// reached end of file without any content (empty line at the end)
				if (isEndOfFile(c)) {
					token.type = Token.Type.EOF;
					// don't set token.isReady here because no content
					return token;
				}
			}
		}

		// did we reach eof during the last iteration already ? EOF
		if (isEndOfFile(lastChar) || !isDelimiter(lastChar) && isEndOfFile(c)) {
			token.type = Token.Type.EOF;
			// don't set token.isReady here because no content
			return token;
		}

		if (isStartOfLine(lastChar) && isCommentStart(c)) {
			final CharSequence line = reader.readLine();
			if (line == null) {
				token.type = Token.Type.EOF;
				// don't set token.isReady here because no content
				return token;
			}
			final String comment = line.toString().trim();
			token.content.append(comment);
			token.type = Token.Type.COMMENT;
			return token;
		}

		// important: make sure a new char gets consumed in each iteration
		while (token.type == Token.Type.INVALID) {
			// ignore whitespaces at beginning of a token
			if (ignoreSurroundingSpaces) {
				while (isWhitespace(c) && !eol) {
					c = reader.read();
					eol = readEndOfLine(c, reader);
				}
			}

			// ok, start of token reached: encapsulated, or token
			if (isDelimiter(c)) {
				// empty token return TOKEN("")
				token.type = Token.Type.TOKEN;
			} else if (eol) {
				// empty token return EORECORD("")
				// noop: token.content.append("");
				token.type = Token.Type.EORECORD;
			} else if (isQuoteChar(c)) {
				// consume encapsulated token
				parseEncapsulatedToken(token, reader);
			} else if (isEndOfFile(c)) {
				// end of file return EOF()
				// noop: token.content.append("");
				token.type = Token.Type.EOF;
				token.isReady = true; // there is data at EOF
			} else {
				// next token must be a simple token
				// add removed blanks when not ignoring whitespace chars...
				parseSimpleToken(token, c, reader);
			}
		}
		return token;
	}

	/**
	 * Parses a simple token.
	 * <p/>
	 * Simple token are tokens which are not surrounded by encapsulators. A simple token might contain escaped
	 * delimiters (as \, or \;). The token is finished when one of the following conditions become true:
	 * <ul>
	 * <li>end of line has been reached (EORECORD)</li>
	 * <li>end of stream has been reached (EOF)</li>
	 * <li>an unescaped delimiter has been reached (TOKEN)</li>
	 * </ul>
	 *
	 * @param token the current token
	 * @param ch    the current character
	 * @throws IOException on stream access error
	 */
	protected void parseSimpleToken(final Token token, int ch, Reader reader) throws IOException {
		// Faster to use while(true)+break than while(token.type == INVALID)
		while (true) {
			if (readEndOfLine(ch, reader)) {
				token.type = Token.Type.EORECORD;
				break;
			} else if (isEndOfFile(ch)) {
				token.type = Token.Type.EOF;
				token.isReady = true; // There is data at EOF
				break;
			} else if (isDelimiter(ch)) {
				token.type = Token.Type.TOKEN;
				break;
			} else if (isEscape(ch)) {
				final int unescaped = readEscape(reader);
				if (unescaped == Constants.END_OF_STREAM) { // unexpected char after escape
					token.content.append((char) ch).append((char) reader.current());
				} else {
					token.content.append((char) unescaped);
				}
				ch = reader.read(); // continue
			} else {
				token.content.append((char) ch);
				ch = reader.read(); // continue
			}
		}

		if (ignoreSurroundingSpaces) {
			trimTrailingSpaces(token.content);
		}
	}

	/**
	 * Parses an encapsulated token.
	 * <p/>
	 * Encapsulated tokens are surrounded by the given encapsulating-string. The encapsulator itself might be included
	 * in the token using a doubling syntax (as "", '') or using escaping (as in \", \'). Whitespaces before and after
	 * an encapsulated token are ignored. The token is finished when one of the following conditions become true:
	 * <ul>
	 * <li>an unescaped encapsulator has been reached, and is followed by optional whitespace then:</li>
	 * <ul>
	 * <li>delimiter (TOKEN)</li>
	 * <li>end of line (EORECORD)</li>
	 * </ul>
	 * <li>end of stream has been reached (EOF)</li> </ul>
	 *
	 * @param token the current token
	 * @throws IOException on invalid state: EOF before closing encapsulator or invalid character before delimiter or EOL
	 */
	protected void parseEncapsulatedToken(final Token token, Reader reader) throws IOException {
		int c;
		while (true) {
			c = reader.read();

			if (isEscape(c)) {
				final int unescaped = readEscape(reader);
				if (unescaped == Constants.END_OF_STREAM) { // unexpected char after escape
					token.content.append((char) c).append((char) reader.current());
				} else {
					token.content.append((char) unescaped);
				}
			} else if (isQuoteChar(c)) {
				if (isQuoteChar(reader.peek())) {
					// double or escaped encapsulator -> add single encapsulator to token
					c = reader.read();
					token.content.append((char) c);
				} else {
					// token finish mark (encapsulator) reached: ignore whitespace till delimiter
					while (true) {
						c = reader.read();
						if (isDelimiter(c)) {
							token.type = Token.Type.TOKEN;
							return;
						} else if (isEndOfFile(c)) {
							token.type = Token.Type.EOF;
							token.isReady = true; // There is data at EOF
							return;
						} else if (readEndOfLine(c, reader)) {
							token.type = Token.Type.EORECORD;
							return;
						} else if (!isWhitespace(c)) {
							// error invalid char between token and next delimiter
							throw new IOException("invalid char between encapsulated token and delimiter");
						}
					}
				}
			} else if (isEndOfFile(c)) {
				// error condition (end of file before end of token)
				throw new IOException("EOF reached before encapsulated token finished");
			} else {
				// consume character
				token.content.append((char) c);
			}
		}
	}

	/**
	 * Handle an escape sequence.
	 * The current character must be the escape character.
	 * On return, the next character is available by calling {@link , Reader#current()}
	 * on the input stream.
	 *
	 * @return the unescaped character (as an int) or {@link Constants#END_OF_STREAM} if char following the escape is
	 * invalid.
	 * @throws IOException if there is a problem reading the stream or the end of stream is detected:
	 *                     the escape character is not allowed at end of strem
	 */
	protected int readEscape(Reader reader) throws IOException {
		// the escape char has just been read (normally a backslash)
		final int ch = reader.read();
		switch (ch) {
			case 'r':
				return Constants.CR;
			case 'n':
				return Constants.LF;
			case 't':
				return Constants.TAB;
			case 'b':
				return Constants.BACKSPACE;
			case 'f':
				return Constants.FF;
			case Constants.CR:
			case Constants.LF:
			case Constants.FF:
			case Constants.TAB:
			case Constants.BACKSPACE:
				return ch;
			case Constants.END_OF_STREAM:
				throw new IOException("EOF whilst processing escape sequence");
			default:
				// Now check for meta-characters
				if (isMetaChar(ch)) {
					return ch;
				}
				// indicate unexpected char - available from in.current()
				return Constants.END_OF_STREAM;
		}
	}

	protected void trimTrailingSpaces(final StringBuilder buffer) {
		int length = buffer.length();
		while (length > 0 && Character.isWhitespace(buffer.charAt(length - 1))) {
			length = length - 1;
		}
		if (length != buffer.length()) {
			buffer.setLength(length);
		}
	}

	/**
	 * Greedily accepts \n, \r and \r\n This checker consumes silently the second control-character...
	 *
	 * @return true if the given or next character is a line-terminator
	 */
	protected boolean readEndOfLine(int ch, Reader reader) throws IOException {
		// check if we have \r\n...
		if (ch == Constants.CR && reader.peek() == Constants.LF) {
			// note: does not change ch outside of this method!
			ch = reader.read();
			// Save the EOL state
			if (firstEol == null) {
				this.firstEol = Constants.CRLF;
			}
		}
		// save EOL state here.
		if (firstEol == null) {
			if (ch == Constants.LF) {
				this.firstEol = LF_STRING;
			} else if (ch == Constants.CR) {
				this.firstEol = CR_STRING;
			}
		}

		return ch == Constants.LF || ch == Constants.CR;
	}


	/**
	 * @return true if the given char is a whitespace character
	 */
	protected boolean isWhitespace(final int ch) {
		return !isDelimiter(ch) && Character.isWhitespace((char) ch);
	}

	/**
	 * Checks if the current character represents the start of a line: a CR, LF or is at the start of the file.
	 *
	 * @param ch the character to check
	 * @return true if the character is at the start of a line.
	 */
	protected boolean isStartOfLine(final int ch) {
		return ch == Constants.LF || ch == Constants.CR || ch == Constants.UNDEFINED;
	}

	/**
	 * @return true if the given character indicates end of file
	 */
	protected boolean isEndOfFile(final int ch) {
		return ch == Constants.END_OF_STREAM;
	}

	protected boolean isDelimiter(final int ch) {
		return ch == delimiter;
	}

	protected boolean isEscape(final int ch) {
		return ch == escape;
	}

	protected boolean isQuoteChar(final int ch) {
		return ch == quoteChar;
	}

	protected boolean isCommentStart(final int ch) {
		return ch == commentStart;
	}

	protected boolean isMetaChar(final int ch) {
		return ch == delimiter || ch == escape || ch == quoteChar || ch == commentStart;
	}
}
