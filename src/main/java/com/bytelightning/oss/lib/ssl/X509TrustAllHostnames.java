package com.bytelightning.oss.lib.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Simple X509HostnameVerifier that allows any host to represent any certificate.
 * Not a good idea for a production system.
 */
public class X509TrustAllHostnames implements HostnameVerifier {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean verify(String string, SSLSession ssls) {
		return true;
	}
}
