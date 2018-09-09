package com.bytelightning.oss.lib.sio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class StreamUtils {
	/**
	 * Write bytes to an OutputStream.
	 * @param r If null, all bytes are assumed to be in the <code>buf</code> parameter.
	 * @param closeIn True if the <code>InputStream</code> should be closed when the write has completed.
	 * @param buf A buffer that should contain all bytes to write if the InputStream is null, OR a buffer to be used for copying between input and output streams, OR null if you want a default buffer allocated.
	 * @param w Destination of the data (null is allowed).
	 * @param closeOut True if the <code>OutputStream</code> should be closed when the write has completed.
	 * @return Returns the number of bytes written to the <code>OutputStream</code>.
	 * @throws IOException
	 */
	public static long write(InputStream r, boolean closeIn, byte[] buf, OutputStream w, boolean closeOut) throws IOException {
		long totalWritten = 0;
		try {
			if (r == null) {
				if (w != null)
					w.write(buf, 0, buf.length);
				totalWritten = buf.length;
			}
			else {
				if (buf == null)
					buf = new byte[8192];
				int numRead = r.read(buf);
				while (numRead != -1) {
					if (w != null)
						w.write(buf, 0, numRead);
					totalWritten += numRead;
					numRead = r.read(buf);
				}
			}
		}
		finally {
			if (closeIn && (r != null))
				r.close();
			if (w != null) {
				w.flush();
				if (closeOut)
					w.close();
			}
		}
		return totalWritten;
	}

	/**
	 * Write chars to a Writer.
	 * @param r If null, all chars are assumed to be in the <code>buf</code> parameter.
	 * @param closeIn True if the <code>Reader</code> should be closed when the write has completed.
	 * @param buf A buffer that should contain all chars to write if the Reader is null, OR a buffer to be used for copying between reader and writer streams, OR null if you want a default buffer allocated.
	 * @param w Destination of the data (null is allowed).
	 * @param closeOut True if the <code>Writer</code> should be closed when the write has completed.
	 * @return Returns the number of chars written to the <code>Writer</code>.
	 * @throws IOException
	 */
	public static long write(Reader r, boolean closeIn, char[] buf, Writer w, boolean closeOut) throws IOException {
		long totalWritten = 0;
		try {
			if (r == null) {
				if (w != null)
					w.write(buf, 0, buf.length);
				totalWritten = buf.length;
			}
			else {
				if (buf == null)
					buf = new char[8192];
				int numRead = r.read(buf);
				while (numRead != -1) {
					if (w != null)
						w.write(buf, 0, numRead);
					totalWritten += numRead;
					numRead = r.read(buf);
				}
			}
		}
		finally {
			if (closeIn && (r != null))
				r.close();
			if (w != null) {
				w.flush();
				if (closeOut)
					w.close();
			}
		}
		return totalWritten;
	}
}
