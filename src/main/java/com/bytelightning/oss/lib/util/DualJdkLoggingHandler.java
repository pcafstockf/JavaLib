package com.bytelightning.oss.lib.util;

import java.util.logging.*;

/**
 * Typically used in standalone console Java applications, this class writes logging output to System.out/err
 * Specifically it writes more critical messages to System.err and more informational messages to System.out
 * This approach allows you to more easily spot errors in your application without losing useful progress information.
 * If the logging message is above the specified threshold, the log entry is written to System.out.
 * If the message is above the specified threshold, the entry will be written to System.err
 * You are also able to specify the format of the output.
 * Typically you would use this class as the very first line in your Application.main method:
 *		DualJdkLoggingHandler.InstallGlobally(Level.INFO, "%4$-7s %5$s%6$s%n");
 */
public class DualJdkLoggingHandler extends StreamHandler {

	public DualJdkLoggingHandler() {
		this(Level.INFO, new SimpleFormatter());
	}
	public DualJdkLoggingHandler(Level threshold, Formatter formatter) {
		super(System.out, formatter);
		this.threshold = threshold.intValue();
		this.stderrHandler = new ConsoleHandler();
	}
	private final int threshold;
	private final ConsoleHandler stderrHandler;

	@Override
	public void publish(LogRecord record) {
		if (record.getLevel().intValue() <= threshold) {
			super.publish(record);
			super.flush();
		}
		else {
			stderrHandler.publish(record);
			stderrHandler.flush();
		}
	}

	public static DualJdkLoggingHandler InstallGlobally(Level threshold, Formatter formatter) {
		LogManager.getLogManager().reset();
		Logger globalLogger = Logger.getLogger("");
		Handler[] handlers = globalLogger.getHandlers();
		for (Handler handler : handlers)
			globalLogger.removeHandler(handler);
		if (formatter == null)
			formatter = new SimpleFormatter();
		DualJdkLoggingHandler retVal = new DualJdkLoggingHandler(threshold, formatter);
		globalLogger.addHandler(retVal);
		return retVal;
	}
	public static DualJdkLoggingHandler InstallGlobally(Level threshold, String format) {
		if (format != null)
			System.setProperty("java.util.logging.SimpleFormatter.format", format);
		return InstallGlobally(threshold, (Formatter)null);
	}
	public static DualJdkLoggingHandler InstallGlobally(Level threshold) {
		return InstallGlobally(threshold, (String)null);
	}
}