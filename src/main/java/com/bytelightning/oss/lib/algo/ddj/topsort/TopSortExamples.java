package com.bytelightning.oss.lib.algo.ddj.topsort;

import java.util.ArrayList;
import java.util.List;

/**
 * Some examples from the article
 *
 */
public class TopSortExamples {

	private static void example1() {
		String[] names = { "A", "B", "C", "D" };
		List<String> results;
		List<List<Integer>> theCycles;
		int numSorted;
		int numCycles;
		int i;

		System.out.println("Example 1: B, A, C, D");

		List<Relation> rels = new ArrayList<Relation>();
		rels.add(new Relation(1, 0));// b precedes a
		rels.add(new Relation(1, 3));// b precedes d
		rels.add(new Relation(0, 2));// a precedes c
		rels.add(new Relation(2, 3));// c precedes d

		results = new ArrayList<String>();
		numSorted = TopSort.topsort(names,
				rels.toArray(new Relation[rels.size()]), results);

		// print out the results. The order should be B, A, C, D
		if (numSorted != 4)
			System.out.println("topsort reports there is a cycle ");
		else
			System.out.println("topsort reports there is no cycle");
		for (i = 0; i < numSorted; i++)
			System.out.println(results.get(i));

		theCycles = new ArrayList<List<Integer>>();
		numCycles = TopSort.cycles(names,
				rels.toArray(new Relation[rels.size()]), theCycles);
		if (numCycles == 0)
			System.out
					.println("cycles reports (correctly) that there is no cycle");
		else
			System.out
					.println("cycles reports (incorrectly) that there is a cycle");

		System.out.println("Example 1A: B, A, [C  D]");
		// add in a recursive call: d precedes c.
		// Now there is a cycle, between d and c, so the order printed by
		// topsort should be B A
		rels.add(new Relation(3, 2)); // c precedes d
		results = new ArrayList<String>();
		numSorted = TopSort.topsort(names,
				rels.toArray(new Relation[rels.size()]), results);

		// print out the results. The order should be B, A
		if (numSorted == 2)
			System.out
					.println("topsort reports (correctly) that there is a cycle ");
		else if (numSorted == 4)
			System.out
					.println("topsort reports (incorrectly) that there is no cycle ");
		else
			System.out
					.println("topsort reports (incorrectly) that there is a cycle after "
							+ numSorted + " elements");
		for (i = 0; i < numSorted; i++)
			System.out.println(results.get(i));

		theCycles = new ArrayList<List<Integer>>();
		numCycles = TopSort.cycles(names,
				rels.toArray(new Relation[rels.size()]), theCycles);
		if (numCycles == 0)
			System.out
					.println("cycles reports (incorrectly) that there is no cycle");
		else
			System.out
					.println("cycles reports (correctly) that there is a cycle");
		for (i = 0; i < numCycles; i++) {
			System.out.print("cycle " + i + ": ");
			for (int j = 0; j < theCycles.get(i).size(); j++)
				System.out.print(names[theCycles.get(i).get(j)] + " ");
			System.out.println();
		}

		results = new ArrayList<String>();
		boolean haveCycle = TopSort.topsortWithCycles(names,
				rels.toArray(new Relation[rels.size()]), results);

		// print out the results. The order should be B, A
		if (haveCycle)
			System.out
					.println("topsortWithCycles reports (correctly) that there is a cycle ");
		else
			System.out
					.println("topsortWithCycles reports (incorrectly) that there is no cycle ");
		for (i = 0; i < 4; i++)
			System.out.println(results.get(i));
	}

	private static void example2() {
		int numSorted;
		int numCycles;
		int i;
		List<String> stringResults;

		System.out.println("Example 2: A, [B, C  D], E, [F, G]");
		String[] strings = { "a", "b", "c", "d", "e", "f", "g" };
		Relation[] rels2 = { new Relation(0, 1),// A precedes B
				new Relation(1, 2),// B precedes C
				new Relation(2, 3),// C precedes D
				new Relation(2, 1),// C precedes B
				new Relation(3, 1),// D precedes B
				new Relation(3, 4),// D precedes E
				new Relation(3, 5),// D precedes F
				new Relation(5, 6),// F precedes G
				new Relation(6, 5) // G precedes F
		};

		stringResults = new ArrayList<String>(strings.length);
		numSorted = TopSort.topsort(strings, rels2, stringResults);

		if (numSorted == 1)
			System.out
					.println("topsort reports (correctly) that there is a cycle after 1 element");
		else if (numSorted == strings.length)
			System.out
					.println("topsort reports (incorrectly) that there is no cycle ");
		else
			System.out
					.println("topsort reports (incorrectly) that there is a cycle after "
							+ numSorted + " elements");
		for (i = 0; i < numSorted; i++)
			System.out.println(stringResults.get(i));

		List<List<Integer>> cycleResults = new ArrayList<List<Integer>>(
				strings.length);
		numCycles = TopSort.cycles(strings, rels2, cycleResults);
		if (numCycles == 2)
			System.out
					.println("cycles reports (correctly) that there are two cycles");
		else if (numCycles == 0)
			System.out
					.println("cycles reports (incorrectly) that there is no cycle");
		else
			System.out.println("cycles reports (incorrectly) that there are "
					+ numCycles + " cycles");
		for (i = 0; i < numCycles; i++) {
			System.out.print("cycle " + i + ": ");
			for (int j = 0; j < cycleResults.get(i).size(); j++)
				System.out.print(strings[cycleResults.get(i).get(j)] + " ");
			System.out.println();
		}

		stringResults = new ArrayList<String>(strings.length);
		boolean haveCycle = TopSort.topsortWithCycles(strings, rels2,
				stringResults);

		// The order should be:
		// A [B C D] E [F G]
		// or
		// A [B C D] [F G] E
		// where the bracketed members can occur in any order
		if (haveCycle)
			System.out
					.println("topsortWithCycles reports (correctly) that there is a cycle ");
		else
			System.out
					.println("topsortWithCycles reports (incorrectly) that there is no cycle ");
		for (i = 0; i < strings.length; i++)
			System.out.println(stringResults.get(i));
	}

	public static void main(String[] args) {
		example1();
		example2();
	}
}
