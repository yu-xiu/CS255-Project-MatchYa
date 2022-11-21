package edu.sjsu.matchya;

public class Comparison {

    public static void testDCC() {
        //int[] arr = getTestArray();
        int[] arr = {25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        System.out.println("-------------------------------------");
        long startDccTime = System.nanoTime();
        System.out.println("Divide and conquer counting inversions: " + MyMatchYa.sortAndCount(arr, 0, arr.length - 1));
        long endDccTime = System.nanoTime();
        System.out.println("The time elapsed for divide and conquer counting inversions is: "
                + (endDccTime - startDccTime));
    }

    public static void testNaiveC() {
        //int[] arr = getTestArray();
        int[] arr = {25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        System.out.println("-------------------------------------");
        long startNCTime = System.nanoTime();
        System.out.println("Naive approach counting inversions: " + MyMatchYa.getNaiveCount(arr));
        long endNCTime = System.nanoTime();
        System.out.println("The time elapsed for naive counting inversions is: "
                + (endNCTime - startNCTime));
    }

    public static void testParallelC() {
        //int[] arr = getTestArray();
        int[] arr = {25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        ParallelCountingInversions pc = new ParallelCountingInversions();
        System.out.println("-------------------------------------");
        long startNCTime = System.nanoTime();
        System.out.println("Parallel counting inversions: " + pc.computeCount(arr));
        long endNCTime = System.nanoTime();
        System.out.println("The time elapsed for parallel counting inversions is: "
                + (endNCTime - startNCTime));
    }

    /**
     * Print an array used for testing the comparison of naive and D&C counting inversions
     * */
    public static int[] getTestArray() {
        int[] array = new int[10000000];
        int j = 0;
        for (int i = 10000000; i >= 1; i--) {
            array[j] = i;
            j++;
        }
        return array;
    }

}
