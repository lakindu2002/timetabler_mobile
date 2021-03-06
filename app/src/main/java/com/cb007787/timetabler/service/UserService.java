package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.model.User;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @GET(BASE_ENDPOINT + "allAcademicAdministrators")
    Call<List<User>> getAcademicAdmins(@Header("Authorization") String token);

    @GET(BASE_ENDPOINT + "allStudents")
    Call<List<User>> getAllStudents(@Header("Authorization") String token);

    @GET(BASE_ENDPOINT + "allLecturers")
    Call<List<User>> getAllLecturers(@Header("Authorization") String token);

    @DELETE(BASE_ENDPOINT + "delete/{username}")
    Call<SuccessResponseAPI> deleteUser(@Header("Authorization") String token, @Path("username") String username);

    @PUT(BASE_ENDPOINT + "deAssignStudent")
    Call<SuccessResponseAPI> deAssignStudentFromBatch(@Header("Authorization") String token, @Query("username") String username);

    @GET(BASE_ENDPOINT + "find/studentsWithNoBatch")
    Call<List<User>> getStudentsWithoutABatch(@Header("Authorization") String token);

    @GET(BASE_ENDPOINT + "find/content/systemAdminHome")
    Call<HashMap<String, Integer>> getContentForSystemAdminHome(@Header("Authorization") String token);

    @GET(BASE_ENDPOINT + "find/content/academicAdminHome")
    Call<HashMap<String, Integer>> getContentForAcademicAdminHome(@Header("Authorization") String token);
}
