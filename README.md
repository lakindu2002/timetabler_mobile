# EEA Mobile Application for TimeTabler - by Lakindu Hewawasam
This repository maintains the code base for the `Android` application for the TimeTabler Web Application. 

It aims to provide a mobile application that integrates with the existing Spring Boot application for EEA Web Application

1. [Access Web Application Repo Here](https://github.com/lakindu2002/EEA_CB007787)
2. [Access Mobile Application Repo Here](https://github.com/lakindu2002/EEA_CB007787_Mobile)


## Development Process 
- JWT has been used for the authentication between mobile application and server.
- Content providers have been used along with an SQLite Database to create `Task Management` sub system for the students 
- Material UI was used to style UI components 
- Retrofit with Async operations were used to fetch and post data from and to the API
- Application widgets were used to expose the tasks and it communicates with SQLite Database via Content Provider to retrieve data.

## Repo Access 

Clone the repo using the `ssh` link or the `https` link. If using `ssh` make sure the `ssh` keygen is set for the repo and access is provided. 

## Code Setup
- Clone the repository using the clone url
- Open the project in `Android Studio`
- Sync the gradle project
- Run on an emulator.
- Make sure the web application is up and running with a working database to ensure connectivity

## Special Components Used
- UI: Google Material UI
- Networking: Retrofit + Jackson Parser
- Content Provider - Utilizes A Custom Built Content Provider Communicating With SQLite DB
- Widget - Has an application widget that is used to provide external communication with the content provider.

## Endpoint Declaration 
- Testing: `http://localhost:8080`

## Integration Testing 
- JUnit Testing has been conducted to test the `Retrofit` API calls from  `Android` to `Backend`
- To ensure accuracy between real world integration, `posting` is not done for the `production database`
- To run the tests, ensure that the `backend` project is up and running before running the tests on the `Android` client.

**Please note that this project requires a minimum sdk of Android 8.0 (SDK - 26 or Oreo).**


