package com.bytelightning.oss.lib.concurrent;

import java.util.concurrent.*;

/**
 * BE VERY CAREFUL WITH THIS CLASS.
 * It assumes that tasks/commands will only be queud through its *own* public methods, and that those tasks/commands will be de-queued by the afterExecute method.
 */
public class BoundedExecutor extends ThreadPoolExecutor {
	public BoundedExecutor(int bound, ThreadFactory threadFactory) {
		super(bound, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
		semaphore = new Semaphore(bound);
	}
	private final Semaphore semaphore;

	public Future<?> submit(Runnable task) {
		semaphore.acquireUninterruptibly();
		return super.submit(task);
	}
	public <T> Future<T> submit(Runnable task, T result) {
		semaphore.acquireUninterruptibly();
		return super.submit(task, result);
	}
	public <T> Future<T> submit(Callable<T> task) {
		semaphore.acquireUninterruptibly();
		return super.submit(task);
	}
	public void execute(Runnable command) {
		semaphore.acquireUninterruptibly();
		super.execute(command);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		semaphore.release();
	}
}
