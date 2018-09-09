package com.bytelightning.oss.lib.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
}
