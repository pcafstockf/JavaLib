package com.bytelightning.oss.lib.config;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationMap;

public class Utils {

	/**
	 * Convert a sub Configuration of the form foo.keyA.1=pat1, foo.keyA.2=pat2, foo.keyB.1=pat3 to a Map<Pattern,String>
	 * This method is used by creating a subset such as config.subset("foo");
	 */
	public static Map<Pattern, String> MakePatternMap(org.apache.commons.configuration.Configuration subset) {
		Map<Pattern, String> retVal = null;
		if (subset != null) {
			ConfigurationMap nreg = new ConfigurationMap(subset);
			if (nreg.size() > 0) {
				retVal = new HashMap<Pattern, String>();
				for (Entry<Object, Object> s : nreg.entrySet()) {
					String name = (String) s.getKey();
					int dotPos = name.indexOf('.');
					if (dotPos > 0)
						name = name.substring(0, dotPos);
					String expr = (String) s.getValue();
					retVal.put(Pattern.compile(expr, Pattern.CASE_INSENSITIVE), name);
				}
			}
		}
		return retVal;
	}

	/**
	 * Convert a sub Configuration of the form foo.keyA.1=pat1, foo.keyA.2=pat2, foo.keyB.1=pat3 to a Map<String,URL>
	 * This method is used by creating a subset such as config.subset("foo");
	 */
	public static Map<String, URL> MakeURLMap(org.apache.commons.configuration.Configuration subset) throws MalformedURLException {
		Map<String, URL> retVal = null;
		if (subset != null) {
			ConfigurationMap nreg = new ConfigurationMap(subset);
			if (nreg.size() > 0) {
				retVal = new HashMap<String, URL>();
				for (Entry<Object, Object> s : nreg.entrySet()) {
					String name = (String) s.getKey();
					int dotPos = name.indexOf('.');
					if (dotPos > 0)
						name = name.substring(0, dotPos);
					String url = (String) s.getValue();
					retVal.put(name, new URL(url));
				}
			}
		}
		return retVal;
	}

	/**
	 * Convert a sub Configuration of the form foo.keyA.1=pat1, foo.keyA.2=pat2, foo.keyB.1=pat3 to a Map<String,URI>
	 * This method is used by creating a subset such as config.subset("foo");
	 */
	public static Map<String, URI> MakeURIMap(Class<?> classPath, org.apache.commons.configuration.Configuration subset) throws MalformedURLException {
		Map<String, URI> retVal = null;
		if (subset != null) {
			ConfigurationMap nreg = new ConfigurationMap(subset);
			if (nreg.size() > 0) {
				retVal = new HashMap<String, URI>();
				for (Entry<Object, Object> s : nreg.entrySet()) {
					String name = (String) s.getKey();
					int dotPos = name.indexOf('.');
					if (dotPos > 0)
						name = name.substring(0, dotPos);
					String uriStr = (String) s.getValue();
					URI uri = com.bytelightning.oss.lib.util.Utils.MakeURI(classPath, uriStr);
					if (uri == null)
						return null;
					retVal.put(name, uri);
				}
			}
		}
		return retVal;
	}
}
