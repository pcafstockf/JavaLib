package com.bytelightning.oss.lib.algo.ddj.topsort;

/**
 * Notes a positional dependency between two elements in the array of 'modules'.
 */
public class Relation {
	public Relation(int first, int second) {
		this.first = first;
		this.second = second;
	}
	protected int first;
	protected int second;
}
