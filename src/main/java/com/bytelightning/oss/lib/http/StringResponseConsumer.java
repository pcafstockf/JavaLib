package com.bytelightning.oss.lib.http;

import org.apache.http.ContentTooLongException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.ContentBufferEntity;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SimpleInputBuffer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Asserts;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Converts an AsyncResponse into a String.
 */
public class StringResponseConsumer extends AbstractAsyncResponseConsumer<String> {

	public StringResponseConsumer() {
		this(256 * 1024);
	}
	public StringResponseConsumer(int maxBufferSize) {
		super();
	}
	private volatile int maxBufferSize;
	private volatile HttpResponse response;
	private volatile SimpleInputBuffer buf;

	@Override
	protected void onResponseReceived(final HttpResponse response) throws IOException {
		this.response = response;
	}

	@Override
	protected void onEntityEnclosed(final HttpEntity entity, final ContentType contentType) throws IOException {
		long len = entity.getContentLength();
		if (len > Integer.MAX_VALUE)
			throw new ContentTooLongException("Entity content is too long: " + len);
		if (len < 0)
			len = 4096;
		final int initialBufferSize = Math.min((int) len, maxBufferSize);
		this.buf = new SimpleInputBuffer(initialBufferSize, new HeapByteBufferAllocator());
		this.response.setEntity(new ContentBufferEntity(entity, this.buf));
	}

	@Override
	protected void onContentReceived(final ContentDecoder decoder, final IOControl ioctrl) throws IOException {
		Asserts.notNull(this.buf, "Content buffer");
		this.buf.consumeContent(decoder);
	}

	@Override
	protected void releaseResources() {
		this.response = null;
		this.buf = null;
	}

	@Override
	protected String buildResult(HttpContext context) throws Exception {
		StatusLine status = response.getStatusLine();
		int statusCode = status.getStatusCode();
		if (statusCode >= 400)
			throw new HttpResponseException(statusCode, status.getReasonPhrase());
		HttpEntity entity = response.getEntity();
		return entity != null ? EntityUtils.toString(entity) : null;
	}
}
