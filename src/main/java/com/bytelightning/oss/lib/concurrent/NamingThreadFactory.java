package com.bytelightning.oss.lib.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class NamingThreadFactory implements ThreadFactory {
	public NamingThreadFactory() {
		this(null);
	}

	public NamingThreadFactory(String threadPrefix) {
		this.threadPrefix = threadPrefix;
		this.nextThreadNum = new AtomicLong(1);
	}
	private final String threadPrefix;
	private AtomicLong nextThreadNum;

	@Override
	public Thread newThread(Runnable r) {
		String name = null;
		if (threadPrefix != null) {
			long num = nextThreadNum.getAndIncrement();
			name = threadPrefix + num;
		}
		return makeThread(name, r);
	}

	protected Thread makeThread(String name, Runnable r) {
		if (name != null)
			return new Thread(r, name);
		return new Thread(r);
	}
}
