package com.bytelightning.oss.lib.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	
	/**
	 * Convenient lookup for the Local time zone.
	 */
	public static final TimeZone LOCAL_TZ = TimeZone.getDefault();

	/**
	 * HTTP spec says dates must be in GMT time, since any date we create will be in local time, convert the date to be offset back to GMT.
	 * @param date
	 */
	public static Date AdjustLocalDateToGMT(Date date) {
		Date ret = new Date(date.getTime() - LOCAL_TZ.getRawOffset());
		// if we are now in DST, back off by the delta. Note that we are checking the GMT date, this is the KEY.
		if (LOCAL_TZ.inDaylightTime(ret)) {
			Date dstDate = new Date(ret.getTime() - LOCAL_TZ.getDSTSavings());
			// check to make sure we have not crossed back into standard time
			// this happens when we are on the cusp of DST (7pm the day before the change for PDT)
			if (LOCAL_TZ.inDaylightTime(dstDate))
				ret = dstDate;
		}
		return ret;
	}

	/**
	 * HTTP spec says dates must be in GMT time, since any date we receive will be in GMT time, convert the date to be offset back to local time.
	 * @param date
	 */
	public static Date AdjustGMTToLocalDate(Date date) {
		Date ret = new Date(date.getTime() + LOCAL_TZ.getRawOffset());
		// if we are now in DST, back off by the delta. Note that we are checking the GMT date, this is the KEY.
		if (LOCAL_TZ.inDaylightTime(ret)) {
			Date dstDate = new Date(ret.getTime() + LOCAL_TZ.getDSTSavings());
			// check to make sure we have not crossed back into standard time
			// this happens when we are on the cusp of DST (7pm the day before the change for PDT)
			if (LOCAL_TZ.inDaylightTime(dstDate))
				ret = dstDate;
		}
		return ret;
	}
}
