package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.User;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.*;

public class UserServiceIntegrationTest {
    private static UserService userService;
    private static AuthService authService;
    private static String token;
    private static String USER_NAME = "lakindu2002";

    @BeforeClass
    public static void beforeClass() throws Exception {
        userService = APIConfigurer.getApiConfigurer().getUserService();
        authService = APIConfigurer.getApiConfigurer().getAuthService();

        doLoginAsAcademicAdmin();
    }

    @Test
    public void testShouldGetAccountInformationSuccessfully() {
        try {
            Response<User> call = userService.getUserInformation(token, USER_NAME).execute();
            if (call.body() == null) {
                fail("testShouldGetAccountInformationSuccessfully: FAILED");
            } else {
                assertEquals("testShouldGetAccountInformationSuccessfully", USER_NAME, call.body().getUsername());
            }
        } catch (Exception ex) {
            System.out.println("testShouldGetAccountInformationSuccessfully: FAILED");
            fail("testShouldGetAccountInformationSuccessfully: FAILED");
        }
    }

    @Test
    public void testShouldGetAllLecturersSuccessfully() {
        try {
            Response<List<User>> call = userService.getAllLecturers(token).execute();
            assertEquals("testShouldGetAllLecturersSuccessfully", 200, call.code());
            System.out.println("testShouldGetAllLecturersSuccessfully: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldGetAllLecturersSuccessfully: FAILED");
            fail("testShouldGetAllLecturersSuccessfully: FAILED");
        }
    }

    @Test
    public void testShouldGetAllStudentsSuccessfully() {
        try {
            Response<List<User>> call = userService.getAllStudents(token).execute();
            assertEquals("testShouldGetAllStudentsSuccessfully", 200, call.code());
            System.out.println("testShouldGetAllStudentsSuccessfully: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldGetAllStudentsSuccessfully: FAILED");
            fail("testShouldGetAllStudentsSuccessfully: FAILED");
        }
    }

    @Test
    public void testShouldNotAllowAcademicAdminToGetAllAcademicAdmins() {
        try {
            //forbidden
            Response<List<User>> call = userService.getAcademicAdmins(token).execute();
            assertEquals("testShouldNotAllowAcademicAdminToGetAllAcademicAdmins", 403, call.code());
            System.out.println("testShouldNotAllowAcademicAdminToGetAllAcademicAdmins: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldNotAllowAcademicAdminToGetAllAcademicAdmins: FAILED");
            fail("testShouldNotAllowAcademicAdminToGetAllAcademicAdmins: FAILED");
        }
    }

    @Test
    public void testShouldGetContentForAcademicAdminHome() {
        try {
            Response<HashMap<String, Integer>> call = userService.getContentForAcademicAdminHome(token).execute();
            assertEquals("testShouldGetContentForAcademicAdminHome", 200, call.code());
            System.out.println("testShouldGetContentForAcademicAdminHome: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldGetContentForAcademicAdminHome: FAILED");
            fail("testShouldGetContentForAcademicAdminHome: FAILED");
        }
    }

    @Test
    public void testShouldGetContentForSystemAdminHome() {
        try {
            HashMap<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", "praveenr");
            loginRequest.put("password", "123123123");

            Response<AuthReturn> execute = authService.login(loginRequest).execute();
            if (execute.code() == 200) {
                Response<HashMap<String, Integer>> call = userService.getContentForSystemAdminHome(execute.headers().get("Authorization")).execute();
                assertEquals("testShouldGetContentForSystemAdminHome", 200, call.code());
                System.out.println("testShouldGetContentForSystemAdminHome: PASSED");
            } else {
                fail("testShouldGetContentForSystemAdminHome: FAILED");
            }
        } catch (Exception ex) {
            System.out.println("testShouldGetContentForSystemAdminHome: FAILED");
            fail("testShouldGetContentForSystemAdminHome: FAILED");
        }
    }

    public static void doLoginAsAcademicAdmin() throws Exception {
        HashMap<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "lakindu2002");
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