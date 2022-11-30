package edu.sjsu.matchya;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import java.util.*;
import static com.mongodb.client.model.Filters.eq;

/**
 * The class used to handle the MongoDB connections, data display
 * and Divide and Conquer, and Naive Counting inversions
 * */
public class MyMatchYa {
    /**
    * Main function
    * */
    public static void main(String[] args) {
        // Creates a new instance of MongoDB Client and connect to localhost
        // port 27017.
        MongoClient client = new MongoClient(
                new ServerAddress("localhost", 27017));

        // Gets the mathcyaDB from the MongoDB instance.
        MongoDatabase database = client.getDatabase("matchyaDB");

        // Gets the myMatchyaDB collections from the database.
        MongoCollection<Document> collection = database.getCollection("myMatchyaDB");

        /**
         * if needs to test the connection, comments the following code and the first user's info will be printed out
         * */

//        // Gets a single document or the first entry from this collection.
//        Document document = collection.find().first();
//
//        // Prints out the document.
//        System.out.println(Objects.requireNonNull(document).toJson());

        //System.out.println("------------test comparison of 3 counting inversions methods-------------");

        /*** call the following methods to do the experiment of comparing the running time*/
//        Comparison.testDCC();
//        Comparison.testNaiveC();
//        Comparison.testParallelC();

        System.out.println();

//        // get the username, inversions from the db
//        List<Document> rankingDB = collection.find().projection(Projections.fields(Projections.include(
//                "username"))).into(new ArrayList<Document>());
//        for (Document d : rankingDB) {
//            System.out.println(Objects.requireNonNull(d).toJson());
//        }

        //System.out.println("__________________test read rankings of user from DB__________________");
        // the username and the list of rankings pair
        HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
        map = getRankingArrayFromDB(collection);

        //System.out.println("____________________test inversion list________________");
        // the username and inversion score pair
        HashMap<String, Integer> usrScorePair = computeInversions(map);

        // call the elo algorithm to list 5 matches
        //list document of all usernames and scores
        //get user input ( username of a user within the DB )
        Scanner input = new Scanner(System.in);
        System.out.println("Enter username");

        String userNum = input.nextLine();  // Read user input
        System.out.println("Username is: " + userNum);

        List<Document> eloDB = collection.find().projection(Projections.fields(Projections.include
                ("score","username"),Projections.excludeId())).into(new ArrayList<Document>());
        //document of usernames and scores (paired)
        Document userInput = collection.find((eq("username",userNum))).projection(Projections.fields
                (Projections.include("score"),Projections.excludeId())).first();

        List<String> output = eloFunction( eloDB,userInput ); //call elo function

        //update DB with a closest value connecting it back to the corresponding user
        collection.updateOne((eq("username",userNum)),new Document("$set", new Document("closest", output)));

        // update DB score and inversions field
        updateDBInversionsAndScore(usrScorePair, collection);

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
        int lCount = 0;
        int rCount = 0;
        int mergedCount = 0;
        // if the list has one element then there is no inversions
        if (arr.length == 1) {
            return count;
        }
        // divide the list into two halves
        if (l < r) {
            int mid = (l + r) / 2;
            // left half
            lCount = sortAndCount(arr, l, mid);
            // right half
            rCount = sortAndCount(arr, mid + 1, r);
            mergedCount = mergeAndCount(arr, l, mid, r);
        }
        count = lCount + rCount + mergedCount;
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

        // copy the first half array into A
        for (int i = 0; i < aSize; i++) {
            A[i] = arr[l + i];
        }

        // copy the second half array into B
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

        // once one list is empty, append the remainder of the other list to the arr
        while (currentB < B.length) {
            arr[k] = B[currentB];
            k++;
            currentB++;
        }

        return count;
    }


    /**
     * Read in user rankings from myMatchyaDB and generate and return a map
     * map key: user name
     * map value: users total rankings of five categories
     * */
    public static HashMap<String, ArrayList<Integer>> getRankingArrayFromDB(MongoCollection<Document> collection) {
        HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();

        List<Document> movieDB = collection.find().projection(Projections.fields(Projections.include("_id",
                "username","music", "movie", "hobbies", "shopping", "opinions"))).into(new ArrayList<Document>());

        //int count = 0;
        for (Document d : movieDB) {
            // a list to store the rankings
            ArrayList<Integer> rankingList = new ArrayList<Integer>();
            String id = d.get("_id").toString();
            String name = d.get("username").toString();

            // read in the data from each category from the DB
            Document music = d.get("music", Document.class);
            Document movie = d.get("movie", Document.class);
            Document hobbies = d.get("hobbies", Document.class);
            Document shopping = d.get("shopping", Document.class);
            Document opinions = d.get("opinions", Document.class);

            // the followings append the ranks to the list based on the category
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
            //count++;
        }

//        // debug printing, prints out the username and their 25 rankings
//        for (Map.Entry<String, ArrayList<Integer>> entry : map.entrySet()) {
//            System.out.println(entry.getKey() + " " + Arrays.toString(entry.getValue().toArray()));
//        }

        // debug printing, the size of the map
        //System.out.println(map.size());
        return map;
    }

    /**
     * Compute number of inversions based on the rankings from the map
     * return a map with
     * key: user name;
     * value: the number of inversions
     * */
    public static HashMap<String, Integer> computeInversions(HashMap<String, ArrayList<Integer>> map) {
        // initialize the inversion count
        int inversions = 0;
        // store the user and their inversion score in a map
        HashMap<String, Integer> resMap = new HashMap<>();

        // compute 25 distinct integers based on user's input ranking
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
                // rankings from 6-10 index
                if (k > 4 && k <= 9) {
                    num += 1;
                    tempArr[i] = num + 5;
                    i++;
                }
                // rankings from 11-15 index
                if (k > 9 && k <= 14) {
                    num += 1;
                    tempArr[i] = num + 10;
                    i++;
                }
                // ranking from 16-20 index
                if (k > 14 && k <= 19) {
                    num += 1;
                    tempArr[i] = num + 15;
                    i++;
                }
                // ranking from 21-25 index
                if (k > 19 && k <= 24) {
                    num += 1;
                    tempArr[i] = num + 20;
                    i++;
                }
            }
            // now tempArr contains distinct numbers from 1 to 25
//            System.out.println(entry.getKey() + " " + Arrays.toString(tempArr));

            // compute the inversions use naive counting inversion
            long startDccTime = System.nanoTime();
            inversions = getNaiveCount(tempArr);

            // the following are the tests of computing the inversions using D&C and parallel methods
            // they are commented out since we are using naive for now, we leave them for easy testing and comparison

//            // compute the inversions use D&C counting inversion
//            inversions = sortAndCount(tempArr, 0, tempArr.length - 1);

//            // compute the inversions use parallel counting inversion
//            ParallelCountingInversions pc = new ParallelCountingInversions();
//            inversions = pc.computeCount(tempArr);

            resMap.put(username, inversions);
        }

//        debugging print of users and their inversion score
//        for (Map.Entry<String, Integer> entry : resMap.entrySet()) {
//            System.out.println(entry.getKey() + " " + entry.getValue());
//        }

        //System.out.println(resMap.size());
        return resMap;
    }

    /**
     * update the inversions and score field in the DB
     * */
    public static void updateDBInversionsAndScore(HashMap<String, Integer> map, MongoCollection<Document> collection) {
        for (Map.Entry<String, Integer> entry: map.entrySet()) {
            // user name
            String name = entry.getKey();
            // corresponding score
            int score = entry.getValue();
            collection.updateOne((eq("username",name)),
                    new Document("$set", new Document("inversions", score)));
            collection.updateOne((eq("username",name)), new Document("$set", new Document("score", score)));
        }
    }


    public static List<String> eloFunction(List<Document> eloDB,Document userInput ){

        List<Float> userScores = new ArrayList<Float>(); //to hold the list of user's score

        for (Document myMatchyaDBupdated:eloDB){// for every user and score within the db
            int s = myMatchyaDBupdated.getInteger("score");
            float score = s; //add the score
            userScores.add(score);
            //System.out.println("score " + score); //users original scores before elo application
        }

        //retrieve info on current user's score before elo
        int currentUser =userInput.getInteger("score");
        float currentUserScore = currentUser;
        //System.out.println(Objects.requireNonNull(userInput).toJson()); // prints current user's score before elo

        // Ra and Rb are the two to compare in the Elo algo
        float Ra = currentUserScore, Rb;

        List<Float> ratingList=new ArrayList<Float>();
        List<Float> matchmaking=new ArrayList<Float>();

        //applying elo
        for(Float ratings:userScores){ //retrieve a rating at time within the user scores
            Rb= ratings;
            Float Match=(Probability(Ra,Rb)); //calls the probability part of the elo algorithm
            ratingList.add(Match); //add the result to the list of ratings/results
        }

        for(Float compare:ratingList){ // for every elo score within the rating list
            float math= Math.abs(compare-(float).5); // compare the distance to find the closest to the perfect match
            matchmaking.add(math); //returns distance away from 0.5 ( 0.5 is a perfect match )
        }


        List<Float> temp=new ArrayList<Float>(matchmaking);// creating a temp array to hold he distances from the target user
        Collections.sort(temp); //sort them in order of smallest to biggest
        List<Float> sorted=new ArrayList<Float>();
        for(int i=1; i<=5;i++){ //this is to get the top 5 of the sorted list of distances
            sorted.add(temp.get(i));
        }

        List<String> indexlist= new ArrayList<String>(); //5 resultes of the users that best ( closest) to the signe-in user
        for(int j=0;j<sorted.size();j++){
            for(int i=0;i<matchmaking.size();i++){
                if (Float.compare(matchmaking.get(i),sorted.get(j))==0){
                    if(!indexlist.contains(eloDB.get(i).getString("username"))) {
                        indexlist.add(eloDB.get(i).getString("username"));// retrieve username of the closest match and add to index list
                        break;
                    }
                }
            }
        }
        System.out.println(indexlist);

        return indexlist;

    }


    static float Probability(float rating1, float rating2){ //calculate the probability between two users score
        return 1.0f / (1 + (float) (Math.pow(10, (rating1 - rating2) / 400))); // comparing the scores the result is the new score, with the compared user's score considered
    }

}
