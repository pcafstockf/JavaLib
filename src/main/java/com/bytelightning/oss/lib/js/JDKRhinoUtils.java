package com.bytelightning.oss.lib.js;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Utility class for dealing with the Rhino script engine found in jdk 1.6
 */
public class JDKRhinoUtils {

	protected JDKRhinoUtils() {
		super();
	}
	/**
	 * Global instance of the ScriptEngineManager.
	 */
	public static final ScriptEngineManager ScriptEngMgr = new ScriptEngineManager();

	/**
	 * Retrieve a ThreadLocal instance of a JavaScriptEngine.
	 * @param loadTimeoutFeature	Load a shim to support setTimeout / setInterval functionality.
	 * @param loadJSONFeature		Load Douglas Crockford's json2 shim library.
	 */
	public static ScriptEngine GetEngine(boolean loadTimeoutFeature, boolean loadJSONFeature) throws ScriptException {
		String key = "" + loadTimeoutFeature + loadJSONFeature;
		ThreadLocal<ScriptEngine> th = new ThreadLocal<ScriptEngine>();
		ThreadLocal<ScriptEngine> prev = engines.putIfAbsent(key, th);
		if (prev != null)
			th = prev;
		ScriptEngine retVal = th.get();
		if (retVal == null) {
			retVal = ScriptEngMgr.getEngineByName("rhino");
			if (loadTimeoutFeature) {
				// Adapted to actually work from: http://stackoverflow.com/questions/2261705/how-to-run-a-javascript-function-asynchronously-without-using-settimeout
				String timeoutHack = 
					"var setTimeout, clearTimeout, setInterval, clearInterval;\n" +
					"(function () {\n" +
					"    var timer = new " + RunnableTimer.class.getName() + "();\n" +
					"    var counter = 1; \n" +
					"    var ids = {};\n" +
					"    setTimeout = function (fn,delay) {\n" +
					"        if ((typeof(delay) === 'undefined') || (! delay))\n" +
					"            delay = 4;\n" +
					"        var id = counter++;\n" +
					"        ids[id] = new JavaAdapter(java.lang.Runnable,{run: fn});\n" +
					"        timer.schedule(ids[id],delay);\n" +
					"        return id;\n" +
					"    }\n" +
					"    clearTimeout = function (id) {\n" +
					"        ids[id].cancel();\n" +
					"        timer.purge();\n" +
					"        delete ids[id];\n" +
					"    }\n" +
					"    setInterval = function (fn,delay) {\n" +
					"        if ((typeof(delay) === 'undefined') || (! delay))\n" +
					"            delay = 4;\n" +
					"        var id = counter++; \n" +
					"        ids[id] = new JavaAdapter(java.lang.Runnable,{run: fn});\n" +
					"        timer.schedule(ids[id],delay,delay);\n" +
					"        return id;\n" +
					"    }\n" +
					"    clearInterval = clearTimeout;\n" +
					"})()";
				retVal.eval(timeoutHack);
			}
			if (loadJSONFeature) {
				InputStream jsonSrcIn = JDKRhinoUtils.class.getClassLoader().getResourceAsStream(JDKRhinoUtils.class.getPackage().getName().replace('.', '/') + "/json2.js");
				Reader jsonSrcRdr;
				try {
					jsonSrcRdr = new BufferedReader(new InputStreamReader(jsonSrcIn, "utf-8"));
					retVal.put(ScriptEngine.FILENAME, "json2.js");
					retVal.eval(jsonSrcRdr);
					try {
						jsonSrcRdr.close();
					}
					catch (IOException e) {
						e.printStackTrace(System.err);
					}
				}
				catch (UnsupportedEncodingException e) {
					e.printStackTrace(System.err); // Won't happen as all systems have utf-8
				}
			}
			th.set(retVal);
		}
		return retVal;
	}
	private static final ConcurrentHashMap<String, ThreadLocal<ScriptEngine>> engines = new ConcurrentHashMap<String, ThreadLocal<ScriptEngine>>();

	/**
	 * You don't need this.  It's public so that Rhino doesn't freak out.
	 */
	public static class RunnableTimer extends java.util.Timer {
		public RunnableTimer() {
			super("JavaScriptTimeoutImplementor", true);
		}
		public void schedule(final Runnable task, long delay) {
			super.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						task.run();
					}
					catch (Throwable t) {
						// Tasks should not throw exceptions, but if they do, don't contribute to the problem.
						t.printStackTrace(System.err);
					}
				}
			}, delay);
		}
	}

	/**
	 * Decompile a JDK Rhino JavaScript function into actual source code.
	 * @param fn	The function.
	 * @param onSingleLineInClosure	Return the text as a single line of code wrapped in a () closure.
	 * @param indentAmount	Spaces to indent.
	 */
	@SuppressWarnings("restriction")
	public static String DecompileFunction(sun.org.mozilla.javascript.internal.NativeFunction fn, boolean onSingleLineInClosure, int indentAmount) {
		String esrc = fn.getEncodedSource();
		sun.org.mozilla.javascript.internal.UintMap indentationMap = new sun.org.mozilla.javascript.internal.UintMap(3);
		indentationMap.put(1, 0); // Indents the entire function
		indentationMap.put(2, indentAmount); // Indent for each block
		indentationMap.put(3, indentAmount); // Indent for each case in switch
		// Note that passing '1' for the second param will return just the body of the function.
		String src = sun.org.mozilla.javascript.internal.Decompiler.decompile(esrc, onSingleLineInClosure ? 2 : 0, indentationMap);
		return src;
	}
}
