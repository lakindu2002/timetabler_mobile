package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.BatchShow;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface BatchService {
    String BASE_ENDPOINT = "batch/";

    @GET(BASE_ENDPOINT + "all")
    Call<List<BatchShow>> getAllBatches(@Header("Authorization") String token);
}
