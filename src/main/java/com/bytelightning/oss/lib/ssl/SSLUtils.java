package com.bytelightning.oss.lib.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Misc routines useful in an SSL context.
 */
public class SSLUtils {
	/**
	 * Utility routine to create an SSLContext for a secure server.
	 */
	public static SSLContext CreateSSLContext(InputStream keystore, char[] storepass, char[] keypass) throws GeneralSecurityException, IOException {
		SSLContext retVal = SSLContext.getInstance("TLS");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		KeyStore ks;
		try {
			ks = KeyStore.getInstance("JKS");
			ks.load(keystore, storepass);
		}
		finally {
			keystore.close();
		}
		kmf.init(ks, keypass);
		X509TrustManager tm = new X509TrustAllManager();
		retVal.init(kmf.getKeyManagers(), new TrustManager[] { tm }, null);
		return retVal;
	}
}
