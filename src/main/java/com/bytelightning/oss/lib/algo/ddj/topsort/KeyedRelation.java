package com.bytelightning.oss.lib.algo.ddj.topsort;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Key value pair for tracking dependency relationships
 */
public class KeyedRelation<T> extends Relation {
	public KeyedRelation(T firstKey, T secondKey) {
		super(-1, -1);
		this.firstKey = firstKey;
		this.secondKey = secondKey;
	}
	private T firstKey;
	private T secondKey;
	
	void resolveIndexes(List<T> keys) {
		this.first = keys.indexOf(firstKey);
		if (this.first < 0)
			throw new NoSuchElementException("Key not found [first]");
		this.second = keys.indexOf(secondKey);
		if (this.second < 0)
			throw new NoSuchElementException("Key not found [second]");
	}
}
