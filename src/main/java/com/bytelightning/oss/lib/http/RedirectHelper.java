package com.bytelightning.oss.lib.http;

import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper to manage redirects without having to set a global redirect strategy for the entire http client.
 */
@SuppressWarnings("WeakerAccess")
public abstract class RedirectHelper extends DefaultRedirectStrategy {
	public RedirectHelper(int maxRedirects) {
		this.maxRedirects = maxRedirects;
		this.redirectCount = new AtomicInteger(0);
	}

	private final int maxRedirects;
	private final AtomicInteger redirectCount;

	/**
	 * Follow 3xx responses while the maximum redirect count is not exceeded, and return the first non-3xx response encountered
	 *
	 * @param originalReq The original request.
	 * @param currentRsp  The response to the original request.  If this is not a 3xx response, this method immediately returns, otherwise the redirect is followed.
	 * @param context     Keep track of the context in which this is all occuring (may be null).
	 * @return The first non-3xx response encountered (which may well be the <code>currentRsp</code> passed to this method.
	 */
	public CloseableHttpResponse followRedirects(HttpRequestBase originalReq, CloseableHttpResponse currentRsp, HttpContext context) throws IOException {
		StatusLine status = currentRsp.getStatusLine();
		if (status.getStatusCode() < 300 || status.getStatusCode() >= 400)
			return currentRsp;
		int count = redirectCount.getAndIncrement();
		if (count > maxRedirects)
			throw new IOException("Too Many Redirects");
		HttpEntity entity = currentRsp.getEntity();
		EntityUtils.consume(entity);
		HttpRequestBase newRequest;
		try {
			newRequest = (HttpRequestBase) getRedirect(originalReq, currentRsp, context);
		} catch (ProtocolException e) {
			throw new IOException(e.getMessage(), e);
		}
		CloseableHttpResponse newRsp;
		try {
			newRsp = execute(newRequest, context);
			return followRedirects(newRequest, newRsp, context);
		} finally {
			currentRsp.close();
		}
	}

	/**
	 * Subclasses should override to invoke the HttpClient they are using.
	 */
	protected abstract CloseableHttpResponse execute(HttpRequestBase req, HttpContext ctx) throws IOException;

	/**
	 * Extension point to allow for altering a new request based on the redirected URI.
	 */
	protected HttpUriRequest createNewRequest(HttpUriRequest originalReq, URI uri) throws ProtocolException {
		Constructor<? extends HttpUriRequest> c;
		try {
			c = originalReq.getClass().getDeclaredConstructor(URI.class);
		} catch (NoSuchMethodException e) {
			throw new ProtocolException("Unable to create URI based request of type " + originalReq.getClass().getName());
		}
		try {
			return c.newInstance(uri);
		} catch (Exception e) {
			throw new ProtocolException("Unable to instantiate URI based request of type " + originalReq.getClass().getName());
		}
	}

	/**
	 * WARNING: You should not call this method directly.
	 * It assumes that <code>request</code> will actually be of type <code>HttpUriRequest</code>.
	 * We make use of the built-in ability to properly extract the location URI.
	 * This does lots of good stuff, like resolving, keeping track of all the redirections, etc.
	 */
	@Override
	public HttpUriRequest getRedirect(final HttpRequest request, final HttpResponse response, final HttpContext context) throws ProtocolException {
		URI uri = getLocationURI(request, response, context);
		// If the remote server has a proxy handling it's ssl, we will get a redirect to an http port instead of the needed https.
		if (((HttpRequestBase) request).getURI().getScheme().equals("https")) {
			if (uri.getScheme().equals("http")) {
				try {
					uri = new URI("https", uri.getSchemeSpecificPart(), uri.getFragment());
				} catch (URISyntaxException e) {
					// Will never happen.
				}
			}
		}
		return createNewRequest((HttpUriRequest) request, uri);
	}
}
