package sort;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.*;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(JUnitQuickcheck.class)
public class PropertyTest {
    SortAlgorithm quickSort = new QuickSort();
    SortAlgorithm mergeSort = new MergeSort();

    @Test
    public void testQuickSort() {
        Integer[] input = {3, 1, 2};
        Integer[] expected = {1, 2, 3};
        var output = quickSort.sort(input);
        assertArrayEquals(expected, output);
    }


    @Property
    public void testQuickSortOrdering(List<Integer> input) {
        var output = quickSort.sort(input);
        for (int i = 1; i < output.size(); i++) {
            assertTrue(output.get(i-1) <= output.get(i));
        }
    }

    @Property
    public void testQuickSortOrdering1(List<Integer> input) {
        System.out.println("Input class: " + input.getClass());
        System.out.println("Input values: " + input.toString());
        var output = quickSort.sort(input);
        System.out.println("Output values: " + output.toString());
        for (int i = 1; i < output.size(); i++) {
            assertTrue(output.get(i-1) <= output.get(i));
        }
        System.out.println("Sorted! \n");
    }

    @Property
    public void testQuickSortOrdering2(@From(MyIntListGen.class) List<Integer> input) {
        System.out.println("Input class: " + input.getClass());
        System.out.println("Input values: " + input.toString());
        var output = quickSort.sort(input);
        System.out.println("Output values: " + output.toString());
        for (int i = 1; i < output.size(); i++) {
            assertTrue(output.get(i-1) <= output.get(i));
        }
        System.out.println("Sorted! \n");
    }

    @Property
    public void testQuickSortOrdering3(List<@InRange(minInt=0, maxInt=10) Integer> input) {
        assumeTrue(new HashSet<>(input).size() < input.size());
        System.out.println("Input class: " + input.getClass());
        System.out.println("Input values: " + input.toString());
        var output = quickSort.sort(input);
        System.out.println("Output values: " + output.toString());
        for (int i = 1; i < output.size(); i++) {
            assertTrue(output.get(i-1) <= output.get(i));
        }
        System.out.println("Sorted! \n");
    }

    @Property
    public void testQuickSortMergeSortEqual(List<Integer> input) {
        var output1 = mergeSort.sort(input);
        var output2 = quickSort.sort(input);
        assertEquals(output1, output2);
    }

    public static class MyIntListGen extends Generator<List<Integer>> {
        @Override
        public List<Integer> generate(SourceOfRandomness random, GenerationStatus generationStatus) {
            int size = random.nextInt(20);
            List<Integer> result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                int x = (i > 0 && random.nextBoolean()) ?         // flip a coin for:
                        result.get(random.nextInt(i)) :           // a previous value
                        random.nextInt(-10, 10);                  // a new value, or
                result.add(x);
            }
            return result;
        }

        public MyIntListGen(Class<List<Integer>> type) {
            super(type);
        }

    }
}
