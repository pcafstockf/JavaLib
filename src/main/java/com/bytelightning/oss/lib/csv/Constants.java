/*
 * Lifted from apache-commons-cvs
 */
package com.bytelightning.oss.lib.csv;

/**
 * Constants for this package.
 */
public final class Constants {

	public static final char BACKSLASH = '\\';

	public static final char BACKSPACE = '\b';

	public static final char COMMA = ',';

	/**
	 * Starts a comment, the remainder of the line is the comment.
	 */
	public static final char COMMENT = '#';

	public static final char CR = '\r';

	/**
	 * RFC 4180 defines line breaks as CRLF
	 */
	public static final String CRLF = "\r\n";

	public static final Character DOUBLE_QUOTE_CHAR = '"';

	public static final String EMPTY = "";

	/**
	 * The end of stream symbol
	 */
	public static final int END_OF_STREAM = -1;

	public static final char FF = '\f';

	public static final char LF = '\n';

	/**
	 * Unicode line separator.
	 */
	public static final String LINE_SEPARATOR = "\u2028";

	/**
	 * Unicode next line.
	 */
	public static final String NEXT_LINE = "\u0085";

	/**
	 * Unicode paragraph separator.
	 */
	public static final String PARAGRAPH_SEPARATOR = "\u2029";

	public static final char PIPE = '|';

	/**
	 * ASCII record separator
	 */
	public static final char RS = 30;

	public static final char SP = ' ';

	public static final char TAB = '\t';

	/**
	 * Undefined state for the lookahead char
	 */
	public static final int UNDEFINED = -2;

	/**
	 * ASCII unit separator
	 */
	public static final char US = 31;

	/**
	 * Constant char to use for disabling comments, escapes and encapsulation. The value -2 is used because it
	 * won't be confused with an EOF signal (-1), and because the Unicode value {@code FFFE} would be encoded as two
	 * chars (using surrogates) and thus there should never be a collision with a real text char.
	 */
	public static final char DISABLED = '\ufffe';
}
