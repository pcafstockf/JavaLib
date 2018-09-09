package com.bytelightning.oss.lib.http;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpHandler;

/**
 * Utility wrapper around the built-in sun http server
 *
 */
@SuppressWarnings("restriction")
public class SunBasedHttpServer implements Closeable {
	public SunBasedHttpServer() {
	}
	private HttpServer httpServer;
	private ExecutorService httpThreadPool;

	/**
	 * Subclass extension point.  Create a com.sun.net.httpserver (HttpServer or HttpsServer) at the indicated local address.
	 * @param addr The address the server should listen on.
	 * @param sslCtx If non-null, create an https server. Otherwise a plain http server will do.
	 */
	protected HttpServer createServer(InetSocketAddress addr, final SSLContext sslCtx) throws IOException, NoSuchAlgorithmException {
		if (sslCtx != null) {
			HttpsServer retVal = HttpsServer.create(addr, 100);
			retVal.setHttpsConfigurator(new HttpsConfigurator(sslCtx) {
				public void configure(HttpsParameters params) {
					SSLParameters sslparams = getSSLContext().getDefaultSSLParameters();
					params.setSSLParameters(sslparams);
				}
			});
			return retVal;
		}
		else
			return HttpServer.create(addr, 100);
	}
	
	/**
	 * Subclass extension point, create a thread pool to handle the incoming http(s) requests.
	 */
	protected ExecutorService createThreadPool(int threadCount) {
		ThreadFactory factory = new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "HTTP Threads");
				t.setDaemon(true);
				return t;
			}
		};
		if (threadCount <= 0)
			return Executors.newCachedThreadPool(factory);
		else
			return Executors.newFixedThreadPool(threadCount);		
	}

	/**
	 * Bind the server to the specified location and have it start listening for requests.
	 * NOTE: This method is quite long and somewhat complex as it is *the* bridge/adapter between the sun http server and the Tools4J compoenents.
	 * 
	 * @param addrStr	 Interface name to bind to.
	 * @param port	Port to listen on.
	 * @param ssoCtx	If non-null, create an https server. Otherwise a plain http server will do.
	 */
	public void init(InetSocketAddress addr, int threadCount, SSLContext sslContext) throws IOException, GeneralSecurityException {
		httpServer = createServer(addr, sslContext);
		httpThreadPool = createThreadPool(threadCount);
		httpServer.setExecutor(httpThreadPool);
	}
	
	/**
	 * @see http://docs.oracle.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html#createContext(java.lang.String,%20com.sun.net.httpserver.HttpHandler)
	 */
	public void addContext(String ctx, HttpHandler handler) {
		httpServer.createContext(ctx, handler);
	}

	/**
	 * @see http://docs.oracle.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html#start()
	 */
	public void start() {
		// Set the ability to handle multiple requests at once
		httpServer.start();
	}
	
	/**
	 * Return the URL from which this server may be accessed (if bound to 0.0.0.0 for instance, this url will contain an actual hostname).
	 */
	public URL getServerUrl() {
		InetSocketAddress sktAddr = httpServer.getAddress();
		if (sktAddr.getAddress().isAnyLocalAddress()) {
			String hostName = HttpUtils.GetHostName();
			sktAddr = new InetSocketAddress(hostName, sktAddr.getPort());
		}
		String prefix;
		String suffix = ":" + sktAddr.getPort();
		if (httpServer instanceof HttpsServer) {
			prefix = "https://";
			if (sktAddr.getPort() == 443)
				suffix = "";
		}
		else {
			prefix = "http://";
			if (sktAddr.getPort() == 80)
				suffix = "";
		}
		String hostStr = null; //TODO: When we upgrade to jdk 1.7 use sktAddr.getHostString() and drop the code below.
		String tmp = sktAddr.toString();
		int slashPos = tmp.indexOf('/');
		if (slashPos > 0)
			hostStr = tmp.substring(0, slashPos);
		else
			hostStr = tmp.substring(slashPos + 1);
		String urlStr = prefix + hostStr + suffix + "/";
		URL retVal;
		try {
			retVal = new URL(urlStr);
		} catch (MalformedURLException e) {
			e.printStackTrace(System.err);	// Should actually be impossible.
			return null;
		}
		return retVal;
	}

	/**
	 * @see http://docs.oracle.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html#stop(int)
	 */
	@Override
	public void close() throws IOException {
		if (httpServer != null) {
			httpServer.stop(2);
			httpThreadPool.shutdownNow();
			httpServer = null;
			httpThreadPool = null;
		}
	}
}
