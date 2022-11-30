package org.example;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class eloTest2 {


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



    public static void main (String[] args)
    {
        //get user input ( username of a user within the DB )
        Scanner input = new Scanner(System.in);
        System.out.println("Enter username");

        String userNum = input.nextLine();  // Read user input
        System.out.println("Username is: " + userNum);

        //-------------------------------------------------

        MongoClient client = new MongoClient(
                new ServerAddress("localhost", 27017));

        // Gets the db from the MongoDB instance.
        MongoDatabase database = client.getDatabase("MatchYa");

        // Gets the collection from the database.
        MongoCollection<Document> collection = database.getCollection("myMatchyaDBupdated");

        //access a single 2 specific columns: scores and username
        List<Document> eloDB = collection.find().projection(Projections.fields(Projections.include("score","username"),Projections.excludeId())).into(new ArrayList<>()); //list document of all usernames and scores
        Document userInput = collection.find((eq("username",userNum))).projection(Projections.fields(Projections.include("score"),Projections.excludeId())).first(); //document of usernames and scores (paired)

        List<String> output = eloFunction( eloDB,userInput ); //call elo function

        //update DB with a closest value connecting it back to the corresponding user
        collection.updateOne((eq("username",userNum)),new Document("$set", new Document("closest", output)));

    }

}
