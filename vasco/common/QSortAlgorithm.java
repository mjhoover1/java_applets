/* $Id: QSortAlgorithm.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

import java.util.*;

public class QSortAlgorithm {

	/**
	 * This is a generic version of C.A.R Hoare's Quick Sort algorithm. This will
	 * handle arrays that are already sorted, and arrays with duplicate keys.<BR>
	 *
	 * If you think of a one dimensional array as going from the lowest index on the
	 * left to the highest index on the right then the parameters to this function
	 * are lowest index or left and highest index or right. The first time you call
	 * this function it will be with the parameters 0, a.length - 1.
	 *
	 * @param a   an integer array
	 * @param lo0 left boundary of array partition
	 * @param hi0 right boundary of array partition
	 */
	static void QuickSort(Comparable a[], int lo0, int hi0) {
		int lo = lo0;
		int hi = hi0;
		double mid;

		if (hi0 > lo0) {

			/*
			 * Arbitrarily establishing partition element as the midpoint of the array.
			 */
			mid = a[(lo0 + hi0) / 2].sortBy();

			// loop through the array until indices cross
			while (lo <= hi) {
				/*
				 * find the first element that is greater than or equal to the partition element
				 * starting from the left Index.
				 */
				while ((lo < hi0) && (a[lo].sortBy() < mid))
					++lo;

				/*
				 * find an element that is smaller than or equal to the partition element
				 * starting from the right Index.
				 */
				while ((hi > lo0) && (a[hi].sortBy() > mid))
					--hi;

				// if the indexes have not crossed, swap
				if (lo <= hi) {
					swap(a, lo, hi);
					++lo;
					--hi;
				}
			}

			/*
			 * If the right index has not reached the left side of array must now sort the
			 * left partition.
			 */
			if (lo0 < hi)
				QuickSort(a, lo0, hi);

			/*
			 * If the left index has not reached the right side of array must now sort the
			 * right partition.
			 */
			if (lo < hi0)
				QuickSort(a, lo, hi0);

		}
	}

	private static void swap(Comparable a[], int i, int j) {
		Comparable T;
		T = a[i];
		a[i] = a[j];
		a[j] = T;

	}

	public static void sort(Vector v) {
		Comparable[] c = new Comparable[v.size()];
		for (int i = 0; i < c.length; i++)
			c[i] = (Comparable) v.elementAt(i);
		QuickSort(c, 0, c.length - 1);
		for (int i = 0; i < c.length; i++)
			v.setElementAt(c[i], i);
	}

	public static void sort(Comparable a[]) {
		QuickSort(a, 0, a.length - 1);
	}
}
