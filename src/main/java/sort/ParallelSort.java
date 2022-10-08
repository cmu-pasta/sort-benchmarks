/*
* Copyright (c) 2009, 2021, Oracle and/or its affiliates. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This code is free software; you can redistribute it and/or modify it
* under the terms of the GNU General Public License version 2 only, as
* published by the Free Software Foundation.  Oracle designates this
* particular file as subject to the "Classpath" exception as provided
* by Oracle in the LICENSE file that accompanied this code.
*
* This code is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
* version 2 for more details (a copy is included in the LICENSE file that
* accompanied this code).
*
* You should have received a copy of the GNU General Public License version
* 2 along with this work; if not, write to the Free Software Foundation,
* Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
*
* Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
* or visit www.oracle.com if you need additional information or have any
* questions.
*/

//modified from java.util.DualPivotQuicksort by Bella Laybourn to deal with objects instead of primitives
package sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.RecursiveTask;

public class ParallelSort implements SortAlgorithm {
    private static final int MAX_PARALLELISM = 256;

    public <T extends Comparable<T>> T[] sort(T[] arr) {
        sort(arr, Math.min(MAX_PARALLELISM, arr.length / MAX_MIXED_INSERTION_SORT_SIZE), 0, arr.length);
        return arr;
    }

    /**
     * Max array size to use mixed insertion sort.
     */
    private static final int MAX_MIXED_INSERTION_SORT_SIZE = 65;

    /**
     * Max array size to use insertion sort.
     */
    private static final int MAX_INSERTION_SORT_SIZE = 44;

    /**
     * Min array size to perform sorting in parallel.
     */
    private static final int MIN_PARALLEL_SORT_SIZE = 4 << 10;

    /**
     * Min array size to try merging of runs.
     */
    private static final int MIN_TRY_MERGE_SIZE = 4 << 10;

    /**
     * Min size of the first run to continue with scanning.
     */
    private static final int MIN_FIRST_RUN_SIZE = 16;

    /**
     * Min factor for the first runs to continue scanning.
     */
    private static final int MIN_FIRST_RUNS_FACTOR = 7;

    /**
     * Max capacity of the index array for tracking runs.
     */
    private static final int MAX_RUN_CAPACITY = 5 << 10;

    /**
     * Min number of runs, required by parallel merging.
     */
    private static final int MIN_RUN_COUNT = 4;

    /**
     * Min array size to use parallel merging of parts.
     */
    private static final int MIN_PARALLEL_MERGE_PARTS_SIZE = 4 << 10;

    /**
     * Threshold of mixed insertion sort is incremented by this value.
     */
    private static final int DELTA = 3 << 1;

    /**
     * Max recursive partitioning depth before using heap sort.
     */
    private static final int MAX_RECURSION_DEPTH = 64 * DELTA;

    /**
     * Calculates the double depth of parallel merging.
     * Depth is negative, if tasks split before sorting.
     *
     * @param parallelism the parallelism level
     * @param size the target size
     * @return the depth of parallel merging
     */
    private static int getDepth(int parallelism, int size) {
        int depth = 0;

        while ((parallelism >>= 3) > 0 && (size >>= 2) > 0) {
            depth -= 2;
        }
        return depth;
    }

    /**
     * Sorts the specified range of the array using parallel merge
     * sort and/or Dual-Pivot Quicksort.
     *
     * To balance the faster splitting and parallelism of merge sort
     * with the faster element partitioning of Quicksort, ranges are
     * subdivided in tiers such that, if there is enough parallelism,
     * the four-way parallel merge is started, still ensuring enough
     * parallelism to process the partitions.
     *
     * @param a the array to be sorted
     * @param parallelism the parallelism level
     * @param low the index of the first element, inclusive, to be sorted
     * @param high the index of the last element, exclusive, to be sorted
     */
    static <T extends Comparable<T>> void sort(T[] a, int parallelism, int low, int high) {
        int size = high - low;

        if (parallelism > 1 && size > MIN_PARALLEL_SORT_SIZE) {
            int depth = getDepth(parallelism, size >> 12);
            ArrayList<T> b = depth == 0 ? null : new ArrayList<>(size);
            if(b != null) { for(int c = 0; c < size; c++) b.add(null); }
            new Sorter<T>(null, a, b == null ? null : (T[]) b.toArray(), low, size, low, depth).invoke();
        } else {
            sort(null, a, 0, low, high);
        }
    }

    /**
     * Sorts the specified array using the Dual-Pivot Quicksort and/or
     * other sorts in special-cases, possibly with parallel partitions.
     *
     * @param sorter parallel context
     * @param a the array to be sorted
     * @param bits the combination of recursion depth and bit flag, where
     *        the right bit "0" indicates that array is the leftmost part
     * @param low the index of the first element, inclusive, to be sorted
     * @param high the index of the last element, exclusive, to be sorted
     */
    static <T extends Comparable<T>> void sort(Sorter<T> sorter, T[] a, int bits, int low, int high) {
        while (true) {
            int end = high - 1, size = high - low;

            /*
             * Run mixed insertion sort on small non-leftmost parts.
             */
            if (size < MAX_MIXED_INSERTION_SORT_SIZE + bits && (bits & 1) > 0) {
                mixedInsertionSort(a, low, high - 3 * ((size >> 5) << 3), high);
                return;
            }

            /*
             * Invoke insertion sort on small leftmost part.
             */
            if (size < MAX_INSERTION_SORT_SIZE) {
                insertionSort(a, low, high);
                return;
            }

            /*
             * Check if the whole array or large non-leftmost
             * parts are nearly sorted and then merge runs.
             */
            if ((bits == 0 || size > MIN_TRY_MERGE_SIZE && (bits & 1) > 0)
                    && tryMergeRuns(sorter, a, low, size)) {
                return;
            }

            /*
             * Switch to heap sort if execution
             * time is becoming quadratic.
             */
            if ((bits += DELTA) > MAX_RECURSION_DEPTH) {
                heapSort(a, low, high);
                return;
            }

            /*
             * Use an inexpensive approximation of the golden ratio
             * to select five sample elements and determine pivots.
             */
            int step = (size >> 3) * 3 + 3;

            /*
             * Five elements around (and including) the central element
             * will be used for pivot selection as described below. The
             * unequal choice of spacing these elements was empirically
             * determined to work well on a wide variety of inputs.
             */
            int e1 = low + step;
            int e5 = end - step;
            int e3 = (e1 + e5) >>> 1;
            int e2 = (e1 + e3) >>> 1;
            int e4 = (e3 + e5) >>> 1;
            T a3 = a[e3];

            /*
             * Sort these elements in place by the combination
             * of 4-element sorting network and insertion sort.
             *
             *    5 ------o-----------o------------
             *            |           |
             *    4 ------|-----o-----o-----o------
             *            |     |           |
             *    2 ------o-----|-----o-----o------
             *                  |     |
             *    1 ------------o-----o------------
             */
            if (a[e5] == null || a[e5].compareTo(a[e2]) < 0) { T t = a[e5]; a[e5] = a[e2]; a[e2] = t; }
            if (a[e4] == null || a[e4].compareTo(a[e1]) < 0) { T t = a[e4]; a[e4] = a[e1]; a[e1] = t; }
            if (a[e5] == null || a[e5].compareTo(a[e4]) < 0) { T t = a[e5]; a[e5] = a[e4]; a[e4] = t; }
            if (a[e2] == null || a[e2].compareTo(a[e1]) < 0) { T t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
            if (a[e4] == null || a[e4].compareTo(a[e2]) < 0) { T t = a[e4]; a[e4] = a[e2]; a[e2] = t; }

            if (a3 == null || a3.compareTo(a[e2]) < 0) {
                if (a3 == null || a3.compareTo(a[e1]) < 0) {
                    a[e3] = a[e2]; a[e2] = a[e1]; a[e1] = a3;
                } else {
                    a[e3] = a[e2]; a[e2] = a3;
                }
            } else if (a3.compareTo(a[e4]) > 0) {
                if (a3.compareTo(a[e5]) > 0) {
                    a[e3] = a[e4]; a[e4] = a[e5]; a[e5] = a3;
                } else {
                    a[e3] = a[e4]; a[e4] = a3;
                }
            }

            // Pointers
            int lower = low; // The index of the last element of the left part
            int upper = end; // The index of the first element of the right part

            /*
             * Partitioning with 2 pivots in case of different elements.
             */
            if ((a[e1] == null || a[e1].compareTo(a[e2]) < 0) && (a[e2] == null || a[e2].compareTo(a[e3]) < 0) && (a[e3] == null || a[e3].compareTo(a[e4]) < 0) && (a[e4] == null || a[e4].compareTo(a[e5]) < 0)) {

                /*
                 * Use the first and fifth of the five sorted elements as
                 * the pivots. These values are inexpensive approximation
                 * of tertiles. Note, that pivot1 < pivot2.
                 */
                T pivot1 = a[e1];
                T pivot2 = a[e5];

                /*
                 * The first and the last elements to be sorted are moved
                 * to the locations formerly occupied by the pivots. When
                 * partitioning is completed, the pivots are swapped back
                 * into their final positions, and excluded from the next
                 * subsequent sorting.
                 */
                a[e1] = a[lower];
                a[e5] = a[upper];

                /*
                 * Skip elements, which are less or greater than the pivots.
                 */
                while (a[lower + 1] == null || a[++lower].compareTo(pivot1) < 0);
                while (a[upper - 1] == null || a[--upper].compareTo(pivot2) > 0);

                /*
                 * Backward 3-interval partitioning
                 *
                 *   left part                 central part          right part
                 * +------------------------------------------------------------+
                 * |  < pivot1  |   ?   |  pivot1 <= && <= pivot2  |  > pivot2  |
                 * +------------------------------------------------------------+
                 *             ^       ^                            ^
                 *             |       |                            |
                 *           lower     k                          upper
                 *
                 * Invariants:
                 *
                 *              all in (low, lower] < pivot1
                 *    pivot1 <= all in (k, upper)  <= pivot2
                 *              all in [upper, end) > pivot2
                 *
                 * Pointer k is the last index of ?-part
                 */
                for (int unused = --lower, k = ++upper; --k > lower; ) {
                    T ak = a[k];

                    if (ak == null || ak.compareTo(pivot1) < 0) { // Move a[k] to the left side
                        while (lower < k) {
                            if (pivot1.compareTo(a[++lower]) <= 0) {
                                if (pivot2.compareTo(a[lower]) < 0) {
                                    a[k] = a[--upper];
                                    a[upper] = a[lower];
                                } else {
                                    a[k] = a[lower];
                                }
                                a[lower] = ak;
                                break;
                            }
                        }
                    } else if (ak.compareTo(pivot2) > 0) { // Move a[k] to the right side
                        a[k] = a[--upper];
                        a[upper] = ak;
                    }
                }

                /*
                 * Swap the pivots into their final positions.
                 */
                a[low] = a[lower]; a[lower] = pivot1;
                a[end] = a[upper]; a[upper] = pivot2;

                /*
                 * Sort non-left parts recursively (possibly in parallel),
                 * excluding known pivots.
                 */
                if (size > MIN_PARALLEL_SORT_SIZE && sorter != null) {
                    sorter.forkSorter(bits | 1, lower + 1, upper);
                    sorter.forkSorter(bits | 1, upper + 1, high);
                } else {
                    sort(sorter, a, bits | 1, lower + 1, upper);
                    sort(sorter, a, bits | 1, upper + 1, high);
                }

            } else { // Use single pivot in case of many equal elements

                /*
                 * Use the third of the five sorted elements as the pivot.
                 * This value is inexpensive approximation of the median.
                 */
                T pivot = a[e3];

                /*
                 * The first element to be sorted is moved to the
                 * location formerly occupied by the pivot. After
                 * completion of partitioning the pivot is swapped
                 * back into its final position, and excluded from
                 * the next subsequent sorting.
                 */
                a[e3] = a[lower];

                /*
                 * Traditional 3-way (Dutch National Flag) partitioning
                 *
                 *   left part                 central part    right part
                 * +------------------------------------------------------+
                 * |   < pivot   |     ?     |   == pivot   |   > pivot   |
                 * +------------------------------------------------------+
                 *              ^           ^                ^
                 *              |           |                |
                 *            lower         k              upper
                 *
                 * Invariants:
                 *
                 *   all in (low, lower] < pivot
                 *   all in (k, upper)  == pivot
                 *   all in [upper, end] > pivot
                 *
                 * Pointer k is the last index of ?-part
                 */
                for (int k = ++upper; --k > lower; ) {
                    T ak = a[k];

                    if (ak != pivot) {
                        a[k] = pivot;

                        if (ak == null || ak.compareTo(pivot) < 0) { // Move a[k] to the left side
                            while (a[lower + 1] == null || a[++lower].compareTo(pivot) < 0);

                            if (pivot.compareTo(a[lower]) < 0) {
                                a[--upper] = a[lower];
                            }
                            a[lower] = ak;
                        } else { // ak > pivot - Move a[k] to the right side
                            a[--upper] = ak;
                        }
                    }
                }

                /*
                 * Swap the pivot into its final position.
                 */
                a[low] = a[lower]; a[lower] = pivot;

                /*
                 * Sort the right part (possibly in parallel), excluding
                 * known pivot. All elements from the central part are
                 * equal and therefore already sorted.
                 */
                if (size > MIN_PARALLEL_SORT_SIZE && sorter != null) {
                    sorter.forkSorter(bits | 1, upper, high);
                } else {
                    sort(sorter, a, bits | 1, upper, high);
                }
            }
            high = lower; // Iterate along the left part
        }
    }

    /**
     * Sorts the specified range of the array using mixed insertion sort.
     *
     * Mixed insertion sort is combination of simple insertion sort,
     * pin insertion sort and pair insertion sort.
     *
     * In the context of Dual-Pivot Quicksort, the pivot element
     * from the left part plays the role of sentinel, because it
     * is less than any elements from the given part. Therefore,
     * expensive check of the left range can be skipped on each
     * iteration unless it is the leftmost call.
     *
     * @param a the array to be sorted
     * @param low the index of the first element, inclusive, to be sorted
     * @param end the index of the last element for simple insertion sort
     * @param high the index of the last element, exclusive, to be sorted
     */
    private static <T extends Comparable<T>> void mixedInsertionSort(T[] a, int low, int end, int high) {
        if (end == high) {

            /*
             * Invoke simple insertion sort on tiny array.
             */
            for (int i; ++low < end; ) {
                T ai = a[i = low];

                while (ai == null || ai.compareTo(a[--i]) < 0) {
                    a[i + 1] = a[i];
                }
                a[i + 1] = ai;
            }
        } else {

            /*
             * Start with pin insertion sort on small part.
             *
             * Pin insertion sort is extended simple insertion sort.
             * The main idea of this sort is to put elements larger
             * than an element called pin to the end of array (the
             * proper area for such elements). It avoids expensive
             * movements of these elements through the whole array.
             */
            T pin = a[end];

            for (int i, p = high; ++low < end; ) {
                T ai = a[i = low];

                if (ai == null || ai.compareTo(a[i - 1]) < 0) { // Small element

                    /*
                     * Insert small element into sorted part.
                     */
                    a[i] = a[--i];

                    while (ai == null || ai.compareTo(a[--i]) < 0) {
                        a[i + 1] = a[i];
                    }
                    a[i + 1] = ai;

                } else if (p > i && ai.compareTo(pin) > 0) { // Large element

                    /*
                     * Find element smaller than pin.
                     */
                    while (pin.compareTo(a[--p]) < 0);

                    /*
                     * Swap it with large element.
                     */
                    if (p > i) {
                        ai = a[p];
                        a[p] = a[i];
                    }

                    /*
                     * Insert small element into sorted part.
                     */
                    while (ai == null || ai.compareTo(a[--i]) < 0) {
                        a[i + 1] = a[i];
                    }
                    a[i + 1] = ai;
                }
            }

            /*
             * Continue with pair insertion sort on remain part.
             */
            for (int i; low < high; ++low) {
                T a1 = a[i = low], a2 = a[++low];

                /*
                 * Insert two elements per iteration: at first, insert the
                 * larger element and then insert the smaller element, but
                 * from the position where the larger element was inserted.
                 */
                if (a1 == null || a2.compareTo(a1) < 0) {

                    while (a1 == null || a1.compareTo(a[--i]) < 0) {
                        a[i + 2] = a[i];
                    }
                    a[++i + 1] = a1;

                    while (a2.compareTo(a[--i]) < 0) {
                        a[i + 1] = a[i];
                    }
                    a[i + 1] = a2;

                } else if (a1.compareTo(a[i - 1]) < 0) {

                    while (a2.compareTo(a[--i]) < 0) {
                        a[i + 2] = a[i];
                    }
                    a[++i + 1] = a2;

                    while (a1.compareTo(a[--i]) < 0) {
                        a[i + 1] = a[i];
                    }
                    a[i + 1] = a1;
                }
            }
        }
    }

    /**
     * Sorts the specified range of the array using insertion sort.
     *
     * @param a the array to be sorted
     * @param low the index of the first element, inclusive, to be sorted
     * @param high the index of the last element, exclusive, to be sorted
     */
    private static <T extends Comparable<T>> void insertionSort(T[] a, int low, int high) {
        for (int i, k = low; ++k < high; ) {
            T ai = a[i = k];

            if (ai == null || ai.compareTo(a[i - 1]) < 0) {
                while (--i >= low && (ai == null || ai.compareTo(a[i]) < 0)) {
                    a[i + 1] = a[i];
                }
                a[i + 1] = ai;
            }
        }
    }

    /**
     * Sorts the specified range of the array using heap sort.
     *
     * @param a the array to be sorted
     * @param low the index of the first element, inclusive, to be sorted
     * @param high the index of the last element, exclusive, to be sorted
     */
    private static <T extends Comparable<T>> void heapSort(T[] a, int low, int high) {
        for (int k = (low + high) >>> 1; k > low; ) {
            pushDown(a, --k, a[k], low, high);
        }
        while (--high > low) {
            T max = a[low];
            pushDown(a, low, a[high], low, high);
            a[high] = max;
        }
    }

    /**
     * Pushes specified element down during heap sort.
     *
     * @param a the given array
     * @param p the start index
     * @param value the given element
     * @param low the index of the first element, inclusive, to be sorted
     * @param high the index of the last element, exclusive, to be sorted
     */
    private static <T extends Comparable<T>> void pushDown(T[] a, int p, T value, int low, int high) {
        for (int k ;; a[p] = a[p = k]) {
            k = (p << 1) - low + 2; // Index of the right child

            if (k > high) {
                break;
            }
            if (k == high || a[k] == null || a[k].compareTo(a[k - 1]) < 0) {
                --k;
            }
            if (a[k] == null || a[k].compareTo(value) <= 0) {
                break;
            }
        }
        a[p] = value;
    }

    /**
     * Tries to sort the specified range of the array.
     *
     * @param sorter parallel context
     * @param a the array to be sorted
     * @param low the index of the first element to be sorted
     * @param size the array size
     * @return true if finally sorted, false otherwise
     */
    private static <T extends Comparable<T>> boolean tryMergeRuns(Sorter<T> sorter, T[] a, int low, int size) {

        /*
         * The run array is constructed only if initial runs are
         * long enough to continue, run[i] then holds start index
         * of the i-th sequence of elements in non-descending order.
         */
        int[] run = null;
        int high = low + size;
        int count = 1, last = low;

        /*
         * Identify all possible runs.
         */
        for (int k = low + 1; k < high; ) {

            /*
             * Find the end index of the current run.
             */
            if (a[k-1] == null || a[k - 1].compareTo(a[k]) < 0) {

                // Identify ascending sequence
                while (++k < high && (a[k - 1] == null || a[k - 1].compareTo(a[k]) <= 0));

            } else if (a[k] == null || a[k].compareTo(a[k - 1]) < 0) {

                // Identify descending sequence
                while (++k < high && (a[k] == null || a[k].compareTo(a[k - 1]) >= 0));

                // Reverse into ascending order
                for (int i = last - 1, j = k; ++i < --j && (a[j] == null || a[j].compareTo(a[i]) < 0); ) {
                    T ai = a[i]; a[i] = a[j]; a[j] = ai;
                }
            } else { // Identify constant sequence
                for (T ak = a[k]; ++k < high && (a[k] == ak || a[k].compareTo(ak) == 0); );

                if (k < high) {
                    continue;
                }
            }

            /*
             * Check special cases.
             */
            if (run == null) {
                if (k == high) {

                    /*
                     * The array is monotonous sequence,
                     * and therefore already sorted.
                     */
                    return true;
                }

                if (k - low < MIN_FIRST_RUN_SIZE) {

                    /*
                     * The first run is too small
                     * to proceed with scanning.
                     */
                    return false;
                }

                run = new int[((size >> 10) | 0x7F) & 0x3FF];
                run[0] = low;

            } else if (a[last] == null || a[last].compareTo(a[last - 1]) > 0) {

                if (count > (k - low) >> MIN_FIRST_RUNS_FACTOR) {

                    /*
                     * The first runs are not long
                     * enough to continue scanning.
                     */
                    return false;
                }

                if (++count == MAX_RUN_CAPACITY) {

                    /*
                     * Array is not highly structured.
                     */
                    return false;
                }

                if (count == run.length) {

                    /*
                     * Increase capacity of index array.
                     */
                    run = Arrays.copyOf(run, count << 1);
                }
            }
            run[count] = (last = k);
        }

        /*
         * Merge runs of highly structured array.
         */
        if (count > 1) {
            T[] b; int offset = low;

            if (sorter == null || (b = sorter.b) == null) {
                ArrayList<T> tmp = new ArrayList<>(size);
                for(int c = 0; c < size; c++) tmp.add(null);
                b = (T[]) tmp.toArray();
            } else {
                offset = sorter.offset;
            }
            mergeRuns(a, b, offset, 1, sorter != null, run, 0, count);
        }
        return true;
    }

    /**
     * Merges the specified runs.
     *
     * @param a the source array
     * @param b the temporary buffer used in merging
     * @param offset the start index in the source, inclusive
     * @param aim specifies merging: to source ( > 0), buffer ( < 0) or any ( == 0)
     * @param parallel indicates whether merging is performed in parallel
     * @param run the start indexes of the runs, inclusive
     * @param lo the start index of the first run, inclusive
     * @param hi the start index of the last run, inclusive
     * @return the destination where runs are merged
     */
    private static <T extends Comparable<T>> T[] mergeRuns(T[] a, T[] b, int offset,
                                   int aim, boolean parallel, int[] run, int lo, int hi) {

        if (hi - lo == 1) {
            if (aim >= 0) {
                return a;
            }
            for (int i = run[hi], j = i - offset, low = run[lo]; i > low;
                 b[--j] = a[--i]
            );
            return b;
        }

        /*
         * Split into approximately equal parts.
         */
        int mi = lo, rmi = (run[lo] + run[hi]) >>> 1;
        while (run[++mi + 1] <= rmi);

        /*
         * Merge the left and right parts.
         */
        T[] a1, a2;

        if (parallel && hi - lo > MIN_RUN_COUNT) {
            RunMerger<T> merger = new RunMerger<>(a, b, offset, 0, run, mi, hi).forkMe();
            a1 = mergeRuns(a, b, offset, -aim, true, run, lo, mi);
            a2 = merger.getDestination();
        } else {
            a1 = mergeRuns(a, b, offset, -aim, false, run, lo, mi);
            a2 = mergeRuns(a, b, offset,    0, false, run, mi, hi);
        }

        T[] dst = a1 == a ? b : a;

        int k   = a1 == a ? run[lo] - offset : run[lo];
        int lo1 = a1 == b ? run[lo] - offset : run[lo];
        int hi1 = a1 == b ? run[mi] - offset : run[mi];
        int lo2 = a2 == b ? run[mi] - offset : run[mi];
        int hi2 = a2 == b ? run[hi] - offset : run[hi];

        if (parallel) {
            new Merger<T>(null, dst, k, a1, lo1, hi1, a2, lo2, hi2).invoke();
        } else {
            mergeParts(null, dst, k, a1, lo1, hi1, a2, lo2, hi2);
        }
        return dst;
    }

    /**
     * Merges the sorted parts.
     *
     * @param merger parallel context
     * @param dst the destination where parts are merged
     * @param k the start index of the destination, inclusive
     * @param a1 the first part
     * @param lo1 the start index of the first part, inclusive
     * @param hi1 the end index of the first part, exclusive
     * @param a2 the second part
     * @param lo2 the start index of the second part, inclusive
     * @param hi2 the end index of the second part, exclusive
     */
    private static <T extends Comparable<T>> void mergeParts(Merger<T> merger, T[] dst, int k,
                                   T[] a1, int lo1, int hi1, T[] a2, int lo2, int hi2) {

        if (merger != null && a1 == a2) {

            while (true) {

                /*
                 * The first part must be larger.
                 */
                if (hi1 - lo1 < hi2 - lo2) {
                    int lo = lo1; lo1 = lo2; lo2 = lo;
                    int hi = hi1; hi1 = hi2; hi2 = hi;
                }

                /*
                 * Small parts will be merged sequentially.
                 */
                if (hi1 - lo1 < MIN_PARALLEL_MERGE_PARTS_SIZE) {
                    break;
                }

                /*
                 * Find the median of the larger part.
                 */
                int mi1 = (lo1 + hi1) >>> 1;
                T key = a1[mi1];
                int mi2 = hi2;

                /*
                 * Partition the smaller part.
                 */
                for (int loo = lo2; loo < mi2; ) {
                    int t = (loo + mi2) >>> 1;

                    if (a2[t] == null || a2[t].compareTo(key) < 0) {
                        loo = t + 1;
                    } else {
                        mi2 = t;
                    }
                }

                int d = mi2 - lo2 + mi1 - lo1;

                /*
                 * Merge the right sub-parts in parallel.
                 */
                merger.forkMerger(dst, k + d, a1, mi1, hi1, a2, mi2, hi2);

                /*
                 * Process the sub-left parts.
                 */
                hi1 = mi1;
                hi2 = mi2;
            }
        }

        /*
         * Merge small parts sequentially.
         */
        while (lo1 < hi1 && lo2 < hi2) {
            dst[k++] = (a1[lo1] == null || a1[lo1].compareTo(a2[lo2]) < 0) ? a1[lo1++] : a2[lo2++];
        }
        if (dst != a1 || k < lo1) {
            while (lo1 < hi1) {
                dst[k++] = a1[lo1++];
            }
        }
        if (dst != a2 || k < lo2) {
            while (lo2 < hi2) {
                dst[k++] = a2[lo2++];
            }
        }
    }

// [class]

    /**
     * This class implements parallel sorting.
     */
    private static final class Sorter<T extends Comparable<T>> extends CountedCompleter<Void> {
        private static final long serialVersionUID = 20180818L;
        @SuppressWarnings("serial")
        private final T[] a, b;
        private final int low, size, offset, depth;

        private Sorter(CountedCompleter<?> parent,
                       T[] a, T[] b, int low, int size, int offset, int depth) {
            super(parent);
            this.a = a;
            this.b = b;
            this.low = low;
            this.size = size;
            this.offset = offset;
            this.depth = depth;
        }

        @Override
        public void compute() {
            if (depth < 0) {
                setPendingCount(2);
                int half = size >> 1;
                new Sorter<>(this, b, a, low, half, offset, depth + 1).fork();
                new Sorter<T>(this, b, a, low + half, size - half, offset, depth + 1).compute();
            } else {
                sort(this, a, depth, low, low + size);
            }
            tryComplete();
        }

        @Override
        public final void onCompletion(CountedCompleter<?> caller) {
            if (depth < 0) {
                int mi = low + (size >> 1);
                boolean src = (depth & 1) == 0;

                new Merger<>(null,
                        a,
                        src ? low : low - offset,
                        b,
                        src ? low - offset : low,
                        src ? mi - offset : mi,
                        b,
                        src ? mi - offset : mi,
                        src ? low + size - offset : low + size
                ).invoke();
            }
        }

        private void forkSorter(int depth, int low, int high) {
            addToPendingCount(1);
            T[] a = this.a; // Use local variable for performance
            new Sorter<T>(this, a, b, low, high - low, offset, depth).fork();
        }
    }

    /**
     * This class implements parallel merging.
     */
    private static final class Merger<T extends Comparable<T>> extends CountedCompleter<Void> {
        private static final long serialVersionUID = 20180818L;
        @SuppressWarnings("serial")
        private final T[] dst, a1, a2;
        private final int k, lo1, hi1, lo2, hi2;

        private Merger(CountedCompleter<?> parent, T[] dst, int k,
                       T[] a1, int lo1, int hi1, T[] a2, int lo2, int hi2) {
            super(parent);
            this.dst = dst;
            this.k = k;
            this.a1 = a1;
            this.lo1 = lo1;
            this.hi1 = hi1;
            this.a2 = a2;
            this.lo2 = lo2;
            this.hi2 = hi2;
        }

        @Override
        public final void compute() {
            mergeParts(this, dst, k, a1, lo1, hi1, a2, lo2, hi2);
            propagateCompletion();
        }

        private void forkMerger(T[] dst, int k,
                                T[] a1, int lo1, int hi1, T[] a2, int lo2, int hi2) {
            addToPendingCount(1);
            new Merger<T>(this, dst, k, a1, lo1, hi1, a2, lo2, hi2).fork();
        }
    }

    /**
     * This class implements parallel merging of runs.
     */
    private static final class RunMerger<T extends Comparable<T>> extends RecursiveTask<Object> {
        private static final long serialVersionUID = 20180818L;
        @SuppressWarnings("serial")
        private final T[] a, b;
        private final int[] run;
        private final int offset, aim, lo, hi;

        private RunMerger(T[] a, T[] b, int offset,
                          int aim, int[] run, int lo, int hi) {
            this.a = a;
            this.b = b;
            this.offset = offset;
            this.aim = aim;
            this.run = run;
            this.lo = lo;
            this.hi = hi;
        }

        @Override
        protected final Object compute() {
            return mergeRuns(a, b, offset, aim, true, run, lo, hi);
        }

        private RunMerger<T> forkMe() {
            fork();
            return this;
        }

        private T[] getDestination() {
            join();
            return (T[]) getRawResult();
        }
    }
}
