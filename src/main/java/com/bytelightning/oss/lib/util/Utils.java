package com.bytelightning.oss.lib.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * A Utils class in a util package :-)  Truly random (but useful) little routines.
 *
 */
public class Utils {
	public String bytes2hex(byte[] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString();
	}

	public byte[] hex2bytes(String hex) {
		int len = hex.length();
		assert (len & 0x01) == 0 : "Hex string must contain even number of chars";
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
		return data;
	}

	public synchronized final String md5(String txt) {
		String retVal = null;
		try {
			if (md5 == null) {
				try {
					md5 = MessageDigest.getInstance("MD5");
				}
				catch (NoSuchAlgorithmException e) {
					e.printStackTrace(System.err); // Don't bother with a log, because for md5, this should *never* happen
				}
			}
			md5.reset();
			md5.update(txt.getBytes("UTF-8"));
			retVal = bytes2hex(md5.digest());
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err); // Don't bother with a log, because for utf-8, this should never happen
		}
		return retVal;
	}
	private MessageDigest md5;


	/**
	 * Convert an array of Strings (such as returned by Configuration.getStrings() ) to an array of <code>Pattern</code>s.
	 */
	public static Pattern[] MakePatternArray(String[] pats) {
		if ((pats == null) || (pats.length == 0))
			return null;
		if ((pats.length == 1) && ((pats[0] == null) || (pats[0].trim().length() == 0)))
			return null;
		Pattern[] retVal = new Pattern[pats.length];
		for (int i = 0; i < retVal.length; i++)
			retVal[i] = Pattern.compile(pats[i], Pattern.CASE_INSENSITIVE);
		return retVal;
	}
	/**
	 * Convert an array of URL's (such as returned by Configuration.getStrings() ) to an array of <code>URL</code>s.
	 */
	public static URL[] MakeURLArray(String[] urls) throws MalformedURLException {
		if ((urls == null) || (urls.length == 0))
			return null;
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < urls.length; i++) {
			String s = urls[i] == null ? "" : urls[i].trim();
			if (s.length() > 0)
				list.add(s);
		}
		if (list.size() == 0)
			return null;
		URL[] retVal = new URL[list.size()];
		for (int i = 0; i < retVal.length; i++)
			retVal[i] = new URL(list.get(i));
		return retVal;
	}
	
	public static URI MakeURI(Class<?> classPath, String s) {
		try {
			if ((classPath != null) && s.startsWith("classpath://")) {
				URL tmp = new URL(s.replace("classpath://", "file://")); // Hack to help us extract the userinfo
				s = s.substring(12);
				String userInfo = tmp.getUserInfo();
				if ((userInfo != null) && (userInfo.length() > 0))
					s = s.substring(userInfo.length() + 1);
				// Despite the FindBugs warning, this is intentional.
				// This is an abstract class and we want to load the properties from the same path as whatever instance subclass we are.
				URL rsrc = classPath.getResource(s);
				if ((userInfo != null) && (userInfo.length() > 0)) {
					String src = rsrc.toExternalForm();
					String target = rsrc.getProtocol() + ":///";
					if (!src.startsWith(target))
						target = rsrc.getProtocol() + "://";
					if (!src.startsWith(target))
						target = rsrc.getProtocol() + ":";
					src = src.replace(target, rsrc.getProtocol() + "://" + userInfo + "@");
					return new URI(src);
				}
				else
					return rsrc.toURI();
			}
			else if (s.startsWith("file://")) {
				int atPos = s.indexOf('@');
				if (atPos > 0) {
					// will look something like file://AcademicPlan4:Test1234@/src/foo
					File f = new File(s.substring(atPos + 1));
					s = s.substring(0, atPos+1) + f.getAbsoluteFile().toURI().getPath();
					return new URI(s);	
				}
				else {
					File f = new File(s.substring(7));
					return f.toURI();
				}			
			}
			else
				return new URI(s);
		}
		catch (Throwable e) {
			// FindBugs is wrong! Many exceptions can happen in the above code.
			System.err.println("Invalid URI: " + s);
			return null;
		}
	}
	
	/**
	 * Convert an array of URI's (such as returned by Configuration.getStrings() ) to an array of <code>URI</code>s.
	 * @throws URISyntaxException 
	 */
	public static URI[] MakeURIArray(Class<?> classPath, String[] uris) throws URISyntaxException {
		if ((uris == null) || (uris.length == 0))
			return null;
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < uris.length; i++) {
			String s = uris[i] == null ? "" : uris[i].trim();
			if (s.length() > 0)
				list.add(s);
		}
		if (list.size() == 0)
			return null;
		URI[] retVal = new URI[list.size()];
		int idx = 0;
		for (String s : list) {
			URI u = MakeURI(classPath, s);
			if (u == null)
				return null;
			retVal[idx++] = u;
		}
		return retVal;
	}
}
