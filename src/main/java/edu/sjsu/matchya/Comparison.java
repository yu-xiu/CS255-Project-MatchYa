package edu.sjsu.matchya;

/**
 * The comparison class is used to experiment the running time of the three counting inversions methods
 * Naive, Divide and Conquer, and multithreading counting inversions
 * */
public class Comparison {

    /**
     * A D&C Counting inversions testing method used to compare the running times
     * */
    public static void testDCC() {
        // an array used to test the D&C counting inversions
        int[] arr = getTestArray();

        System.out.println("-------------------------------------");
        // count the elapsed time and print the result and time out
        long startDccTime = System.nanoTime();
        System.out.println("Divide and conquer counting inversions: " +
                MyMatchYa.sortAndCount(arr, 0, arr.length - 1));
        long endDccTime = System.nanoTime();
        System.out.println("The time elapsed for divide and conquer counting inversions is: "
                + (endDccTime - startDccTime));
    }

    /**
     * A Naive counting inversions testing method used to examine the running times
     * */
    public static void testNaiveC() {
        // an array used to test the naive counting inversion
        int[] arr = getTestArray();

        System.out.println("-------------------------------------");
        // count the elapsed time and print the result and time out
        long startNCTime = System.nanoTime();
        System.out.println("Naive approach counting inversions: " + MyMatchYa.getNaiveCount(arr));
        long endNCTime = System.nanoTime();
        System.out.println("The time elapsed for naive counting inversions is: "
                + (endNCTime - startNCTime));
    }

    /**
     * A Parallel or multithreading counting inversions running time tester
     * */
    public static void testParallelC() {
        // an array used to test the parallel counting inversions
        int[] arr = getTestArray();

        // initialize the parallel counting inversion object
        ParallelCountingInversions pc = new ParallelCountingInversions();

        System.out.println("-------------------------------------");
        // count the elapsed time and print the result and time out
        long startNCTime = System.nanoTime();
        System.out.println("Parallel counting inversions: " + pc.computeCount(arr));
        long endNCTime = System.nanoTime();
        System.out.println("The time elapsed for parallel counting inversions is: "
                + (endNCTime - startNCTime));
    }

    /**
     * Generating an array used for testing the comparison of naive and D&C and parallel counting inversions
     * The size of the array may vary
     * */
    public static int[] getTestArray() {
        // an array with vary size used to generate a testing array
        int[] array = new int[50];
        // index
        int j = 0;
        for (int i = 50; i >= 1; i--) {
            array[j] = i;
            j++;
        }
        return array;
    }

}
