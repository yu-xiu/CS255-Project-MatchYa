# CS255-Project-MatchYa
The CS255 Group Project - Tiffant and Rebecca
## Introduction
MatchYa is a dating APP that uses Counting Inversions algorihtm and Elo algorihtm to recommend the five best matches list of users to the current user.
The Counting Inversions algorihtm is implemented with three methods: the Naive, Divide and Conquer, and the Multithreaded solutions.
We aslo have a Comparison experiment to compare the running time of three solutions using various size of input arrays.
## Pre-requisite
Install and connect to MongoDB (may use a Maven project)<br />
The installation and how to set up the mogonDB can be referenced in our Sprint 1
```
https://github.com/yu-xiu/CS255-Project-MatchYa/blob/master/Sprint%201%20Report.pdf
```
Create a MongoDB database named matchyaDB and add myMatchyaDB.json to matchyaDB<br />
This is the myMatchyaDB.json file
```
https://github.com/yu-xiu/CS255-Project-MatchYa/blob/master/myMatchyaDB.json
```
Start the connection to MongoDB <br />
Mac termial:
```
brew service list
```
when seeing started, open a new terminal and type:
```
mongosh
```
## Project Architecture
```
src
  |_ main
    |_java
      |_edu.sjsu.matchya
        |_ App
        |_ Comparison
        |_ MyMacthYa (main)
        |_ ParallelCountingInversions
```
### Run MyMacthYa
MyMacthYa contains the main function<br />
