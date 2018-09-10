package com.bytelightning.oss.lib.csv;

import java.io.IOException;

/**
 * Abstract interface for reading cvs input.
 */
public interface Reader {
	/**
	 * Consume the next character and return it.
	 */
	int read() throws IOException;

	/**
	 * Returns the next character that would be read (without consuming it).
	 */
	int peek() throws IOException;

	/**
	 * Returns the most recently 'read' character.
	 */
    int current() throws IOException;

	/**
	 * Consume characters up until EOL.
	 */
	CharSequence readLine() throws IOException;
}
