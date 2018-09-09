package com.bytelightning.oss.lib.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

/**
 * Java utility class met to be similar to the Javascript JSON object api
 *
 */
public class JSON {
	public static interface Replacer {
		Object replace(Object target, String key, Object value);
	}
	
	private JSON() {
	}

	public static Object parse(String txt) throws IOException {
		return parse(new StringReader(txt));
	}
	public static Object parse(Reader in) throws IOException {
		JsonParser parser = jsonParsers.get();
		if (parser == null) {
			parser = new JsonParser(false, false, false);
			jsonParsers.set(parser);
		}
		return parser.parse(in);
	}
	private static final ThreadLocal<JsonParser> jsonParsers = new ThreadLocal<JsonParser>();

	public static Object parse(String txt, JsonSaxListener l) throws IOException {
		return parse(new StringReader(txt), l);
	}
	public static Object parse(Reader in, JsonSaxListener l) throws IOException {
		JsonSaxer saxer = jsonSaxers.get();
		if (saxer == null) {
			saxer = new JsonSaxer();
			jsonSaxers.set(saxer);
		}
		return saxer.parse(in, l);
	}
	private static final ThreadLocal<JsonSaxer> jsonSaxers = new ThreadLocal<JsonSaxer>();

	public static String stringify(Object obj) {
		return stringify(obj, null, null);
	}
	@SuppressWarnings("unchecked")
	public static String stringify(Object obj, Object replacer, Object space) {
		String spacing = null;
		if (space instanceof Number) {
			if (((Number)space).doubleValue() >= 1) {
				spacing = "";
				int min = Math.min(10, ((Number)space).intValue());
				for (int i=0; i<min; i++)
					spacing += " ";
			}
		}
		else if (space instanceof String) {
			spacing = ((String)space).substring(0, Math.min(10, ((String)space).length()));
			if (spacing.length() == 0)
				spacing = null;
		}
		
		if (replacer != null && ((replacer instanceof Collection) || replacer.getClass().isArray())) {
			final Collection<Object> c;
			if (replacer.getClass().isArray()) {
				c = new ArrayList<Object>(Arrays.asList(((Object[])replacer)));
				c.add(null);
			}
			else
				c = (Collection<Object>)replacer;
			replacer = new Replacer() {
				@Override
				public Object replace(Object target, String key, Object value) {
					if (c.contains(key))
						return value;
					return this;
				}
			};
		}
		else if (! (replacer instanceof Replacer))
			replacer = null;
		
		if (replacer == null) {
			replacer = new Replacer() {
				@Override
				public Object replace(Object target, String key, Object value) {
					return value;
				}
			};
		}
		
		StringBuilder sb = new StringBuilder(2048);
		obj = ((Replacer)replacer).replace(obj, null, obj);
		if (obj != replacer) {
			Set<Object> cyclicDetector = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
			appendObject(sb, obj, (Replacer)replacer, "", spacing, cyclicDetector);
		}
		return sb.toString().trim();
	}

	@SuppressWarnings("unchecked")
	private static void appendObject(final StringBuilder sb, final Object obj, final Replacer replacer, final String indent, final String space, Set<Object> cyclicDetector) {
		if (obj == null)
			sb.append("null");
		else if (obj instanceof String) {
			sb.append('"');
			sb.append(JsonUtils.EscapeJsonString((String) obj));
			sb.append('"');
		}
		else if (obj instanceof Date) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			sb.append('"');
			sb.append(sdf.format((Date)obj));
			sb.append('"');
		}
		else if (obj instanceof Number) {
			final int i = ((Number)obj).intValue();
			if (obj instanceof Double || obj instanceof Float) {
				final double d = ((Number) obj).doubleValue();
				if (Double.isInfinite(d))
					sb.append("null");
				else if (Double.isNaN(d))
					sb.append("null");
				else if (d == i)
					sb.append(i);
				else
					sb.append(d);
			}
			else
				sb.append(i);
		}
		else if (obj.getClass().isArray()) {
			if (cyclicDetector.contains(obj))
				throw new RuntimeException("Cyclic TypeError");
			String i = space == null ? "" : indent + space;
			int count = 0;
			sb.append('[');
			cyclicDetector.add(obj);
			for (Object o : (Object[]) obj) {
				o = replacer.replace(obj, "" + count, o);
				if (o != replacer) {
					if (count > 0)
						sb.append(',');
					if (space != null) {
						sb.append('\n');
						sb.append(i);
					}
					appendObject(sb, o, replacer, i, space, cyclicDetector);
				}
				count++;
			}
			cyclicDetector.remove(obj);
			if (space != null && count > 0) {
				sb.append('\n');
				sb.append(indent);
			}
			sb.append(']');
		}
		else if (obj instanceof Map<?, ?>) {
			if (cyclicDetector.contains(obj))
				throw new RuntimeException("Cyclic TypeError");
			String i = space == null ? "" : indent + space;
			boolean moreThanOne = false;
			sb.append('{');
			cyclicDetector.add(obj);
			for (Entry<String, Object> e : ((Map<String, Object>) obj).entrySet()) {
				Object o = replacer.replace(obj, e.getKey(), e.getValue());
				if (o != replacer) {
					if (moreThanOne)
						sb.append(',');
					if (space != null) {
						sb.append('\n');
						sb.append(i);
					}				
					sb.append('"');
					sb.append(e.getKey());
					sb.append("\":");
					if (space != null)
						sb.append(' ');
					appendObject(sb, o, replacer, i, space, cyclicDetector);
					moreThanOne = true;
				}
			}
			cyclicDetector.remove(obj);
			if (space != null && moreThanOne) {
				sb.append('\n');
				sb.append(indent);
			}
			sb.append('}');
		}
		else if (obj instanceof Collection<?>) {
			if (cyclicDetector.contains(obj))
				throw new RuntimeException("Cyclic TypeError");
			String i = space == null ? "" : indent + space;
			int count = 0;
			sb.append('[');
			cyclicDetector.add(obj);
			Iterator<?> iter = ((Collection<?>) obj).iterator();
			while (iter.hasNext()) {
				Object o = replacer.replace(obj, "" + count, iter.next());
				if (o != replacer) {
					if (count > 0)
						sb.append(',');
					if (space != null) {
						sb.append('\n');
						sb.append(i);
					}
					appendObject(sb, o, replacer, i, space, cyclicDetector);
				}
				count++;
			}
			cyclicDetector.remove(obj);
			if (space != null && count > 0) {
				sb.append('\n');
				sb.append(indent);
			}
			sb.append(']');
		}
		else if (obj instanceof Boolean)
			sb.append(((Boolean) obj).booleanValue());
		else {
			sb.append('"');
			sb.append(JsonUtils.EscapeJsonString(obj.toString()));
			sb.append('"');
		}
	}
}
