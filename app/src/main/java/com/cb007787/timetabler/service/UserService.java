package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Interface exposes the API for the User API
 */
public interface UserService {
    String BASE_ENDPOINT = "user/"; //base endpoint

    /**
     * Method will retrieve the user information for the logged in user from the rest api
     *
     * @param token    The bearer token for authentication
     * @param username The username to get user information for
     * @return The logged in user information
     */
    @GET(BASE_ENDPOINT + "account/{username}")
    Call<User> getUserInformation(@Header("Authorization") String token, @Path("username") String username);

    @PUT(BASE_ENDPOINT + "account/update")
    Call<SuccessResponseAPI> updateUserAccount(@Header("Authorization") String token, @Body User updateUserRequest);

    @POST(BASE_ENDPOINT + "createUser")
    Call<SuccessResponseAPI> createNewUser(@Body User theNewUser, @Header("Authorization") String token);
}
