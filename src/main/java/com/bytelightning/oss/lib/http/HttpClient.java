package com.bytelightning.oss.lib.http;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytelightning.oss.lib.http.HttpAsyncClient.IdleConnectionEvictor;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * This is a global service based on the Apache http-client library.
 * It may be Injected anywhere you need it, and is thread safe, concurrent, and secure.
 * NOTE: This client injects the needed Encoding, Accept, etc. headers.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class HttpClient {
	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class.getPackage().getName());

	/**
	 * Primary constructor
	 */
	public HttpClient() {
	}
	private CloseableHttpClient httpclient;
	private RequestConfig defaultRequestConfig;
	private IdleConnectionEvictor connEvictor;

	/**
	 * One of these setup methods MUST be called after construction and before use of this object.
	 */
	public void setup(boolean includeHttps, List<Header> defaultHdrs) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		setup(includeHttps ? SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		}).build() : null, defaultHdrs);
	}

	/**
	 * One of these setup methods MUST be called after construction and before use of this object.
	 */
	public void setup(File trustStore, String storeType, char[] passwrd, List<Header> defaultHdrs) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		KeyStore ks = KeyStore.getInstance(storeType);
		ks.load(new FileInputStream(trustStore), passwrd);
		setup(ks, defaultHdrs);
	}

	/**
	 * One of these setup methods MUST be called after construction and before use of this object.
	 */
	public void setup(KeyStore trustStore, List<Header> defaultHdrs) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		// Trust own CA and all self-signed certs
		SSLContext sslcontext = trustStore == null ? SSLContexts.createSystemDefault() : SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();
		setup(sslcontext, defaultHdrs);
	}

	/**
	 * One of these setup methods MUST be called after construction and before use of this object.
	 */
	public void setup(SSLContext sslcontext, List<Header> defaultHdrs) {
		// Configure default headers to be included in all requests
		if (defaultHdrs == null)
			defaultHdrs = new ArrayList<Header>();
		HttpClientBuilder clientBuilder = HttpClients.custom().setDefaultHeaders(defaultHdrs);

		Registry<ConnectionSocketFactory> socketFactory = createSocketFactoryBuilder(clientBuilder, sslcontext).build();

		HttpClientConnectionManager connManager = createConnectionManager(socketFactory);
		clientBuilder.setConnectionManager(connManager);

		defaultRequestConfig = createDefaultRequestBuilder().build();
		clientBuilder.setDefaultRequestConfig(defaultRequestConfig);

		CookieStore cookieStore = createCookieStore();
		if (cookieStore != null)
			clientBuilder.setDefaultCookieStore(cookieStore);

		httpclient = buildClient(clientBuilder);
		if (httpclient == null)
			logger.error("Failed to build the HttpClient");

		connEvictor.start();
	}

	protected RegistryBuilder<ConnectionSocketFactory> createSocketFactoryBuilder(HttpClientBuilder clientBuilder, SSLContext sslcontext) {
		RegistryBuilder<ConnectionSocketFactory> csf = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE);
		if (sslcontext != null) {
			// Allow TLSv1 protocol only
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
			csf.register("https", new SSLConnectionSocketFactory(sslcontext));
			clientBuilder.setSSLSocketFactory(sslsf);
		}
		return csf;
	}

	protected ConnectionConfig.Builder buildConnectionConfig() {
		@SuppressWarnings("UnnecessaryLocalVariable")
		ConnectionConfig.Builder connectionConfig = ConnectionConfig.custom()
				.setMalformedInputAction(CodingErrorAction.REPORT) // Defines the action to perform if the input byte sequence is not legal for this charset
				.setUnmappableInputAction(CodingErrorAction.REPORT) // Defines the action to perform if the input byte sequence is legal but cannot be mapped to a valid Unicode character
				.setCharset(Consts.UTF_8);
		return connectionConfig;
	}

	protected HttpClientConnectionManager createConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry) {
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		// Validate connections after 5 sec of inactivity
		connManager.setValidateAfterInactivity(5000);
		// Configure total max or per route limits for persistent connections that can be kept in the pool or leased by the connection manager.
		connManager.setMaxTotal(25000);
		connManager.setDefaultMaxPerRoute(20000);
		// Configure the connection manager to use connection configuration.
		connManager.setDefaultConnectionConfig(buildConnectionConfig().build());
		return connManager;
	}

	protected CookieStore createCookieStore() {
		return null; // new BasicCookieStore();
	}

	protected RequestConfig.Builder createDefaultRequestBuilder() {
		return RequestConfig.custom();
	}

	protected CloseableHttpClient buildClient(HttpClientBuilder clientBuilder) {
		return clientBuilder.build();
	}

	/**
	 * Clean up the client when it is disposed.
	 */
	public void teardown() throws IOException {
		if (httpclient != null)
			httpclient.close();
	}

	/**
	 * Extension point to allow customization of individual requests.
	 */
	public RequestConfig.Builder customRequestConfig() {
		return RequestConfig.copy(defaultRequestConfig);
	}

	/**
	 * Ensure the URL can be created
	 */
	@SuppressWarnings("WeakerAccess")
	public URI resolveUrl(String url) {
		@SuppressWarnings("UnnecessaryLocalVariable")
		URI uri = URI.create(url);
		return uri;
	}

	/**
	 * Make an HTTP HEAD request (using the provided headers) and return the response as a String.
	 *
	 * @param url     Will be converted to a URI by <code>resolveUrl</code>
	 * @param reqHdrs May be null
	 */
	public void head(String url, List<Header> reqHdrs) throws IOException {
		HttpHead httphead = new HttpHead(resolveUrl(url));
		if (reqHdrs != null)
			for (Header hdr : reqHdrs)
				httphead.setHeader(hdr);
		head(httphead);
	}

	/**
	 * Make an HTTP HEAD request (using the provided headers) and return the response as the indicated type.
	 *
	 * @param url     Will be converted to a URI by <code>resolveUrl</code>
	 * @param reqHdrs May be null
	 */
	public <T> void head(String url, List<Header> reqHdrs, ResponseHandler<? extends T> responseHandler) throws IOException {
		HttpHead httphead = new HttpHead(resolveUrl(url));
		if (reqHdrs != null)
			for (Header hdr : reqHdrs)
				httphead.setHeader(hdr);
		head(httphead, responseHandler);
	}

	/**
	 * Send the specified HTTP HEAD request and return the response as a String.
	 */
	public void head(HttpHead httphead) throws IOException {
		ResponseHandler<Void> responseHandler = new ResponseHandler<Void>() {
			@Override
			public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
				StatusLine status = response.getStatusLine();
				int statusCode = status.getStatusCode();
				if (statusCode >= 200 && statusCode < 300)
					return null;
				else
					throw new HttpResponseException(statusCode, status.getReasonPhrase());
			}
		};
		head(httphead, responseHandler);
	}

	/**
	 * Send the specified HTTP HEAD request and invoke the supplied handler to process the response as the indicated type.
	 */
	public <T> void head(HttpHead httphead, ResponseHandler<? extends T> responseHandler) throws IOException {
		execute(httphead, responseHandler);
	}

	/**
	 * Make an HTTP GET request (using the provided headers) and return the response as a String.
	 *
	 * @param url     Will be converted to a URI by <code>resolveUrl</code>
	 * @param reqHdrs May be null
	 */
	public String get(String url, List<Header> reqHdrs) throws IOException {
		HttpGet httpget = new HttpGet(resolveUrl(url));
		if (reqHdrs != null)
			for (Header hdr : reqHdrs)
				httpget.setHeader(hdr);
		return get(httpget);
	}

	/**
	 * Make an HTTP GET request (using the provided headers) and return the response as the indicated type.
	 *
	 * @param url     Will be converted to a URI by <code>resolveUrl</code>
	 * @param reqHdrs May be null
	 */
	public <T> T get(String url, List<Header> reqHdrs, ResponseHandler<? extends T> responseHandler) throws IOException {
		HttpGet httpget = new HttpGet(resolveUrl(url));
		if (reqHdrs != null)
			for (Header hdr : reqHdrs)
				httpget.setHeader(hdr);
		return get(httpget, responseHandler);
	}

	/**
	 * Send the specified HTTP GET request and return the response as a String.
	 */
	public String get(HttpGet httpget) throws IOException {
		return get(httpget, new StringResponseHandler());
	}

	/**
	 * Send the specified HTTP GET request and invoke the supplied handler to process the response as the indicated type.
	 */
	public <T> T get(HttpGet httpget, ResponseHandler<? extends T> responseHandler) throws IOException {
		return execute(httpget, responseHandler);
	}

	/**
	 * Make an HTTP POST request (using the provided headers and NaveValue pairs) and return the response as a String.
	 *
	 * @param url     Will be converted to a URI by <code>resolveUrl</code>
	 * @param nvps    The key value pairs to post (will be sent as UrlEncodedForm).
	 * @param reqHdrs May be null
	 */
	public String post(String url, List<NameValuePair> nvps, List<Header> reqHdrs) throws IOException {
		return post(url, new UrlEncodedFormEntity(nvps), reqHdrs);
	}

	/**
	 * Make an HTTP POST request (using the provided headers and entity body) and return the response as a String.
	 *
	 * @param url     Will be converted to a URI by <code>resolveUrl</code>
	 * @param entity  Any subtype of Apache http-client Entity.
	 * @param reqHdrs May be null
	 */
	public String post(String url, HttpEntity entity, List<Header> reqHdrs) throws IOException {
		HttpPost httpPost = new HttpPost(resolveUrl(url));
		if (reqHdrs != null)
			for (Header hdr : reqHdrs)
				httpPost.setHeader(hdr);
		httpPost.setEntity(entity);
		return post(httpPost);
	}

	/**
	 * Make an HTTP POST request (using the provided headers and entity body) and  invoke the supplied handler to process the response as the indicated type.
	 *
	 * @param url             Will be converted to a URI by <code>resolveUrl</code>
	 * @param entity          Any subtype of Apache http-client Entity.
	 * @param reqHdrs         May be null
	 * @param responseHandler The callback to handle the response from the remote endpoint.
	 */
	public <T> T post(String url, HttpEntity entity, List<Header> reqHdrs, ResponseHandler<? extends T> responseHandler) throws IOException {
		HttpPost httpPost = new HttpPost(resolveUrl(url));
		if (reqHdrs != null)
			for (Header hdr : reqHdrs)
				httpPost.setHeader(hdr);
		httpPost.setEntity(entity);
		return post(httpPost, responseHandler);
	}

	/**
	 * Send the specified HTTP POST request and return the response as a String.
	 */
	public String post(HttpPost httpPost) throws IOException {
		return post(httpPost, new StringResponseHandler());
	}

	/**
	 * Send the specified HTTP POST request and invoke the supplied handler to process the response as the indicated type.
	 */
	public <T> T post(HttpPost httpPost, ResponseHandler<? extends T> responseHandler) throws IOException {
		return execute(httpPost, responseHandler);
	}

	/**
	 * All Http requests eventually route through this method, which actually invokes the Apache http-client.
	 */
	protected <T> T execute(HttpRequestBase req, ResponseHandler<? extends T> responseHandler) throws IOException {
		return httpclient.execute(req, responseHandler);
	}

	/**
	 * It's all up to you.  Just passes the request to our encapsulated httpclient, and return it's response.
	 */
	public CloseableHttpResponse execute(HttpRequestBase req) throws IOException {
		return httpclient.execute(req);
	}

	public CloseableHttpResponse execute(HttpRequestBase req, HttpContext context) throws IOException {
		return httpclient.execute(req, context);
	}


	/**
	 * Make an HTTP PUT request (using the provided headers and NaveValue pairs) and return the response as a String.
	 *
	 * @param url     Will be converted to a URI by <code>resolveUrl</code>
	 * @param nvps    The key value pairs to put (will be sent as UrlEncodedForm).
	 * @param reqHdrs May be null
	 */
	public String put(String url, List<NameValuePair> nvps, List<Header> reqHdrs) throws IOException {
		return put(url, new UrlEncodedFormEntity(nvps), reqHdrs);
	}

	/**
	 * Make an HTTP PUT request (using the provided headers and entity body) and return the response as a String.
	 *
	 * @param url     Will be converted to a URI by <code>resolveUrl</code>
	 * @param entity  Any subtype of Apache http-client Entity.
	 * @param reqHdrs May be null
	 */
	public String put(String url, HttpEntity entity, List<Header> reqHdrs) throws IOException {
		HttpPut httpPut = new HttpPut(resolveUrl(url));
		if (reqHdrs != null)
			for (Header hdr : reqHdrs)
				httpPut.setHeader(hdr);
		httpPut.setEntity(entity);
		return put(httpPut);
	}

	/**
	 * Make an HTTP PUT request (using the provided headers and entity body) and  invoke the supplied handler to process the response as the indicated type.
	 *
	 * @param url             Will be converted to a URI by <code>resolveUrl</code>
	 * @param entity          Any subtype of Apache http-client Entity.
	 * @param reqHdrs         May be null
	 * @param responseHandler The callback to handle the response from the remote endpoint.
	 */
	public <T> T put(String url, HttpEntity entity, List<Header> reqHdrs, ResponseHandler<? extends T> responseHandler) throws IOException {
		HttpPut httpPut = new HttpPut(resolveUrl(url));
		if (reqHdrs != null)
			for (Header hdr : reqHdrs)
				httpPut.setHeader(hdr);
		httpPut.setEntity(entity);
		return put(httpPut, responseHandler);
	}

	/**
	 * Send the specified HTTP PUT request and return the response as a String.
	 */
	public String put(HttpPut httpPut) throws IOException {
		return put(httpPut, new StringResponseHandler());
	}

	/**
	 * Send the specified HTTP PUT request and invoke the supplied handler to process the response as the indicated type.
	 */
	public <T> T put(HttpPut httpPut, ResponseHandler<? extends T> responseHandler) throws IOException {
		return execute(httpPut, responseHandler);
	}

	/**
	 * Helper to evict idle connections out of a connection pool.
	 */
	public static class IdleConnectionEvictor extends Thread {
		public IdleConnectionEvictor(HttpClientConnectionManager connMgr) {
			super();
			this.connMgr = connMgr;
		}
		private volatile boolean shutdown;
		private final HttpClientConnectionManager connMgr;

		@Override
		public void run() {
			try {
				while (!shutdown) {
					synchronized (this) {
						wait(5000);
						// Close expired connections
						connMgr.closeExpiredConnections();
						// Optionally, close connections that have been idle longer than 5 sec
						connMgr.closeIdleConnections(5, TimeUnit.SECONDS);
					}
				}
			} catch (InterruptedException ex) {
				// terminate
			}
		}

		public void shutdown() {
			shutdown = true;
			synchronized (this) {
				notifyAll();
			}
		}
	}
}
