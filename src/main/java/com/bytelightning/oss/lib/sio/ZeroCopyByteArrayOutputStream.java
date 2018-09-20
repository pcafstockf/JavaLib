package com.bytelightning.oss.lib.sio;

import java.io.ByteArrayOutputStream;

/**
 * Utility class to ease integrations with <code>ByteBuffer</code>.
 * 	e.g.  ByteBuffer.wrap(baos.getBuf(), 0, baos.getCount())
 */
public class ZeroCopyByteArrayOutputStream extends ByteArrayOutputStream {
	/**
	 * {@inheritDoc}
	 */
	public ZeroCopyByteArrayOutputStream() {
	}

	/**
	 * {@inheritDoc}
	 */
	public ZeroCopyByteArrayOutputStream(int size) {
		super(size);
	}

	/**
	 * Access the protected buffer of <code>ByteArrayOutputStream</code>
	 * @return
	 */
	public byte[] getBuf() {
		return buf;
	}

	/**
	 * Remember, the byte array may only be partially used.
	 */
	public int getCount() {
		return count;
	}
}
