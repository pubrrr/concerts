# concerts

Getting upcoming concerts in munich. Getting the artist genre using spotify api and lastfm api 

# Installation

You need 
java > 21
wget

Only working on linux and probably on MAC. Feierwerk cannot be downloaded in windows

# Spotify and LastFM

add an api key for spotify and lastfm. Create an account, create a key and paste it in application.properties

# Run the project
```
./mvnw spring-boot:run 
```

# Build the project

```
./mvnw package
```

# Run the project

```
java -jar target/concerts-0.0.1-SNAPSHOT.jar
```

# Native build

Install graalvm and run
```
mvn -Pnative native:compile
```
