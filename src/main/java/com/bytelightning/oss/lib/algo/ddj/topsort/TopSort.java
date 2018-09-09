package com.bytelightning.oss.lib.algo.ddj.topsort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TopSort {
	/**
	 * Topologically sort a set
	 * @param members the members being sorted
	 * @param relationships the relations imposing an order on the members list.
	 * @param result the longest linear order that can be created. If there are no cycles, this will be members.size()
	 * @return the number of members that could be sorted
	 */
	public static <T> int topsort(T[] members, Relation[] relationships, List<T> result) {
		// information about each index
		GraphInfo info = new GraphInfo(members.length);

		// Add each relation to the initial information, recording the list of successors and the predecessor count for each member.
		for (Relation rel : relationships) {
			if (rel.first != rel.second)
				if (!members[rel.first].equals(members[rel.second]))
					info.add(rel.first, rel.second);
		}

		// Begin sorting
		info.beginSort();

		// Now, repeatedly get the next member (updating the successor count) and put it into the next spot of the linear order
		int nextIndex;
		while ((nextIndex = info.nextWithoutPredecessors()) >= 0)
			result.add(members[nextIndex]);

		// we're done -- either because we sorted everything, or because we found a cycle.
		return info.sortedSize();
	}
	public static <T> int topsort(T[] members, KeyedRelation<T>[] relationships, List<T> result) {
		List<T> m = Arrays.asList(members);
		for (KeyedRelation<T> relationship : relationships)
			relationship.resolveIndexes(m);
		return topsort(members, (Relation[])relationships, result);
	}

	/**
	 * Find strongly-connected components
	 * @param members the members being sorted
	 * @param relationships the relations imposing an order on the members list.
	 * @param includeIndividuals whether to include SCCs of size 1
	 * @param result the list of strongly-connected components
	 * @return the number of strongly-connected components
	 */
	private static <T> int findSCCs(T[] members, Relation[] relationships, boolean includeIndividuals, List<List<Integer>> result) {
		int origResult = result.size();
		// information about each index
		GraphInfo info = new GraphInfo(members.length);

		// Add each relation to the initial information, recording the list of successors and the predecessor count for each member.
		for (Relation rel : relationships) {
			if (rel.first != rel.second)
				if (!members[rel.first].equals(members[rel.second]))
					info.add(rel.first, rel.second);
		}

		// now, look for SCCs beginning with each index
		int count = 0; // how many nodes have been visited
		for (int i = 0; i < members.length; i++)
			count = info.searchForSCC(i, count, includeIndividuals, result);

		return result.size() - origResult;
	}

	/**
	 * Find cycles in an order
	 * @param members the members being sorted
	 * @param relationships the relations imposing an order on the members list.
	 * @param result the list of cycles
	 * @return the number of cycles
	 */
	public static <T> int cycles(T[] members, Relation[] relationships, List<List<Integer>> result) {
		return findSCCs(members, relationships, false, result);
	}
	public static <T> int cycles(T[] members, KeyedRelation<T>[] relationships, List<List<Integer>> result) {
		List<T> m = Arrays.asList(members);
		for (KeyedRelation<T> relationship : relationships)
			relationship.resolveIndexes(m);
		return cycles(members, (Relation[])relationships, result);
	}

	/**
	 * Topologically sort a set expanding cycles
	 * @param members the members being sorted
	 * @param relationships the relations imposing an order on the members list.
	 * @param result the longest linear order that can be created. If there are no cycles, this will be members.size();
	 * @return whether or not there is a cycle
	 */
	public static <T> boolean topsortWithCycles(T[] members, Relation[] relationships, List<T> result) {
		// first of all, find strongly-connected components
		List<List<Integer>> sccs = new ArrayList<List<Integer>>(members.length);
		int numSccs = findSCCs(members, relationships, true, sccs);

		// for efficiency, create a mapping from each node to the SCC it is found in
		List<Integer> sccForIndex = new ArrayList<Integer>(members.length);
		for (int i = 0; i < members.length; i++)
			sccForIndex.add(0);
		for (int i = 0; i < numSccs; i++)
			for (int j = 0; j < sccs.get(i).size(); j++)
				sccForIndex.set(sccs.get(i).get(j), i);

		// now use this mapping to create relations between sccs.
		// Don't worry about relations between two nodes in the same SCC.
		List<Relation> SCCrels = new ArrayList<Relation>();
		for (Relation rel : relationships) {
			int predcessor = sccForIndex.get(rel.first);
			int successor = sccForIndex.get(rel.second);
			if (predcessor != successor)
				if (!members[predcessor].equals(members[successor]))
					SCCrels.add(new Relation(predcessor, successor));
		}

		// sort the SCCs using the between-SCC relations
		List<List<Integer>> sortedSccs = new ArrayList<List<Integer>>(members.length);
		@SuppressWarnings("unchecked")
		List<Integer>[] sccsA = (List<Integer>[]) new List<?>[sccs.size()];
		topsort(sccs.toArray(sccsA), SCCrels.toArray(new Relation[SCCrels.size()]), sortedSccs);

		// and now expand the sorted SCCs
		for (int i = 0; i < numSccs; i++)
			for (int j = 0; j < sortedSccs.get(i).size(); j++)
				result.add(members[sortedSccs.get(i).get(j)]);

		// finally, we have a cycle if there is at least one SCC of size > 1, which would imply that there are fewer SCCs than nodes
		return (numSccs < members.length);
	}
	public static <T> boolean topsortWithCycles(T[] members, KeyedRelation<T>[] relationships, List<T> result) {
		List<T> m = Arrays.asList(members);
		for (KeyedRelation<T> relationship : relationships)
			relationship.resolveIndexes(m);
		return topsortWithCycles(members, (Relation[])relationships, result);
	}
}
