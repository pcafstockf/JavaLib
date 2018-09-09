package com.bytelightning.oss.lib.algo.ddj.topsort;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Stack;

/**
 * The overall information about a graph, used both for sorting and strongly connected components.
 */
public final class GraphInfo {
	public GraphInfo(int num) {
		members = new ArrayList<MemberInfo>(num);
		for (int i = 0; i < num; i++)
			members.add(new MemberInfo());
		numSorted = 0;
		stack = new Stack<Integer>();
		outputQueue = new ArrayDeque<Integer>();
	}

	/**
	 * Add a relation (specified as two indices)
	 * @param predecessor
	 * @param successor
	 */
	public void add(int predecessor, int successor) {
		members.get(predecessor).addSuccessor(successor);
		members.get(successor).addPredecessor(predecessor);
	}

	/**
	 * Add all elements without predecessors to the outputQueue.
	 * Strictly speaking, only the first needs to be added, but adding them all is a nice touch in that it will make all the leaf objects come out first.
	 */
	public void beginSort() {
		for (int i = 0; i < members.size(); i++) {
			if (members.get(i).numPredecessors() == 0)
				outputQueue.addLast(i);
		}
	}

	/**
	 * return the next element not in a cycle
	 */
	public int nextWithoutPredecessors() {
		// if there's nothing on the queue, we're either done, or there's a cycle
		if (outputQueue.isEmpty())
			return -1;
		// Pop the next member off of the queue; it's the next member in the linear order
		int nextIndex = outputQueue.pollFirst();
		numSorted++;

		// Now decrement the count of all its successors.
		// If any of them have no more predecessors, they can go onto the output queue
		List<Integer> successors = members.get(nextIndex).getSuccessors();
		for (int i = 0; i < successors.size(); i++) {
			int successor = successors.get(i);
			members.get(successor).removePredecessor(i);
			if (members.get(successor).numPredecessors() == 0)
				outputQueue.addLast(successor);
		}
		return nextIndex;
	}

	/**
	 * do a depth-first search
	 * @param start
	 * @param count
	 * @param includeIndividuals
	 * @param components
	 * @return
	 */
	public int searchForSCC(int start, int count, boolean includeIndividuals, List<List<Integer>> components) {
		// if we've already visited this member, nothing else need be done
		if (members.get(start).getVisited())
			return count;

		// record that we've visited this member
		members.get(start).beginVisit(count++);

		// push this member on the stack and begin iterating through its successors
		stack.push(start);
		List<Integer> successors = members.get(start).getSuccessors();
		for (int i = 0; i < successors.size(); i++) {
			int next = successors.get(i);

			if (next == start)
				// it's a cycle; but we need to continue because we may find other (longer) cycles
				continue;

			if (members.get(next).getVisited()) {
				// we've visited this already.
				// If it's on the stack that we've visited on this path, then it's a cycle -- but perhaps not the longest cycle
				if (stack.contains(next))
					members.get(start).recordCycle(members.get(next).getDfn());
			}
			else {
				// it's a node we've never visited before
				count = searchForSCC(next, count, includeIndividuals, components);
				members.get(start).recordCycle(members.get(next).getRoot());
			}
		}

		// we've searched through all the sucessors, so we'll have found a cycle if one is there
		if (members.get(start).getRoot() == members.get(start).getDfn()) {
			List<Integer> newComponent = new ArrayList<Integer>();
			int last;
			// pop everything up to this node off the stack
			do {
				last = stack.pop();
				newComponent.add(last);
			}
			while (last != start);
			// if there's at least one here, it's a cycle
			if (newComponent.size() > 1 || (includeIndividuals && newComponent.size() == 1))
				components.add(newComponent);
		}
		return count;
	}

	/**
	 * how many members are there?
	 */
	public int size() {
		return members.size();
	}

	/**
	 * has everything been sorted?
	 * @return
	 */
	public int sortedSize() {
		return numSorted;
	}

	/**
	 * how many are sorted?
	 */
	private int numSorted;
	/**
	 * the info field is a list of memberInfo structs, with the information about each member
	 */
	private List<MemberInfo> members;
	/**
	 * the outputQueue is the queue of indices of elements that are ready to be output, i.e., have no predecessors
	 */
	private Deque<Integer> outputQueue;
	/**
	 * the stack of nodes visited on this path
	 */
	private Stack<Integer> stack;
}
