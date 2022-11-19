package edu.sjsu.matchya;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import java.util.*;
import static com.mongodb.client.model.Filters.eq;
public class MyMatchYa {
    public static void main(String[] args) {
        // Creates a new instance of MongoDBClient and connect to localhost
        // port 27017.
        MongoClient client = new MongoClient(
                new ServerAddress("localhost", 27017));

        // Gets the mathcyaDB from the MongoDB instance.
        MongoDatabase database = client.getDatabase("matchyaDB");

        // Gets the myMatchyaDB collections from the database.
        MongoCollection<Document> collection = database.getCollection("myMatchyaDB");

        // Test of querying the first entry from this collection.
        Document document = collection.find().first();

        // Prints out the document.
        System.out.println(Objects.requireNonNull(document).toJson());

        // a test of naive and D&C counting inversions algorithm
        testDCC();
        testNaiveC();
        // generate testing array
        getTestArray();

        System.out.println();

        System.out.println("------------test to get array from the DB-------------");
        Document userInput = collection.find((eq("username", "daeni"))).
                projection(Projections.fields(Projections.include("inversions"))).first();
        System.out.println(Objects.requireNonNull(userInput).toJson());

        // get the username, inversions from the db
        List<Document> rankingDB = collection.find().projection(Projections.fields(Projections.include(
                "username"))).into(new ArrayList<Document>());
        for (Document d : rankingDB) {
            System.out.println(Objects.requireNonNull(d).toJson());
        }

        System.out.println("__________________test read rankings of user from DB__________________");
        // the username and the list of rankings pair
        HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
        map = getRankingArrayFromDB(collection);

        System.out.println("____________________test inversion list________________");
        // the username and inversion score pair
        HashMap<String, Integer> usrScorePair = computeInversions(map);

        // update DB score and inversions field
        updateDBInversionsAndScore(usrScorePair, collection);

    }

    public static void testDCC() {
        //int[] arr = {25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        //int[] arr = {50, 49, 48, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        //int[] arr = {100, 99, 98, 97, 96, 95, 94, 93, 92, 91, 90, 89, 88, 87, 86, 85, 84, 83, 82, 81, 80, 79, 78, 77, 76, 75, 74, 73, 72, 71, 70, 69, 68, 67, 66, 65, 64, 63, 62, 61, 60, 59, 58, 57, 56, 55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        int[] arr = {150, 149, 148, 147, 146, 145, 144, 143, 142, 141, 140, 139, 138, 137, 136, 135, 134, 133, 132, 131, 130, 129, 128, 127, 126, 125, 124, 123, 122, 121, 120, 119, 118, 117, 116, 115, 114, 113, 112, 111, 110, 109, 108, 107, 106, 105, 104, 103, 102, 101, 100, 99, 98, 97, 96, 95, 94, 93, 92, 91, 90, 89, 88, 87, 86, 85, 84, 83, 82, 81, 80, 79, 78, 77, 76, 75, 74, 73, 72, 71, 70, 69, 68, 67, 66, 65, 64, 63, 62, 61, 60, 59, 58, 57, 56, 55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        //int[] arr = {500, 499, 498, 497, 496, 495, 494, 493, 492, 491, 490, 489, 488, 487, 486, 485, 484, 483, 482, 481, 480, 479, 478, 477, 476, 475, 474, 473, 472, 471, 470, 469, 468, 467, 466, 465, 464, 463, 462, 461, 460, 459, 458, 457, 456, 455, 454, 453, 452, 451, 450, 449, 448, 447, 446, 445, 444, 443, 442, 441, 440, 439, 438, 437, 436, 435, 434, 433, 432, 431, 430, 429, 428, 427, 426, 425, 424, 423, 422, 421, 420, 419, 418, 417, 416, 415, 414, 413, 412, 411, 410, 409, 408, 407, 406, 405, 404, 403, 402, 401, 400, 399, 398, 397, 396, 395, 394, 393, 392, 391, 390, 389, 388, 387, 386, 385, 384, 383, 382, 381, 380, 379, 378, 377, 376, 375, 374, 373, 372, 371, 370, 369, 368, 367, 366, 365, 364, 363, 362, 361, 360, 359, 358, 357, 356, 355, 354, 353, 352, 351, 350, 349, 348, 347, 346, 345, 344, 343, 342, 341, 340, 339, 338, 337, 336, 335, 334, 333, 332, 331, 330, 329, 328, 327, 326, 325, 324, 323, 322, 321, 320, 319, 318, 317, 316, 315, 314, 313, 312, 311, 310, 309, 308, 307, 306, 305, 304, 303, 302, 301, 300, 299, 298, 297, 296, 295, 294, 293, 292, 291, 290, 289, 288, 287, 286, 285, 284, 283, 282, 281, 280, 279, 278, 277, 276, 275, 274, 273, 272, 271, 270, 269, 268, 267, 266, 265, 264, 263, 262, 261, 260, 259, 258, 257, 256, 255, 254, 253, 252, 251, 250, 249, 248, 247, 246, 245, 244, 243, 242, 241, 240, 239, 238, 237, 236, 235, 234, 233, 232, 231, 230, 229, 228, 227, 226, 225, 224, 223, 222, 221, 220, 219, 218, 217, 216, 215, 214, 213, 212, 211, 210, 209, 208, 207, 206, 205, 204, 203, 202, 201, 200, 199, 198, 197, 196, 195, 194, 193, 192, 191, 190, 189, 188, 187, 186, 185, 184, 183, 182, 181, 180, 179, 178, 177, 176, 175, 174, 173, 172, 171, 170, 169, 168, 167, 166, 165, 164, 163, 162, 161, 160, 159, 158, 157, 156, 155, 154, 153, 152, 151, 150, 149, 148, 147, 146, 145, 144, 143, 142, 141, 140, 139, 138, 137, 136, 135, 134, 133, 132, 131, 130, 129, 128, 127, 126, 125, 124, 123, 122, 121, 120, 119, 118, 117, 116, 115, 114, 113, 112, 111, 110, 109, 108, 107, 106, 105, 104, 103, 102, 101, 100, 99, 98, 97, 96, 95, 94, 93, 92, 91, 90, 89, 88, 87, 86, 85, 84, 83, 82, 81, 80, 79, 78, 77, 76, 75, 74, 73, 72, 71, 70, 69, 68, 67, 66, 65, 64, 63, 62, 61, 60, 59, 58, 57, 56, 55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        System.out.println("-------------------------------------");
        long startDccTime = System.nanoTime();
        System.out.println("Divide and conquer counting inversions: " + sortAndCount(arr, 0, arr.length - 1));
        long endDccTime = System.nanoTime();
        System.out.println("The time elapsed for divide and conquer counting inversions is: "
                + (endDccTime - startDccTime));
    }

    public static void testNaiveC() {
        //int[] arr = {25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        //int[] arr = {50, 49, 48, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        //int[] arr = {100, 99, 98, 97, 96, 95, 94, 93, 92, 91, 90, 89, 88, 87, 86, 85, 84, 83, 82, 81, 80, 79, 78, 77, 76, 75, 74, 73, 72, 71, 70, 69, 68, 67, 66, 65, 64, 63, 62, 61, 60, 59, 58, 57, 56, 55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        int[] arr = {150, 149, 148, 147, 146, 145, 144, 143, 142, 141, 140, 139, 138, 137, 136, 135, 134, 133, 132, 131, 130, 129, 128, 127, 126, 125, 124, 123, 122, 121, 120, 119, 118, 117, 116, 115, 114, 113, 112, 111, 110, 109, 108, 107, 106, 105, 104, 103, 102, 101, 100, 99, 98, 97, 96, 95, 94, 93, 92, 91, 90, 89, 88, 87, 86, 85, 84, 83, 82, 81, 80, 79, 78, 77, 76, 75, 74, 73, 72, 71, 70, 69, 68, 67, 66, 65, 64, 63, 62, 61, 60, 59, 58, 57, 56, 55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        //int[] arr = {500, 499, 498, 497, 496, 495, 494, 493, 492, 491, 490, 489, 488, 487, 486, 485, 484, 483, 482, 481, 480, 479, 478, 477, 476, 475, 474, 473, 472, 471, 470, 469, 468, 467, 466, 465, 464, 463, 462, 461, 460, 459, 458, 457, 456, 455, 454, 453, 452, 451, 450, 449, 448, 447, 446, 445, 444, 443, 442, 441, 440, 439, 438, 437, 436, 435, 434, 433, 432, 431, 430, 429, 428, 427, 426, 425, 424, 423, 422, 421, 420, 419, 418, 417, 416, 415, 414, 413, 412, 411, 410, 409, 408, 407, 406, 405, 404, 403, 402, 401, 400, 399, 398, 397, 396, 395, 394, 393, 392, 391, 390, 389, 388, 387, 386, 385, 384, 383, 382, 381, 380, 379, 378, 377, 376, 375, 374, 373, 372, 371, 370, 369, 368, 367, 366, 365, 364, 363, 362, 361, 360, 359, 358, 357, 356, 355, 354, 353, 352, 351, 350, 349, 348, 347, 346, 345, 344, 343, 342, 341, 340, 339, 338, 337, 336, 335, 334, 333, 332, 331, 330, 329, 328, 327, 326, 325, 324, 323, 322, 321, 320, 319, 318, 317, 316, 315, 314, 313, 312, 311, 310, 309, 308, 307, 306, 305, 304, 303, 302, 301, 300, 299, 298, 297, 296, 295, 294, 293, 292, 291, 290, 289, 288, 287, 286, 285, 284, 283, 282, 281, 280, 279, 278, 277, 276, 275, 274, 273, 272, 271, 270, 269, 268, 267, 266, 265, 264, 263, 262, 261, 260, 259, 258, 257, 256, 255, 254, 253, 252, 251, 250, 249, 248, 247, 246, 245, 244, 243, 242, 241, 240, 239, 238, 237, 236, 235, 234, 233, 232, 231, 230, 229, 228, 227, 226, 225, 224, 223, 222, 221, 220, 219, 218, 217, 216, 215, 214, 213, 212, 211, 210, 209, 208, 207, 206, 205, 204, 203, 202, 201, 200, 199, 198, 197, 196, 195, 194, 193, 192, 191, 190, 189, 188, 187, 186, 185, 184, 183, 182, 181, 180, 179, 178, 177, 176, 175, 174, 173, 172, 171, 170, 169, 168, 167, 166, 165, 164, 163, 162, 161, 160, 159, 158, 157, 156, 155, 154, 153, 152, 151, 150, 149, 148, 147, 146, 145, 144, 143, 142, 141, 140, 139, 138, 137, 136, 135, 134, 133, 132, 131, 130, 129, 128, 127, 126, 125, 124, 123, 122, 121, 120, 119, 118, 117, 116, 115, 114, 113, 112, 111, 110, 109, 108, 107, 106, 105, 104, 103, 102, 101, 100, 99, 98, 97, 96, 95, 94, 93, 92, 91, 90, 89, 88, 87, 86, 85, 84, 83, 82, 81, 80, 79, 78, 77, 76, 75, 74, 73, 72, 71, 70, 69, 68, 67, 66, 65, 64, 63, 62, 61, 60, 59, 58, 57, 56, 55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        System.out.println("-------------------------------------");
        long startNCTime = System.nanoTime();
        System.out.println("Naive approach counting inversions: " + getNaiveCount(arr));
        long endNCTime = System.nanoTime();
        System.out.println("The time elapsed naive counting inversions is: "
                + (endNCTime - startNCTime));
    }

    /**
     * Naive Approach to count the inversions in an array
     * loop through the array and check the rest number with the current number
     * if the current number is larger, increment the counts
     * time complexity: O(n^2)
     * */

    public static int getNaiveCount(int[] arr) {
        int count = 0;
        int len = arr.length;
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j < len; j++) {
                if (arr[i] > arr[j]) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Counting inversions with divide and conquer to count the inversions
     * in each half of the given array
     * time complexity: O(nlgn)
     * */

    public static int sortAndCount(int[] arr, int l, int r) {
        int count = 0;
        // if the list has one element then there is no inversions
        if (arr.length == 1) {
            return count;
        }
        // divide the list into two halves
        if (l < r) {
            int mid = (l + r) / 2;
            // left half
            count += sortAndCount(arr, l, mid);
            // right half
            count += sortAndCount(arr, mid + 1, r);
            count += mergeAndCount(arr, l, mid, r);
        }
        return count;
    }

    /**
     * Counting inversions crossing the whole array
     * time complexity: O(n)
     * */
    public static int mergeAndCount(int[] arr, int l, int mid, int r){
        // maintain a variable count for the number of inversions, initialized to 0
        int count = 0;

        int aSize = mid - l + 1;
        int bSize = r - mid;

        // create temp arrays
        int[] A = new int[aSize];
        int[] B = new int[bSize];

        // copy the first half and the second half array into A and B
        for (int i = 0; i < aSize; i++) {
            A[i] = arr[l + i];
        }

        for (int j = 0; j < bSize; j++) {
            B[j] = arr[mid + 1 + j];
        }

        // maintain a variable current pointer into each list, initialized to point to front number
        int currentA = 0;
        int currentB = 0;
        int k = l;

        // while both lists are nonempty
        while ((currentA < A.length) && (currentB < B.length)) {
            // let ai and bj be the elements pointed to by the current pointer
            int ai = A[currentA];
            int bj = B[currentB];

            // if bj is the smaller element, increment count
            if (ai <= bj) {
                arr[k] = A[currentA];
                k++;
                currentA++;
            } else {
                // increment the count by the number of elements remaining in A
                count += mid - currentA + 1 - l;

                arr[k] = B[currentB];
                k++;
                currentB++;
            }
        }

        // once one list is empty, append the remainder of the other list to the arr
        while (currentA < A.length) {
            arr[k] = A[currentA];
            k++;
            currentA++;
        }
        while (currentB < B.length) {
            arr[k] = B[currentB];
            k++;
            currentB++;
        }

        return count;
    }

    /**
     * Print an array used for testing the comparison of naive and D&C counting inversions
     * */
    public static void getTestArray() {
        for (int i = 150; i >= 1; i--) {
            System.out.print(i + ", ");
        }
    }

    /**
     * Read in user rankings from myMatchyaDB as a map
     * map key: user name
     * map value: users total rankings of five categories
     * */
    public static HashMap<String, ArrayList<Integer>> getRankingArrayFromDB(MongoCollection<Document> collection) {
        HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();

        List<Document> movieDB = collection.find().projection(Projections.fields(Projections.include("_id",
                "username","music", "movie", "hobbies", "shopping", "opinions"))).into(new ArrayList<Document>());

        int count = 0;
        for (Document d : movieDB) {
            ArrayList<Integer> rankingList = new ArrayList<Integer>();
            String id = d.get("_id").toString();
            String name = d.get("username").toString();

            //Object s = d.get("movie");
            // int num = (Integer) d.get("movie", Document.class).get("Horror");
            //int num = Integer.valueOf(n);
            //System.out.println(Objects.requireNonNull(d).toJson());
            Document music = d.get("music", Document.class);
            Document movie = d.get("movie", Document.class);
            Document hobbies = d.get("hobbies", Document.class);
            Document shopping = d.get("shopping", Document.class);
            Document opinions = d.get("opinions", Document.class);

            rankingList.add(music.getInteger("slow songs"));
            rankingList.add(music.getInteger("country"));
            rankingList.add(music.getInteger("pop"));
            rankingList.add(music.getInteger("rock"));
            rankingList.add(music.getInteger("jazz"));

            rankingList.add(movie.getInteger("Horror"));
            rankingList.add(movie.getInteger("Comedy"));
            rankingList.add(movie.getInteger("Sci-fi"));
            rankingList.add(movie.getInteger("Fantasy"));
            rankingList.add(movie.getInteger("Action"));

            rankingList.add(hobbies.getInteger("Mathmetics"));
            rankingList.add(hobbies.getInteger("History"));
            rankingList.add(hobbies.getInteger("Reading"));
            rankingList.add(hobbies.getInteger("Dancing"));
            rankingList.add(hobbies.getInteger("Outdoor activity"));

            rankingList.add(shopping.getInteger("large shopping centers"));
            rankingList.add(shopping.getInteger("Spending on looks"));
            rankingList.add(shopping.getInteger("Branded clothing"));
            rankingList.add(shopping.getInteger("fragual with money"));
            rankingList.add(shopping.getInteger("Only what you need"));

            rankingList.add(opinions.getInteger("get angry very easily"));
            rankingList.add(opinions.getInteger("cry when feel down"));
            rankingList.add(opinions.getInteger("always full of life and energy"));
            rankingList.add(opinions.getInteger("Life struggles"));
            rankingList.add(opinions.getInteger("Happiness in life"));

            //System.out.print(d.get("movie", Document.class).get("Comedy"));
            //System.out.println(num);
            map.put(name, rankingList);
            count++;
        }

        for (Map.Entry<String,ArrayList<Integer>> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " " + Arrays.toString(entry.getValue().toArray()));
        }

        System.out.println(map.size());
        return map;
    }

    /**
     * Compute number of inversions based on the rankings from the map
     * return a map with
     * key: user name;
     * value: the number of inversions
     * */
    public static HashMap<String, Integer> computeInversions(HashMap<String, ArrayList<Integer>> map) {
        int inversions = 0;
        HashMap<String, Integer> resMap = new HashMap<>();

        for (Map.Entry<String,ArrayList<Integer>> entry : map.entrySet()) {
            String username = entry.getKey();
            // 5 categories has a total 25 rankings
            int[] tempArr = new int[25];
            int i = 0;
            ArrayList<Integer> list = entry.getValue();
            for (int k = 0; k < list.size(); k++) {
                int num = list.get(k);
                // first 5 rankings in the music category
                if (k <= 4) {
                    num += 1;
                    tempArr[i] = num;
                    i++;
                }
                if (k > 4 && k <= 9) {
                    num += 1;
                    tempArr[i] = num + 5;
                    i++;
                }
                if (k > 9 && k <= 14) {
                    num += 1;
                    tempArr[i] = num + 10;
                    i++;
                }
                if (k > 14 && k <= 19) {
                    num += 1;
                    tempArr[i] = num + 15;
                    i++;
                }
                if (k > 19 && k <= 24) {
                    num += 1;
                    tempArr[i] = num + 20;
                    i++;
                }
            }
            // now tempArr contains distinct numbers from 1 to 25
            System.out.println(entry.getKey() + " " + Arrays.toString(tempArr));
            // compute the inversions use D&C counting inversion
            inversions = sortAndCount(tempArr, 0, tempArr.length - 1);

            resMap.put(username, inversions);
        }

        for (Map.Entry<String, Integer> entry : resMap.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }

        //System.out.println(resMap.size());
        return resMap;
    }

    /**
     * update the inversions and score field in the DB
     * */
    public static void updateDBInversionsAndScore(HashMap<String, Integer> map, MongoCollection<Document> collection) {
        for (Map.Entry<String, Integer> entry: map.entrySet()) {
            String name = entry.getKey();
            int score = entry.getValue();
            collection.updateOne((eq("username",name)),
                    new Document("$set", new Document("inversions", score)));
            collection.updateOne((eq("username",name)), new Document("$set", new Document("score", score)));
        }
    }

}
