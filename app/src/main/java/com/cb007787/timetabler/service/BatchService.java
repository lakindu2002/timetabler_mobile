package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.BatchShow;
import com.cb007787.timetabler.model.SuccessResponseAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface BatchService {
    String BASE_ENDPOINT = "batch/";

    @GET(BASE_ENDPOINT + "all")
    Call<List<BatchShow>> getAllBatches(@Header("Authorization") String token);

    @DELETE(BASE_ENDPOINT + "delete/{batchCode}")
    Call<SuccessResponseAPI> deleteBatch(@Header("Authorization") String token, @Path("batchCode") String batchCode);

    @PUT(BASE_ENDPOINT + "update")
    Call<SuccessResponseAPI> updateBatchName(@Body BatchShow batchAtPosition, @Header("Authorization") String token);
}
