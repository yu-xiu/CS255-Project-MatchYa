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

        List<Float> userScores = new ArrayList<Float>();

        for (Document myMatchyaDBupdated:eloDB){// for every user and score within the db
            int s = myMatchyaDBupdated.getInteger("score");
            float score = s;
            userScores.add(score);
            //System.out.println("score " + score); //users original scores before elo application
        }

        //retrieve info on current user's score before elo
        int currentUser =userInput.getInteger("score");
        float currentUserScore = currentUser;
        //System.out.println(Objects.requireNonNull(userInput).toJson()); // prints current user's score before elo

        // Ra and Rb are current ELO ratings
        float Ra = currentUserScore, Rb;

        List<Float> ratingList=new ArrayList<Float>();
        List<Float> matchmaking=new ArrayList<Float>();

        //applying elo
        for(Float ratings:userScores){
            Rb= ratings;
            Float Match=(Probability(Ra,Rb));
            ratingList.add(Match);
        }

        for(Float compare:ratingList){
            float math= Math.abs(compare-(float).5);
            matchmaking.add(math);
        }


        List<Float> temp=new ArrayList<Float>(matchmaking);
        Collections.sort(temp);
        System.out.println(temp);
        List<Float> sorted=new ArrayList<Float>();
        for(int i=1; i<=5;i++){
            sorted.add(temp.get(i));
        }

        List<String> indexlist= new ArrayList<String>();
        for(int j=0;j<sorted.size();j++){
            for(int i=0;i<matchmaking.size();i++){
                if (Float.compare(matchmaking.get(i),sorted.get(j))==0){
                    if(!indexlist.contains(eloDB.get(i).getString("username"))) {
                        indexlist.add(eloDB.get(i).getString("username"));
                        break;
                    }
                }
            }
        }
        System.out.println(indexlist);

        return indexlist;

    }

    static float Probability(float rating1, float rating2){
        return 1.0f * 1.0f / (1 + 1.0f *
                (float)(Math.pow(10, 1.0f *
                        (rating1 - rating2) / 400)));
    }



    public static void main (String[] args)
    {
       // this may need to be removed / redone 
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

        //test to see if i actually got access to db
        //Document document = collection.find().first();
        //System.out.println(Objects.requireNonNull(document).toJson());
        //---------------------------------------------------------------

        List<Document> eloDB = collection.find().projection(Projections.fields(Projections.include("score","username"),Projections.excludeId())).into(new ArrayList<>()); // this stays
        Document userInput = collection.find((eq("username",userNum))).projection(Projections.fields(Projections.include("score"),Projections.excludeId())).first(); // this stays

        List<String> output = eloFunction( eloDB,userInput );

        collection.updateOne((eq("username",userNum)),new Document("$set", new Document("closest", output)));

        ////System.out.println(Probability(Ra,(float)138.5)); this is for the like system, 138 is Ra's score divided by 2
        ////System.out.println(eloDB.get(3).getString("users"));

    }

}
