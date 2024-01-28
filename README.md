# DreamCase
Dream Games Case Study

## Technologies Used
* Spring
* MySQL
* Redis
* Unittest

## How to run

-  JDK version 21
-  Maven

```bash
mvn clean install
```

```bash
java -jar -DDB_USERNAME=<your_mysql_db_username> -DDB_PASSWORD=<your_my_sql_db_password> target/dream-backend-0.0.1-SNAPSHOT.jar
```

### First Approach

At first, I created only Entities without using any cache, I had *TournamentGroup*, *User* and *UserTournament* as Entity objects.  

*UserTournament* object was for having the relations between User data and TournamentGroup data objects.  
They had some fields that had been checked frequently.  
After considering better performance I decided to add Redis caches for the data that only used within a tournament.  
This may help to have faster response while handling daily tournament logic.

### Last Approach

After the first attempt, I created *GroupQueue*, *TournamentGroups*, *GroupLeaderboard*, *UserGroup* and *CountryLeaderboard* Redis caches.

*GroupQueue* is for keeping the groups that are created by a user that first come.  
*TournamentGroup* is for having group data to know its creation status.  
*UserGroup* is for knowing the group that user is in.  
*GroupLeaderboard* is for having list sorted by score of users within a tournament group.  
*CountryLeaderboard* is for having list sorted by score of countries.  

### Design Diagram

<img src="https://github.com/omersuve/DreamCase/assets/45875987/f0005e11-350f-4f19-9917-d57581296965" alt="Design" width="1000"/>
