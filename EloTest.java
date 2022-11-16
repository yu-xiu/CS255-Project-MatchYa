package org.example;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import java.lang.Math;
import java.util.*;

public class EloTest {

    static float Probability(float rating1,
                             float rating2)
    {
        return 1.0f * 1.0f / (1 + 1.0f *
                (float)(Math.pow(10, 1.0f *
                        (rating1 - rating2) / 400)));
    }



    public static void main (String[] args)
    {
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Enter username");

        String userNum = myObj.nextLine();  // Read user input
        System.out.println("Username is: " + userNum);

        MongoClient client = new MongoClient(
                new ServerAddress("localhost", 27017));

        // Gets the peopledb from the MongoDB instance.
        MongoDatabase database = client.getDatabase("MatchYa");

        // Gets the persons collections from the database.
        MongoCollection<Document> collection = database.getCollection("eloTest");

        List<Document> eloDB = collection.find().projection(Projections.fields(Projections.include("score ","users"),Projections.excludeId())).into(new ArrayList<>());
        List<Float> userScores = new ArrayList<Float>();
        for (Document elotest:eloDB){
            String s= elotest.getString("score ");
            float score= Float.parseFloat(s);
            userScores.add(score);
            System.out.println(score);
        }
        Document userInput = collection.find((eq("users",userNum))).projection(Projections.fields(Projections.include("score "),Projections.excludeId())).first();
        String user1 =userInput.getString("score ");
        float user1Score=Float.parseFloat(user1);
        System.out.println(Objects.requireNonNull(userInput).toJson());

       // FindIterable<Document> scores = collection.find().projection(Projections.include("score"));
        //System.out.println(Objects.requireNonNull(scores));



        // Ra and Rb are current ELO ratings

        float Ra = user1Score, Rb;


        List<Float> ratingList=new ArrayList<Float>();
        List<Float> matchmaking=new ArrayList<Float>();
        for(Float ratings:userScores){
            Rb= ratings;
            //EloRating(Ra, Rb, K, d);
            Float Match=(Probability(Ra,Rb));
            ratingList.add(Match);
        }
        //Collections.sort(ratingList);
        System.out.println(ratingList);
        for(Float compare:ratingList){
            float math= Math.abs(compare-(float).5);
            matchmaking.add(math);
        }
        List<Float> temp=new ArrayList<Float>(matchmaking);
        Collections.sort(temp);
        //System.out.println(matchmaking);
        List<Float> sorted=new ArrayList<Float>();
        for(int i=1; i<=5;i++){
            sorted.add(temp.get(i));
        }
        //.out.println("first"+ sorted.get(0));
        List<String> indexlist= new ArrayList<String>();
        for(int j=0;j<sorted.size();j++){
            for(int i=0;i<matchmaking.size();i++){
                if (Float.compare(matchmaking.get(i),sorted.get(j))==0){
                    //.out.println("hello"+ matchmaking.get(i));
                    indexlist.add(eloDB.get(i).getString("users"));
                    //indexlist.add(i);
                    break;
                }
            }
        }
        System.out.println(indexlist);

        collection.updateOne((eq("users",userNum)),new Document("$set", new Document("closest", indexlist)));
        ;
        System.out.println(Probability(Ra,(float)138.5));
        //System.out.println(eloDB.get(3).getString("users"));

    }

}
