package com.bytelightning.oss.lib.util;

/**
 * Assists in generating and printing a combinatorial matrix
 */
public class MatrixOption {
	public MatrixOption(String name, String[] values) {
		this.name = name;
		this.values = values;
	}
	public String name;
	public String[] values;

	public static void PrintMatrix(MatrixOption[] opts) {
		Columns = new String[opts.length];
		for (int j=Columns.length-1; j>=0; j--) {
			System.out.print(opts[j].name);
			System.out.print('\t');;
		}
		System.out.println();
		print(opts, opts.length-1);
	}
	private static void print(MatrixOption[] opts, int idx) {
		if (idx < 0) {
			for (int j=Columns.length-1; j>=0; j--) {
				System.out.print(Columns[j]);
				System.out.print('\t');
			}
			System.out.println();
			return;
		}
		MatrixOption o = opts[idx];
		for (int i=0; i<o.values.length; i++) {
			Columns[idx] = o.values[i];
			print(opts, idx-1);
		}
	}
	private static String[] Columns;

	// Usage of this class is as follows.
//	static public void main(String[] args) {
//		MatrixOption[] opts = {
//			new MatrixOption("A", new String[] {"1", "2", "3"}),
//			new MatrixOption("is", new String[] {"yes", "no"}),
//			new MatrixOption("I", new String[] {"a", "b", "c"}),
//		};
//		MatrixOption.PrintMatrix(opts);
//	}
}