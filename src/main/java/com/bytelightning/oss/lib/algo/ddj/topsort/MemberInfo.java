package com.bytelightning.oss.lib.algo.ddj.topsort;

import java.util.ArrayList;
import java.util.List;

/**
 * MemberInfo represents information about each member in the sorting process, including how many predecessors it has, and a list of successors
 * 
 */
public final class MemberInfo {
	public MemberInfo() {
		predecessorCount = 0;
		successors = new ArrayList<Integer>();
		dfn = 0;
		lowestRoot = 0;
		visited = false;
	}

	public void addSuccessor(int successor) {
		successors.add(successor);
	}

	public void addPredecessor(int predecessor) {
		predecessorCount++;
	}

	public int removePredecessor(int predecessor) {
		return --predecessorCount;
	}

	public int numPredecessors() {
		return predecessorCount;
	}

	public List<Integer> getSuccessors() {
		return successors;
	}

	public int getDfn() {
		return dfn;
	}

	public boolean getVisited() {
		return visited;
	}

	public void beginVisit(int count) {
		dfn = lowestRoot = count;
		visited = true;
	}

	public int getRoot() {
		return lowestRoot;
	}

	public void recordCycle(int root) {
		if (root < lowestRoot)
			lowestRoot = root;
	}

	private boolean visited;
	private int predecessorCount;
	private int dfn;
	private int lowestRoot;
	private List<Integer> successors;
}
