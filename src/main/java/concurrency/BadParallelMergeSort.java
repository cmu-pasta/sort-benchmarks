package concurrency;

import cmu.pasta.cdiff.IndexedThread;

/** modification of the other mergesort*/
public class BadParallelMergeSort {
  static final Object MSLOCK = "MERGESORTLOCK";

  /**
   * This method implements the Generic Merge Sort
   *
   * @param unsorted the array which should be sorted
   * @param <T> Comparable class
   * @return sorted array
   */
  public <T extends Comparable<T>> T[] sort(T[] unsorted) {
    try {
      doSort(unsorted, 0, unsorted.length - 1);
      return unsorted;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * @param arr The array to be sorted
   * @param left The first index of the array
   * @param right The last index of the array Recursively sorts the array in increasing order
   */
  private static <T extends Comparable<T>> void doSort(T[] arr, int left, int right) throws InterruptedException {
    if (left < right) {
      int mid = left + (right - left) / 2;

      IndexedThread t1 = new IndexedThread(() -> {
        try {
          doSort(arr, left, mid);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });
      IndexedThread t2 = new IndexedThread(() -> {
        try {
          doSort(arr, mid + 1, right);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });
      t1.start();
      t2.start();
      t1.newJoin();
      t2.newJoin();
      merge(arr, left, mid, right);
    }
  }

  /**
   * This method implements the merge step of the merge sort
   *
   * @param arr The array to be sorted
   * @param left The first index of the array
   * @param mid The middle index of the array
   * @param right The last index of the array merges two parts of an array in increasing order
   */
  private static <T extends Comparable<T>> void merge(T[] arr, int left, int mid, int right) {
    T[] temp = (T[]) new Comparable[arr.length];
    synchronized (MSLOCK) {
      //copy whole array instead of just the half we're working with
      System.arraycopy(arr, 0, temp, 0, arr.length);
    }
    int i = left;
    int j = mid + 1;
    int k = 0;

    while (i <= mid && j <= right) {
        if (arr[i].compareTo(arr[j]) <= 0) {
          temp[k++] = arr[i++];
        } else {
          temp[k++] = arr[j++];
        }
    }

    while (i <= mid) {
        temp[k++] = arr[i++];
    }

    while (j <= right) {
        temp[k++] = arr[j++];
    }

    synchronized (MSLOCK) {
      //bug: copying back over the old half-we-weren't-working-with
      System.arraycopy(temp, 0, arr, 0, arr.length);
    }
  }
}
