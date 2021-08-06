package com.cb007787.timetabler.service;


import com.cb007787.timetabler.model.LectureShow;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface LectureService {
    String BASE_ENDPOINT = "lectures/";

    @GET(BASE_ENDPOINT + "getMyLectures")
    Call<List<LectureShow>> getMyLectures(@Header("Authorization") String token, @Query("selectedDate") String requestBody);
}
