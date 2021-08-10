package com.cb007787.timetabler.service;


import com.cb007787.timetabler.model.LectureShow;
import com.cb007787.timetabler.model.SuccessResponseAPI;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LectureService {
    String BASE_ENDPOINT = "lectures/";

    /**
     * Get all lectures for given date
     *
     * @param token       JWT Token for authorization
     * @param requestBody In dd-MM-yyyy format
     * @return the list of lectures for selected date.
     */
    @GET(BASE_ENDPOINT + "getMyLectures")
    Call<List<LectureShow>> getMyLectures(@Header("Authorization") String token, @Query("selectedDate") String requestBody);

    @GET(BASE_ENDPOINT + "findModuleAndClassroomsForLectureSchedule/{moduleId}")
    Call<HashMap<String, Object>> getModuleInformationAndClassroomForSchedule(@Header("Authorization") String token, @Path("moduleId") int moduleIdPassedFromIntent);

    @DELETE(BASE_ENDPOINT + "cancel/{lectureId}")
    Call<SuccessResponseAPI> cancelLecture(@Header("Authorization") String token, @Path("lectureId") int lectureId);
}
