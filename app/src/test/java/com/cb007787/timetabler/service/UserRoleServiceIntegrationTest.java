package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.Role;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.*;

public class UserRoleServiceIntegrationTest {
    private static UserRoleService userRoleService;
    private static AuthService authService;
    private static String token;

    @BeforeClass
    public static void beforeClass() throws Exception {
        userRoleService = APIConfigurer.getApiConfigurer().getUserRoleService();
        authService = APIConfigurer.getApiConfigurer().getAuthService();
        doLoginAsSystemAdmin();
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

    @Test
    public void testShouldGetAllUsersExceptSystemAdminSuccessfully() {
        try {
            Response<List<Role>> call = userRoleService.getAllRolesWithoutSystemAdmin(token).execute();
            assertEquals("testShouldGetAllUsersExceptSystemAdminSuccessfully", 200, call
                    .code());
            System.out.println("testShouldGetAllUsersExceptSystemAdminSuccessfully: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldGetAllUsersExceptSystemAdminSuccessfully: FAILED");
        }
    }

    @Test
    public void testShouldNotGetAllUsersExceptSystemAdminSuccessfullyWhenTokenIsInvalid() {
        try {
            //401 - token invalid - unauthorized.
            Response<List<Role>> call = userRoleService.getAllRolesWithoutSystemAdmin(token + "1").execute();
            assertEquals("testShouldGetAllUsersExceptSystemAdminSuccessfully", 401, call
                    .code());
            System.out.println("testShouldGetAllUsersExceptSystemAdminSuccessfully: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldGetAllUsersExceptSystemAdminSuccessfully: FAILED");
        }
    }

    @Test
    public void testShouldNotGetAllUsersExceptSystemAdminSuccessfullyWhenTokenIsMissing() {
        try {
            //403 - forbidden
            Response<List<Role>> call = userRoleService.getAllRolesWithoutSystemAdmin(null).execute();
            assertEquals("testShouldGetAllUsersExceptSystemAdminSuccessfully", 403, call
                    .code());
            System.out.println("testShouldGetAllUsersExceptSystemAdminSuccessfully: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldGetAllUsersExceptSystemAdminSuccessfully: FAILED");
        }
    }
}