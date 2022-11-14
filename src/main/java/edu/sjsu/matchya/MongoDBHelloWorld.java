package edu.sjsu.matchya;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Objects;

public class MongoDBHelloWorld {
    public static void main(String[] args) {
        // Creates a new instance of MongoDBClient and connect to localhost
        // port 27017.
        MongoClient client = new MongoClient(
                new ServerAddress("localhost", 27017));

        // Gets the mathcyaDB from the MongoDB instance.
        MongoDatabase database = client.getDatabase("matchyaDB");

        // Gets the matchyadb collections from the database.
        MongoCollection<Document> collection = database.getCollection("matchyadb");

        // Gets a single document or the first entry from this collection.
        Document document = collection.find().first();

        // Prints out the document.
        System.out.println(Objects.requireNonNull(document).toJson());
    }
}