package com.bytelightning.oss.lib.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.bytelightning.oss.lib.ssl.SSLUtils;

/**
 * Misc routines useful in an Http context.
 */
public class HttpUtils extends SSLUtils {
	/**
	 * This routine courtesy of:  http://stackoverflow.com/questions/7348711/recommended-way-to-get-hostname-in-java
	 */
	public static String GetHostName() {
		// try InetAddress.LocalHost first; NOTE -- InetAddress.getLocalHost().getHostName() will not work in certain environments.
		try {
			String result = InetAddress.getLocalHost().getHostName();
			if ((result != null) && (result.trim().length() > 0))
				return result.trim();
		}
		catch (UnknownHostException e) {
			// failed; try alternate means.
		}
		// try environment properties.
		String host = System.getenv("COMPUTERNAME");
		if (host != null)
			return host;
		host = System.getenv("HOSTNAME");
		if (host != null)
			return host;

		return "localhost";
	}

	/**
	 * Format used for HTTP date headers.
	 */
	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

	/**
	 * Convenient lookup for the GMT time zone.
	 */
	public static final TimeZone GMT_TZ = TimeZone.getTimeZone("GMT");

	/**
	 * Get a new date formatter compatible with HTTP headers protocol.
	 * SPECIFICALLY... This means:
	 * 	The parser will parse GMT and return local time.
	 * 	The formatter will take a local time and output a GMT string.
	 */
	public static DateFormat GetHTTPDateFormater() {
		SimpleDateFormat f = new SimpleDateFormat(HTTP_DATE_FORMAT);
		f.setTimeZone(GMT_TZ);
		return f;
	}
}
