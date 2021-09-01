package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.AuthReturn;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AuthServiceIntegrationTest {
    //api testing must be done synchronously as Junit does not execute callback due to assertion
    //ending from primary thread

    private static AuthService authService;
    private static Logger logger;

    @BeforeClass
    public static void beforeClass() {
        authService = APIConfigurer.getApiConfigurer().getAuthService();
        logger = Logger.getLogger(AuthServiceIntegrationTest.class.getName());
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
                    "testShouldLoginSuccessfully: ", execute.code(), 200
            );
            logger.info("testShouldLoginSuccessfully: PASSED");
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
                    "testShouldNotLoginWhenUsernameOrPasswordIsMissing: ", execute.code(), 400
            );
            logger.info("testShouldNotLoginWhenUsernameOrPasswordIsMissing: PASSED");
        } catch (Exception ex) {
            fail("testShouldNotLoginWhenUsernameOrPasswordIsMissing: FAILED");
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
                    "testShouldNotLoginWhenUsernameOrPasswordIsMissing: ", execute.code(), 400
            );
            logger.info("testShouldNotLoginWhenUsernameOrPasswordIsMissing: PASSED");
        } catch (Exception ex) {
            fail("testShouldNotLoginWhenUsernameOrPasswordIsMissing: FAILED");
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
                    "testShouldNotLoginWhenUsernameOrPasswordIsMissing: ", execute.code(), 400
            );
            logger.info("testShouldNotLoginWhenUsernameOrPasswordIsMissing: PASSED");
        } catch (Exception ex) {
            fail("testShouldNotLoginWhenUsernameOrPasswordIsMissing: FAILED");
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
                    "testShouldNotLoginWhenUsernameOrPasswordIsMissing: ", execute.code(), 400
            );
            logger.info("testShouldNotLoginWhenUsernameOrPasswordIsMissing: PASSED");
        } catch (Exception ex) {
            fail("testShouldNotLoginWhenUsernameOrPasswordIsMissing: FAILED");
            ex.printStackTrace();
        }
    }
}