package com.bytelightning.oss.lib.sio;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Classic splitter of OutputStream. Named after the unix 'tee' command. It allows a stream to be branched off so there are now two streams.
 * 
 */
public class TeeOutputStream extends OutputStream {

	public TeeOutputStream(OutputStream o1, OutputStream o2) throws IOException {
		this.o1 = o1;
		this.o2 = o2;
	}
	private OutputStream o1;
	private OutputStream o2;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(int b) throws IOException {
		o1.write(b);
		o2.write(b);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte b[]) throws IOException {
		o1.write(b);
		o2.write(b);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		o1.write(b, off, len);
		o2.write(b, off, len);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush() throws IOException {
		o1.flush();
		o2.flush();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		o1.close();
		o2.close();
	}
}
