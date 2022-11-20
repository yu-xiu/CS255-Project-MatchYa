package edu.sjsu.matchya;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelCountingInversions {
    public int computeCount(int[] array) {
        SortTask mainTask = new SortTask(array, 0, array.length - 1);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(mainTask);
        int totalCount = mainTask.getCount();
        return totalCount;
    }

    private static class SortTask extends RecursiveAction {
        private int[] array;
        private int left;
        private int right;
        private int count;

        public SortTask(int[] array, int left, int right) {
            this.array = array;
            this.left = left;
            this.right = right;
            this.count = 0;
            //System.out.println("create sort task: left " + left + " right" + right);
        }

        public int getCount() {
            return this.count;
        }

        @Override
        protected void compute() {
            // if the list has one element then there is no inversions
            if (left < right) {
                int mid = (left + right) / 2;

                // left half
                SortTask firstTask = new SortTask(array, left, mid);
                // right half
                SortTask secondTask = new SortTask(array, mid+1, right);

                invokeAll(firstTask, secondTask);

                // merge
                int lCount = firstTask.getCount();
                int rCount = secondTask.getCount();
                int mergedCount = MyMatchYa.mergeAndCount(array, left, mid, right) ;
                this.count = lCount + rCount + mergedCount;
            }
        }
    }
}
