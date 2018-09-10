package com.bytelightning.oss.lib.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public abstract class CleanUpThreadFactory extends NamingThreadFactory {
	protected static Logger logger = LoggerFactory.getLogger(CleanUpThreadFactory.class);

	public CleanUpThreadFactory() {
		this(null);
	}
	public CleanUpThreadFactory(String threadPrefix) {
		this(threadPrefix, threadPrefix != null);
	}
	public CleanUpThreadFactory(String threadPrefix, boolean indexSuffix) {
		this.threadPrefix = threadPrefix;
		if (indexSuffix)
			this.nextThreadNum = new AtomicLong(1);
		else
			this.nextThreadNum = null;
	}
	private final String threadPrefix;
	private AtomicLong nextThreadNum;

	@Override
	protected Thread makeThread(String name, final Runnable r) {
		Runnable wrapper = new Runnable() {
			public void run() {
				try {
					r.run();
				} finally {
					try {
						cleanup();
					}
					catch (Throwable t) {
						logger.error("Thread cleanup failed", t);
					}
				}
			}
		};
		if (threadPrefix != null)
			name = threadPrefix + name;
		if (nextThreadNum != null)
			name = name + nextThreadNum.incrementAndGet();
		return super.makeThread(name, wrapper);
	}

	protected abstract void cleanup();
}
