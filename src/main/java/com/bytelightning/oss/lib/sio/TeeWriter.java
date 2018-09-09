package com.bytelightning.oss.lib.sio;

import java.io.IOException;
import java.io.Writer;

/**
 * Classic splitter of Writer. Named after the unix 'tee' command. It allows a writer to be branched off so there are now two writer.
 * 
 */
public class TeeWriter extends Writer {
	public TeeWriter(Writer w1, Writer w2) {
		this.w1 = w1;
		this.w2 = w2;
	}

	private Writer w1;
	private Writer w2;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(int c) throws IOException {
		w1.write(c);
		w2.write(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(char cbuf[]) throws IOException {
		w1.write(cbuf);
		w2.write(cbuf);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(char cbuf[], int off, int len) throws IOException {
		w1.write(cbuf, off, len);
		w2.write(cbuf, off, len);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(String str, int off, int len) throws IOException {
		w1.write(str, off, len);
		w2.write(str, off, len);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush() throws IOException {
		w1.flush();
		w2.flush();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		w1.flush();
		w2.flush();
	}
}
