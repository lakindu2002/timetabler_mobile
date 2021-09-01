package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.AuthReturn;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AuthServiceIntegrationTest {
    //api testing must be done synchronously as Junit does not execute callback due to assertion
    //ending from primary thread

    private static AuthService authService;

    @BeforeClass
    public static void beforeClass() {
        authService = APIConfigurer.getApiConfigurer().getAuthService();
    }

    @Test
    public void testShouldLoginSuccessfully() {
        try {
            HashMap<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", "disalrashmik");
            loginRequest.put("password", "123123123");

            Call<AuthReturn> loginCall = authService.login(loginRequest);
            Response<AuthReturn> execute = loginCall.execute(); //execute synchronously
            assertEquals(
                    "testShouldLoginSuccessfully: ", 200, execute.code()
            );
            System.out.println("testShouldLoginSuccessfully: PASSED");
        } catch (Exception ex) {
            fail("testShouldLoginSuccessfully: FAILED");
            ex.printStackTrace();
        }
    }

    @Test
    public void testShouldNotLoginWhenUsernameIsMissing() {
        try {
            HashMap<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", "");
            loginRequest.put("password", "123123123");

            Call<AuthReturn> loginCall = authService.login(loginRequest);
            Response<AuthReturn> execute = loginCall.execute(); //execute synchronously
            assertEquals(
                    "testShouldNotLoginWhenUsernameIsMissing: ", 400, execute.code()
            );
            System.out.println("testShouldNotLoginWhenUsernameIsMissing: PASSED");
        } catch (Exception ex) {
            fail("testShouldNotLoginWhenUsernameIsMissing: FAILED");
            ex.printStackTrace();
        }
    }

    @Test
    public void testShouldNotLoginWhenPasswordIsMissing() {
        try {
            HashMap<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", "disalrashmik");
            loginRequest.put("password", null);

            Call<AuthReturn> loginCall = authService.login(loginRequest);
            Response<AuthReturn> execute = loginCall.execute(); //execute synchronously
            assertEquals(
                    "testShouldNotLoginWhenPasswordIsMissing: ", 400, execute.code()
            );
            System.out.println("testShouldNotLoginWhenPasswordIsMissing: PASSED");
        } catch (Exception ex) {
            fail("testShouldNotLoginWhenPasswordIsMissing: FAILED");
            ex.printStackTrace();
        }
    }

    @Test
    public void testShouldNotLoginWhenUsernameExceeds30Characters() {
        try {
            HashMap<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", "disalrashmikdisalrashmikdisalrashmikdisalrashmikdisalrashmikdisalrashmikdisalrashmik");
            loginRequest.put("password", null);

            Call<AuthReturn> loginCall = authService.login(loginRequest);
            Response<AuthReturn> execute = loginCall.execute(); //execute synchronously
            assertEquals(
                    "testShouldNotLoginWhenUsernameExceeds30Characters: ", 400, execute.code()
            );
            System.out.println("testShouldNotLoginWhenUsernameExceeds30Characters: PASSED");
        } catch (Exception ex) {
            fail("testShouldNotLoginWhenUsernameExceeds30Characters: FAILED");
            ex.printStackTrace();
        }
    }

    @Test
    public void testShouldNotLoginWhenPasswordExceeds30Characters() {
        try {
            HashMap<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", "disalrashmik");
            loginRequest.put("password", "disalrashmikdisalrashmikdisalrashmikdisalrashmikdisalrashmikdisalrashmikdisalrashmikdisalrashmikdisalrashmikdisalrashmik");

            Call<AuthReturn> loginCall = authService.login(loginRequest);
            Response<AuthReturn> execute = loginCall.execute(); //execute synchronously
            assertEquals(
                    "testShouldNotLoginWhenPasswordExceeds30Characters: ", 400, execute.code()
            );
            System.out.println("testShouldNotLoginWhenPasswordExceeds30Characters: PASSED");
        } catch (Exception ex) {
            fail("testShouldNotLoginWhenPasswordExceeds30Characters: FAILED");
            ex.printStackTrace();
        }
    }
}