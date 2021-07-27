package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.AuthReturnDTO;
import com.cb007787.timetabler.model.SuccessResponseAPI;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Interface declared to expose the API Methods.
 * <p>
 * Retrofit will provide the implementations required to communicate with the API.
 *
 * @author Lakindu Hewawasam
 * @since 7th July 2021
 */
public interface AuthService {
    String API_ENDPOINT = "auth/";

    /**
     * Call is the HTTP Request made
     *
     * @param theLoginRequest The request body
     * @return The return provided from the API on 200 status code.
     */
    @POST(API_ENDPOINT + "login")
    Call<AuthReturnDTO> login(@Body HashMap<String, String> theLoginRequest);

    /**
     * Makes an HTTP call to the <b>/reset</b> endpoint of the Auth API to reset default password
     *
     * @param resetRequest Request body containing the username and the new password.
     * @return The success provided by the API upon successful password reset.
     */
    @PUT(API_ENDPOINT + "reset")
    Call<SuccessResponseAPI> resetDefaultPassword(@Body HashMap<String, String> resetRequest, @Header("Authorization") String token);
}
