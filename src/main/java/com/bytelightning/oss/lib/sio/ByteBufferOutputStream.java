package com.bytelightning.oss.lib.sio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream {
	public ByteBufferOutputStream(ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer;
		this.bytesWritten = 0;
	}
	private ByteBuffer byteBuffer;
	private int bytesWritten;

	public void write(int b) throws IOException {
		if (!byteBuffer.hasRemaining())
			flush();
		byteBuffer.put((byte) b);
		bytesWritten++;
	}

	public void write(byte[] bytes, int offset, int length) throws IOException {
		if (byteBuffer.remaining() < length)
			flush();
		byteBuffer.put(bytes, offset, length);
		bytesWritten += length;
	}

	public int getBytesWritten() {
		return bytesWritten;
	}
}
