package com.bytelightning.oss.lib.http;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.impl.nio.reactor.IOReactorConfig.Builder;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.security.auth.Subject;
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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 * This is a global service based on the Apache http-async-client library.
 * It may be Injected anywhere you need it, and is thread safe, concurrent, and secure.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class HttpAsyncClient {
	protected static final Logger logger = LoggerFactory.getLogger(HttpAsyncClient.class.getPackage().getName());

	/**
	 * Primary constructor
	 */
	public HttpAsyncClient() {
	}
	private CloseableHttpAsyncClient httpclient;
	private RequestConfig defaultRequestConfig;
	private IdleConnectionEvictor connEvictor;


	/**
	 * One of these setup methods MUST be called after construction and before use of this object.
	 */
	public void setup(boolean includeHttps, List<Header> defaultHdrs) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOReactorException {
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
	public void setup(KeyStore trustStore, List<Header> defaultHdrs) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOReactorException {
		// Trust own CA and all self-signed certs
		SSLContext sslcontext = trustStore == null ? SSLContexts.createSystemDefault() : SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();
		setup(sslcontext, defaultHdrs);
	}

	/**
	 * One of these setup methods MUST be called after construction and before use of this object.
	 */
	@SuppressWarnings("ConstantConditions")
	public void setup(SSLContext sslcontext, List<Header> defaultHdrs) throws IOReactorException {
		// Configure default headers to be included in all requests
		if (defaultHdrs == null)
			defaultHdrs = new ArrayList<Header>();
		HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom().setDefaultHeaders(defaultHdrs);
		Registry<SchemeIOSessionStrategy> socketFactory = createSocketFactoryBuilder(clientBuilder, sslcontext).build();

		ConnectingIOReactor ioReactor = createConnectingIOReactor();

		NHttpClientConnectionManager connManager = createConnectionManager(ioReactor, socketFactory);
		clientBuilder.setConnectionManager(connManager);

		defaultRequestConfig = createDefaultRequestBuilder().build();
		clientBuilder.setDefaultRequestConfig(defaultRequestConfig);

		CookieStore cookieStore = createCookieStore();
		if (cookieStore != null)
			clientBuilder.setDefaultCookieStore(cookieStore);

		connEvictor = new IdleConnectionEvictor(connManager);

		httpclient = buildClient(clientBuilder);
		if (httpclient == null)
			logger.error("Failed to build the HttpClient");

		httpclient.start();

		connEvictor.start();
	}

	protected RegistryBuilder<SchemeIOSessionStrategy> createSocketFactoryBuilder(HttpAsyncClientBuilder clientBuilder, SSLContext sslcontext) {
		RegistryBuilder<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder.<SchemeIOSessionStrategy>create().register("http", NoopIOSessionStrategy.INSTANCE);
		if (sslcontext != null) {
			// Allow TLSv1 protocol only
			SSLIOSessionStrategy sslsf = new SSLIOSessionStrategy(sslcontext, new String[]{"TLSv1"}, null, SSLIOSessionStrategy.getDefaultHostnameVerifier());
			sessionStrategyRegistry.register("https", SSLIOSessionStrategy.getDefaultStrategy());
			clientBuilder.setSSLStrategy(sslsf);
		}
		return sessionStrategyRegistry;
	}
	
	protected IOReactorConfig.Builder buildIOReactorConfig() {
		Builder ioReactorConfig = IOReactorConfig.custom()
				.setIoThreadCount(Runtime.getRuntime().availableProcessors())
				.setConnectTimeout(30000)
				.setSoTimeout(30000);
		return ioReactorConfig;
	}
	
	protected ConnectingIOReactor createConnectingIOReactor() throws IOReactorException {
		Builder ioReactorConfig = IOReactorConfig.custom()
				.setIoThreadCount(Runtime.getRuntime().availableProcessors())
				.setConnectTimeout(30000)
				.setSoTimeout(30000);
		return new DefaultConnectingIOReactor(buildIOReactorConfig().build());
	}

	protected ConnectionConfig.Builder buildConnectionConfig() {
		ConnectionConfig.Builder connectionConfig = ConnectionConfig.custom()
				.setMalformedInputAction(CodingErrorAction.REPORT) // Defines the action to perform if the input byte sequence is not legal for this charset
				.setUnmappableInputAction(CodingErrorAction.REPORT) // Defines the action to perform if the input byte sequence is legal but cannot be mapped to a valid Unicode character
				.setCharset(Consts.UTF_8);
		return connectionConfig;
	}
	

	protected NHttpClientConnectionManager createConnectionManager(ConnectingIOReactor ioReactor, Registry<SchemeIOSessionStrategy> socketFactory) {
		PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor, socketFactory);
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

	protected CloseableHttpAsyncClient buildClient(HttpAsyncClientBuilder clientBuilder) {
		return clientBuilder.build();
	}

	/**
	 * Clean up the client when it is disposed.
	 */
	public void teardown() throws IOException {
		if (connEvictor != null)
			connEvictor.shutdown();
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
	 * Ensure the URL can be created.
	 */
	@SuppressWarnings("WeakerAccess")
	public URI resolveUrl(String url) {
		URI uri = URI.create(url);
		return uri;
	}

	/**
	 * Make an HTTP GET request (using the provided headers) and return the response as a String.
	 *
	 * @param url     Will be converted to a URI by <code>resolveUrl</code>
	 * @param reqHdrs May be null
	 */
	public Future<String> get(String url, List<Header> reqHdrs) {
		return get(url, reqHdrs, (FutureCallback<String>) null);
	}
	public Future<String> get(String url, List<Header> reqHdrs, FutureCallback<String> callback) {
		HttpGet httpget = new HttpGet(resolveUrl(url));
		if (reqHdrs != null)
			for (Header hdr : reqHdrs)
				httpget.setHeader(hdr);
		return get(httpget, callback);
	}

	/**
	 * Make an HTTP GET request (using the provided headers) and return the response as the indicated type.
	 *
	 * @param url     Will be converted to a URI by <code>resolveUrl</code>
	 * @param reqHdrs May be null
	 */
	public <T> Future<T> get(String url, List<Header> reqHdrs, HttpAsyncResponseConsumer<T> responseConsumer) {
		return get(url, reqHdrs, responseConsumer, null);
	}
	public <T> Future<T> get(String url, List<Header> reqHdrs, HttpAsyncResponseConsumer<T> responseConsumer, FutureCallback<T> callback) {
		HttpGet httpget = new HttpGet(resolveUrl(url));
		if (reqHdrs != null)
			for (Header hdr : reqHdrs)
				httpget.setHeader(hdr);
		return get(httpget, responseConsumer, callback);
	}

	/**
	 * Send the specified HTTP GET request and return the response as a String.
	 */
	public Future<String> get(HttpGet httpget) {
		return get(httpget, (FutureCallback<String>) null);
	}
	public Future<String> get(HttpGet httpget, FutureCallback<String> callback) {
		return get(httpget, new StringResponseConsumer(), callback);
	}

	/**
	 * Send the specified HTTP GET request and invoke the supplied handler to process the response as the indicated type.
	 */
	public <T> Future<T> get(HttpGet httpget, HttpAsyncResponseConsumer<T> responseConsumer) {
		return get(httpget, responseConsumer, null);
	}
	public <T> Future<T> get(HttpGet httpget, HttpAsyncResponseConsumer<T> responseConsumer, FutureCallback<T> callback) {
		return execute(HttpAsyncMethods.create(httpget), responseConsumer, callback, null);
	}


	/**
	 * Make an HTTP HEAD request (using the provided headers) and return the response as a String.
	 *
	 * @param url     Will be converted to a URI by <code>resolveUrl</code>
	 * @param reqHdrs May be null
	 */
	public Future<HttpResponse> head(String url, List<Header> reqHdrs) {
		return head(url, reqHdrs, (FutureCallback<HttpResponse>) null);
	}
	public Future<HttpResponse> head(String url, List<Header> reqHdrs, FutureCallback<HttpResponse> callback) {
		HttpHead httpget = new HttpHead(resolveUrl(url));
		if (reqHdrs != null)
			for (Header hdr : reqHdrs)
				httpget.setHeader(hdr);
		return head(httpget, callback);
	}

	/**
	 * Make an HTTP HEAD request (using the provided headers) and return the response as the indicated type.
	 *
	 * @param url     Will be converted to a URI by <code>resolveUrl</code>
	 * @param reqHdrs May be null
	 */
	public <T> Future<T> head(String url, List<Header> reqHdrs, HttpAsyncResponseConsumer<T> responseConsumer) {
		return head(url, reqHdrs, responseConsumer, null);
	}
	public <T> Future<T> head(String url, List<Header> reqHdrs, HttpAsyncResponseConsumer<T> responseConsumer, FutureCallback<T> callback) {
		HttpHead httphead = new HttpHead(resolveUrl(url));
		if (reqHdrs != null)
			for (Header hdr : reqHdrs)
				httphead.setHeader(hdr);
		return head(httphead, responseConsumer, callback);
	}

	/**
	 * Send the specified HTTP HEAD request and return the response as a String.
	 */
	public Future<HttpResponse> head(HttpHead httphead) {
		return head(httphead, (FutureCallback<HttpResponse>) null);
	}
	public Future<HttpResponse> head(HttpHead httphead, FutureCallback<HttpResponse> callback) {
		return head(httphead, new BasicAsyncResponseConsumer(), callback);
	}

	/**
	 * Send the specified HTTP HEAD request and invoke the supplied handler to process the response as the indicated type.
	 */
	public <T> Future<T> head(HttpHead httphead, HttpAsyncResponseConsumer<T> responseConsumer) {
		return head(httphead, responseConsumer, null);
	}
	public <T> Future<T> head(HttpHead httphead, HttpAsyncResponseConsumer<T> responseConsumer, FutureCallback<T> callback) {
		return execute(HttpAsyncMethods.create(httphead), responseConsumer, callback, null);
	}

	/**
	 * It's all up to you.  Just passes the request to our encapsulated httpclient, and return it's response.
	 */
	public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, FutureCallback<T> callback, HttpContext context) {
		return httpclient.execute(requestProducer, responseConsumer, context, callback);
	}

	/**
	 * Helper to evict idle connections out of a connection pool.
	 */
	public static class IdleConnectionEvictor extends Thread {
		public IdleConnectionEvictor(NHttpClientConnectionManager connMgr) {
			super();
			this.connMgr = connMgr;
		}
		private volatile boolean shutdown;
		private final NHttpClientConnectionManager connMgr;

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
