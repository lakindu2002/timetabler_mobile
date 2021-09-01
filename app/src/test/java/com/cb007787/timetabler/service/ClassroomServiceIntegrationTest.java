package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.Classroom;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.*;

public class ClassroomServiceIntegrationTest {
    private static ClassroomService classroomService;
    private static AuthService authService;
    private static String token;

    @BeforeClass
    public static void beforeClass() throws Exception {
        classroomService = APIConfigurer.getApiConfigurer().getTheClassroomService();
        authService = APIConfigurer.getApiConfigurer().getAuthService();

        doLoginAsSystemAdmin();
    }

    @Test
    public void testShouldGetAllClassrooms() {
        try {
            Response<List<Classroom>> call = classroomService.getAllClassrooms(token).execute();
            assertEquals("testShouldGetAllClassrooms", 200, call.code());
            System.out.println("testShouldGetAllClassrooms: PASSED");
        } catch (Exception ex) {
            fail("testShouldGetAllClassrooms: FAILED");
            ex.printStackTrace();
        }
    }

    @Test
    public void testShouldNotGetAllClassroomsWhenTokenNotPresent() {
        try {
            //403 - forbidden
            Response<List<Classroom>> call = classroomService.getAllClassrooms(null).execute();
            assertEquals("testShouldNotGetAllClassroomsWhenTokenNotPresent", 403, call.code());
            System.out.println("testShouldNotGetAllClassroomsWhenTokenNotPresent: PASSED");
        } catch (Exception ex) {
            fail("testShouldNotGetAllClassroomsWhenTokenNotPresent: FAILED");
            ex.printStackTrace();
        }
    }

    @Test
    public void testShouldNotGetAllClassroomsWhenLoggedInAsStudent() {
        try {
            HashMap<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", "disalrashmik");
            loginRequest.put("password", "123123123");

            Call<AuthReturn> loginCall = authService.login(loginRequest);
            Response<AuthReturn> execute = loginCall.execute(); //execute synchronously
            token = execute.headers().get("Authorization");

            Response<List<Classroom>> call = classroomService.getAllClassrooms(token).execute();
            assertEquals("testShouldNotGetAllClassroomsWhenLoggedInAsStudent", 403, call.code());
            System.out.println("testShouldNotGetAllClassroomsWhenLoggedInAsStudent: PASSED");
        } catch (Exception ex) {
            fail("testShouldNotGetAllClassroomsWhenLoggedInAsStudent: FAILED");
            ex.printStackTrace();
        }
    }

    public static void doLoginAsSystemAdmin() throws Exception {
        HashMap<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "praveenr");
        loginRequest.put("password", "123123123");

        Call<AuthReturn> loginCall = authService.login(loginRequest);
        Response<AuthReturn> execute = loginCall.execute(); //execute synchronously
        if (execute.code() == 200) {
            token = execute.headers().get("Authorization");
        } else {
            throw new Exception("Login Failed");
        }
    }
}