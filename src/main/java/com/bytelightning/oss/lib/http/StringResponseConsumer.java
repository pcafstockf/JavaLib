package com.bytelightning.oss.lib.http;

import org.apache.http.ContentTooLongException;
import org.apache.http.Header;
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
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

/**
 * Converts an AsyncResponse into a String.
 */
public class StringResponseConsumer extends AbstractAsyncResponseConsumer<String> {

	public StringResponseConsumer() {
		this(256 * 1024);
	}
	public StringResponseConsumer(int maxBufferSize) {
		super();
		this.maxBufferSize = maxBufferSize;
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
		if (entity == null)
			return null;
		InputStream content = entity.getContent();
		Header hdr = entity.getContentEncoding();
		if (hdr != null && hdr.getValue().equalsIgnoreCase("gzip"))
			content = new GZIPInputStream(content);
		return streamToString(content, (int)entity.getContentLength(), ContentType.get(entity));
	}
	
	private static String streamToString(InputStream instream, int capacity, ContentType contentType) throws IOException {
		if (instream == null)
			return null;
		try {
			if (capacity < 0)
				capacity = 4096;
			Charset charset = null;
			if (contentType != null) {
				charset = contentType.getCharset();
				if (charset == null) {
					final ContentType defaultContentType = ContentType.getByMimeType(contentType.getMimeType());
					charset = defaultContentType != null ? defaultContentType.getCharset() : null;
				}
			}
			if (charset == null)
				charset = HTTP.DEF_CONTENT_CHARSET;
			final Reader reader = new InputStreamReader(instream, charset);
			final CharArrayBuffer buffer = new CharArrayBuffer(capacity);
			final char[] tmp = new char[1024];
			int l;
			while ((l = reader.read(tmp)) != -1)
				buffer.append(tmp, 0, l);
			return buffer.toString();
		}
		finally {
			instream.close();
		}
	}
}
