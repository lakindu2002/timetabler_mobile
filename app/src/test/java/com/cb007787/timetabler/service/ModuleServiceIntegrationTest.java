package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.Module;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.*;

public class ModuleServiceIntegrationTest {
    private static AuthService authService;
    private static ModuleService moduleService;
    private static String token;

    @BeforeClass
    public static void beforeClass() throws Exception {
        authService = APIConfigurer.getApiConfigurer().getAuthService();
        moduleService = APIConfigurer.getApiConfigurer().getModuleService();

        doLoginAsAcademicAdmin();
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

    @Test
    public void testShouldGetAllModulesSuccessfully() {
        try {
            Response<List<Module>> call = moduleService.getAllModulesAtTimetabler(token).execute();
            assertEquals("testShouldGetAllModulesSuccessfully", 200, call.code());
            System.out.println("testShouldGetAllModulesSuccessfully: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldGetAllModulesSuccessfully: FAILED");
            fail("testShouldGetAllModulesSuccessfully: FAILED");
        }
    }

    @Test
    public void testShouldNotGetAllModulesWhenTokenIsMissing() {
        try {
            //forbidden
            Response<List<Module>> call = moduleService.getAllModulesAtTimetabler(null).execute();
            assertEquals("testShouldNotGetAllModulesWhenTokenIsMissing", 403, call.code());
            System.out.println("testShouldNotGetAllModulesWhenTokenIsMissing: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldNotGetAllModulesWhenTokenIsMissing: FAILED");
            fail("testShouldNotGetAllModulesWhenTokenIsMissing: FAILED");
        }
    }

    @Test
    public void testShouldNotGetAllModulesWhenUserIsUnAuthorized() {
        try {
            //should not allow student login to view all modules.
            HashMap<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", "disalrashmik"); //student login
            loginRequest.put("password", "123123123");

            Call<AuthReturn> loginCall = authService.login(loginRequest);
            Response<AuthReturn> execute = loginCall.execute(); //execute synchronously

            Response<List<Module>> call = moduleService.getAllModulesAtTimetabler(execute.headers().get("Authorization")).execute();
            assertEquals("testShouldNotGetAllModulesWhenUserIsUnAuthorized", 403, call.code());
            System.out.println("testShouldNotGetAllModulesWhenUserIsUnAuthorized: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldNotGetAllModulesWhenUserIsUnAuthorized: FAILED");
            fail("testShouldNotGetAllModulesWhenUserIsUnAuthorized: FAILED");
        }
    }

    @Test
    public void testShouldNotGetMyModulesForAcademicAdministrator() {
        try {
            //forbidden for academic admin
            Response<List<Module>> call = moduleService.getAllModulesForUser(token).execute();
            assertEquals("testShouldNotGetMyModulesForAcademicAdministrator", 403, call.code());
            System.out.println("testShouldNotGetMyModulesForAcademicAdministrator: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldNotGetMyModulesForAcademicAdministrator: FAILED");
            fail("testShouldNotGetMyModulesForAcademicAdministrator: FAILED");
        }
    }
}