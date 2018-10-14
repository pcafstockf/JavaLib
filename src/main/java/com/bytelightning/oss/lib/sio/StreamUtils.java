package com.bytelightning.oss.lib.sio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;

public class StreamUtils {
	/**
	 * Write bytes to an OutputStream.
	 * @param src If null, all bytes are assumed to be in the <code>buf</code> parameter.
	 * @param closeIn True if the <code>InputStream</code> should be closed when the write has completed.
	 * @param buf A buffer that should contain all bytes to write if the InputStream is null, OR a buffer to be used for copying between input and output streams, OR null if you want a default buffer allocated.
	 * @param dst Destination of the data (null is allowed).
	 * @param closeOut True if the <code>OutputStream</code> should be closed when the write has completed.
	 * @return Returns the number of bytes written to the <code>OutputStream</code>.
	 * @throws IOException
	 */
	public static long write(InputStream src, boolean closeIn, byte[] buf, OutputStream dst, boolean closeOut) throws IOException {
		long totalWritten = 0;
		try {
			if (src == null) {
				if (dst != null)
					dst.write(buf, 0, buf.length);
				totalWritten = buf.length;
			}
			else {
				if (buf == null)
					buf = new byte[8192];
				int numRead = src.read(buf);
				while (numRead != -1) {
					if (dst != null)
						dst.write(buf, 0, numRead);
					totalWritten += numRead;
					numRead = src.read(buf);
				}
			}
		}
		finally {
			if (closeIn && (src != null))
				src.close();
			if (dst != null) {
				dst.flush();
				if (closeOut)
					dst.close();
			}
		}
		return totalWritten;
	}
	
	/**
	 * Write a ByteBuffer to an OutputStream
	 * @param src Should contain the data to be written
	 * @param buf A buffer that should contain all bytes to write if the ByteBuffer is null, OR a buffer to be used for copying between input and output streams, OR null if you want a default buffer allocated.
	 * @param dst Destination of the data (null is allowed).
	 * @param closeOut True if the <code>OutputStream</code> should be closed when the write has completed.
	 * @return Returns the number of bytes written to the <code>OutputStream</code>.
	 * @throws IOException
	 */
	public static long write(ByteBuffer src, byte[] buf, OutputStream dst, boolean closeOut) throws IOException {
		long totalWritten = 0;
		try {
			if (src == null) {
				if (dst != null)
					dst.write(buf, 0, buf.length);
				totalWritten = buf.length;
			}
			else {
				if (buf == null)
					buf = new byte[8192];
				int numRead = Math.min(buf.length, src.remaining());
				while (numRead > 0) {
					src.get(buf, 0, numRead);
					if (dst != null)
						dst.write(buf, 0, numRead);
					totalWritten += numRead;
					numRead = Math.min(buf.length, src.remaining());
				}
			}
		}
		finally {
			if (dst != null) {
				dst.flush();
				if (closeOut)
					dst.close();
			}
		}
		return totalWritten;
	}

	/**
	 * Write chars to a Writer.
	 * @param src If null, all chars are assumed to be in the <code>buf</code> parameter.
	 * @param closeIn True if the <code>Reader</code> should be closed when the write has completed.
	 * @param buf A buffer that should contain all chars to write if the Reader is null, OR a buffer to be used for copying between reader and writer streams, OR null if you want a default buffer allocated.
	 * @param dst Destination of the data (null is allowed).
	 * @param closeOut True if the <code>Writer</code> should be closed when the write has completed.
	 * @return Returns the number of chars written to the <code>Writer</code>.
	 * @throws IOException
	 */
	public static long write(Reader src, boolean closeIn, char[] buf, Writer dst, boolean closeOut) throws IOException {
		long totalWritten = 0;
		try {
			if (src == null) {
				if (dst != null)
					dst.write(buf, 0, buf.length);
				totalWritten = buf.length;
			}
			else {
				if (buf == null)
					buf = new char[8192];
				int numRead = src.read(buf);
				while (numRead != -1) {
					if (dst != null)
						dst.write(buf, 0, numRead);
					totalWritten += numRead;
					numRead = src.read(buf);
				}
			}
		}
		finally {
			if (closeIn && (src != null))
				src.close();
			if (dst != null) {
				dst.flush();
				if (closeOut)
					dst.close();
			}
		}
		return totalWritten;
	}
	/**
	 * Write a ByteBuffer to an OutputStream
	 * @param src Should contain the data to be written
	 * @param buf A buffer that should contain all bytes to write if the ByteBuffer is null, OR a buffer to be used for copying between input and output streams, OR null if you want a default buffer allocated.
	 * @param dst Destination of the data (null is allowed).
	 * @return Returns the number of bytes written to the <code>ByteBuffer</code>.
	 * @throws IOException
	 */
	public static long write(InputStream src, byte[] buf, ByteBuffer dst) throws IOException {
		long totalWritten = 0;
		if (src != null) {
			if (dst != null && dst.isDirect()) {
				int pos = dst.position();
				totalWritten = src.read(dst.array(), pos, dst.remaining());
				dst.position(pos + (int) totalWritten);
			}
			else {
				if (buf == null)
					buf = new byte[8192];
				int numRead = Math.min(buf.length, dst == null ? Integer.MAX_VALUE : dst.remaining());
				while (numRead > 0) {
					numRead = src.read(buf, 0, numRead);
					if (numRead < 0) {
						if (totalWritten == 0)
							totalWritten = -1;
						break;
					}
					if (dst != null) {
						dst.put(buf, 0, numRead);
						totalWritten += numRead;
					}
					numRead = Math.min(buf.length, dst == null ? Integer.MAX_VALUE : dst.remaining());
				}
			}
		}
		return totalWritten;
	}
}
