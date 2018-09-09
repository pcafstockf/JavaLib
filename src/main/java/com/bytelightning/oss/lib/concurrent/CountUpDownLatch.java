package com.bytelightning.oss.lib.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Similar to java.util.concurrent.CountDownLatch, but allows to count *up* as well as down.
 */
public class CountUpDownLatch {

	public CountUpDownLatch() {
		latch = new CountDownLatch(1);
		count = new AtomicLong(0);
	}
	volatile CountDownLatch latch;
	volatile AtomicLong count;

	/**
	 * Causes the current thread to wait until the latch has counted down to zero, unless the thread is interrupted.
	 * If the current count is zero then this method returns immediately.
	 * If the current count is greater than zero then the current thread becomes disabled for thread scheduling purposes and lies dormant until one of two things happen:
	 * The count reaches zero due to invocations of the countDown() method; or
	 * Some other thread interrupts the current thread.
	 * If the current thread:
	 * has its interrupted status set on entry to this method; or
	 * is interrupted while waiting,
	 * then InterruptedException is thrown and the current thread's interrupted status is cleared.
	 * @throws InterruptedException
	 */
	public void await() throws InterruptedException {
		if (count.get() == 0)
			return;
		CountDownLatch old;
		do {
			old = latch;
			latch.await();
		}
		while ((latch != old) || (count.get() > 0));
	}

	/**
	 * Decrements the count of the latch, releasing all waiting threads if the count reaches zero.
	 * If the current count is greater than zero then it is decremented. If the new count is zero then all waiting threads are re-enabled for thread scheduling purposes.
	 * If the current count equals zero then nothing happens.
	 */
	public void countDown() {
		if (count.decrementAndGet() == 0)
			latch.countDown();
	}

	/**
	 * Returns the current count.
	 * This method is typically used for debugging and testing purposes.
	 */
	public long getCount() {
		return count.get();
	}

	/**
	 * Increments the count of the latch temporarily preventing it from reaching zero.
	 */
	public void countUp() {
		if (latch.getCount() == 0)
			latch = new CountDownLatch(1);
		count.incrementAndGet();
	}
}
