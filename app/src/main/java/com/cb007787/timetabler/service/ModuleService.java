package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.model.SuccessResponseAPI;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ModuleService {
    String BASE_ENDPOINT = "module/";

    @GET(BASE_ENDPOINT + "getAll")
    Call<List<Module>> getAllModulesForUser(@Header("Authorization") String token);

    @GET(BASE_ENDPOINT + "find/all")
    Call<List<Module>> getAllModulesAtTimetabler(@Header("Authorization") String token);

    @POST(BASE_ENDPOINT + "create")
    Call<SuccessResponseAPI> createModule(@Body Module theModule, @Header("Authorization") String token);

    @DELETE(BASE_ENDPOINT + "delete/{moduleId}")
    Call<SuccessResponseAPI> deleteModule(@Path("moduleId") int moduleId, @Header("Authorization") String token);
}
