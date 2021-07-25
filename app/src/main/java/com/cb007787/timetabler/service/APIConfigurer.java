package com.cb007787.timetabler.service;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class APIConfigurer {
    private static final APIConfigurer apiConfigurer = new APIConfigurer();

    private Retrofit theAPI;

    private APIConfigurer() {
        this.theAPI = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create()) //use Jackson to deserialize JSON to Java.
                .baseUrl("http://192.168.1.3:8080/api/") //add the base url for the api
                .build(); //build the Retrofit instance
    }

    public static APIConfigurer getApiConfigurer() {
        return apiConfigurer;
    }

    public Retrofit getTheAPI() {
        return theAPI;
    }

    public AuthService getAuthService() {
        //generate an instance of the AuthService interface by adding the required HTTP implementations and return it to caller.
        return getTheAPI().create(AuthService.class); //return an instance of the auth service.
    }
}
