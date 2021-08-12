package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.Role;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserRoleService {
    String BASE_ENDPOINT = "roles/";

    @GET(BASE_ENDPOINT + "getAllExceptSystemAdmin")
    Call<List<Role>> getAllRolesWithoutSystemAdmin(@Header("Authorization") String token);
}
