package edu.sjsu.matchya;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * A class to implement the parallel counting inversions
 * */
public class ParallelCountingInversions {
    /**
     * Compute the results of the inversions
     * */
    public int computeCount(int[] array) {
        // the main task
        CountTask mainTask = new CountTask(array, 0, array.length - 1);
        // a ForkJoinPool object
        ForkJoinPool pool = new ForkJoinPool();
        // invoke the main task
        pool.invoke(mainTask);
        // the result
        int totalCount = mainTask.getCount();
        return totalCount;
    }

    /**
     * The counting task that extends the Java RecursiveAction class
     * to implement the multithreading functionality in order to count the inversions in parallel
     * */
    private static class CountTask extends RecursiveAction {
        private int[] array;
        private int left;
        private int right;
        private int count;


        /**
         * construct the count task
         * taking the array and left and right pointer
         * */
        public CountTask(int[] array, int left, int right) {
            this.array = array;
            this.left = left;
            this.right = right;
            this.count = 0;
            //System.out.println("create sort task: left " + left + " right" + right);
        }

        /**
        *  A Getter API to get the count
        * */
        public int getCount() {
            return this.count;
        }

        @Override
        /**
         * to compute the count
         * */
        protected void compute() {
            // if the list has one element then there is no inversions
            if (left < right) {
                // mid pointer
                int mid = (left + right) / 2;

                // left half task or thread
                CountTask firstTask = new CountTask(array, left, mid);
                // right half task or thread
                CountTask secondTask = new CountTask(array, mid+1, right);

                // invoke the tasks
                invokeAll(firstTask, secondTask);

                // get the counts of each task
                int lCount = firstTask.getCount();
                int rCount = secondTask.getCount();
                // sync the result
                int mergedCount = MyMatchYa.mergeAndCount(array, left, mid, right) ;
                // final result
                this.count = lCount + rCount + mergedCount;
            }
        }
    }
}
