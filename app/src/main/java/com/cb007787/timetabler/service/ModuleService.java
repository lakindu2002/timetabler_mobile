package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.Module;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ModuleService {
    String BASE_ENDPOINT = "module/";

    @GET(BASE_ENDPOINT + "getAll")
    Call<List<Module>> getAllModulesForUser(@Header("Authorization") String token);
}
