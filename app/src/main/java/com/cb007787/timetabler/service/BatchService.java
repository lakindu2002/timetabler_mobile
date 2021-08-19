package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.BatchCreate;
import com.cb007787.timetabler.model.BatchShow;
import com.cb007787.timetabler.model.SuccessResponseAPI;

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

public interface BatchService {
    String BASE_ENDPOINT = "batch/";

    @GET(BASE_ENDPOINT + "all")
    Call<List<BatchShow>> getAllBatches(@Header("Authorization") String token);

    @DELETE(BASE_ENDPOINT + "delete/{batchCode}")
    Call<SuccessResponseAPI> deleteBatch(@Header("Authorization") String token, @Path("batchCode") String batchCode);

    @PUT(BASE_ENDPOINT + "update")
    Call<SuccessResponseAPI> updateBatchName(@Body BatchShow batchAtPosition, @Header("Authorization") String token);

    @POST(BASE_ENDPOINT + "create")
    Call<SuccessResponseAPI> createBatch(@Body BatchCreate theCreateObj, @Header("Authorization") String token);

    @GET(BASE_ENDPOINT + "find/{batchCode}")
    Call<BatchShow> getBatchInformation(@Header("Authorization") String token, @Path("batchCode") String batchCodeToLoad);

    @PUT(BASE_ENDPOINT + "removeModuleFromBatch")
    Call<SuccessResponseAPI> deAssignModuleFromBatch(@Header("Authorization") String token, @Query("moduleId") int moduleId, @Query("batchCode") String batchCode);

}
