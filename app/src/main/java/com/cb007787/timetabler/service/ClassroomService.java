package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.Classroom;
import com.cb007787.timetabler.model.SuccessResponseAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface ClassroomService {
    String BASE_ENDPOINT = "classroom/";

    @DELETE(BASE_ENDPOINT + "delete/{classroomId}")
    Call<SuccessResponseAPI> deleteClassroom(@Path("classroomId") int classroomId, @Header("Authorization") String token);

    @GET(BASE_ENDPOINT + "all")
    Call<List<Classroom>> getAllClassrooms(@Header("Authorization") String token);
}
