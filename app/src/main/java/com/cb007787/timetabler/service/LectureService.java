package com.cb007787.timetabler.service;


import com.cb007787.timetabler.model.LectureCreate;
import com.cb007787.timetabler.model.LectureShow;
import com.cb007787.timetabler.model.SuccessResponseAPI;

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

    @POST(BASE_ENDPOINT + "create")
    Call<SuccessResponseAPI> createLecture(@Body LectureCreate lectureToBeManaged, @Header("Authorization") String token);

    @GET(BASE_ENDPOINT + "getUpdateInformation/{lectureId}")
    Call<HashMap<String, Object>> getTheUpdateInformationForLecture(@Header("Authorization") String token, @Path("lectureId") int lectureId);

    @PUT(BASE_ENDPOINT + "reschedule/{lectureId}")
    Call<SuccessResponseAPI> rescheduleLecture(@Header("Authorization") String token, @Body LectureCreate createdLecture, @Path("lectureId") int lectureId);

    @GET(BASE_ENDPOINT + "find/allLectures/{batchCode}")
    Call<List<LectureShow>> loadLecturesPerBatch(@Path("batchCode") String batchCode, @Header("Authorization") String token);

    @DELETE(BASE_ENDPOINT + "cancel/admin/{lectureId}")
    Call<SuccessResponseAPI> cancelLectureForAdmin(@Header("Authorization") String token, @Path("lectureId") int lectureId, @Query("batchCode") String batchCode);

    @GET(BASE_ENDPOINT + "find/lectures/lecturer/{lecturer}")
    Call<List<LectureShow>> getLectureForLecturer(@Header("Authorization") String token, @Path("lecturer") String usernameOfLecturer);
}
