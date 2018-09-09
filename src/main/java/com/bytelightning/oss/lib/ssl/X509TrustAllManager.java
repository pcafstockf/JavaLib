package com.bytelightning.oss.lib.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Simple X509TrustManager that is willing to trust anybody and everybody
 * Not a good idea for a production system.
 */
final class X509TrustAllManager implements X509TrustManager {
	/**
	 * {@inheritDoc}
	 */
	public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	}

	/**
	 * {@inheritDoc}
	 */
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}
