package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.BatchCreate;
import com.cb007787.timetabler.model.BatchShow;
import com.cb007787.timetabler.model.SuccessResponseAPI;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.*;

public class BatchServiceIntegrationTest {
    private static AuthService authService;
    private static BatchService batchService;
    private static String token;
    private static String CREATE_BATCH_CODE = "INTEGRATION TEST";

    @BeforeClass
    public static void beforeClass() throws Exception {
        authService = APIConfigurer.getApiConfigurer().getAuthService();
        batchService = APIConfigurer.getApiConfigurer().getBatchService();
        doLoginAsAcademicAdmin();
    }

    @Test
    public void testShouldGetAllBatchesSuccessfully() {
        try {
            //expect a 200 back from the server.
            Call<List<BatchShow>> testPoint = batchService.getAllBatches(token);
            Response<List<BatchShow>> executedCall = testPoint.execute();

            assertEquals("testShouldGetAllBatchesSuccessfully", 200, executedCall.code());
            System.out.println("testShouldGetAllBatchesSuccessfully: PASSED");
        } catch (Exception ex) {
            fail("testShouldGetAllBatchesSuccessfully: FAILED");
        }
    }

    @Test
    public void testShouldCreateABatchSuccessfully() {
        try {
            BatchCreate request = new BatchCreate();
            request.setBatchCode(CREATE_BATCH_CODE);
            request.setBatchName("Testing a Batch On jUnit Integration");
            request.setModuleId(new String[]{});

            Response<SuccessResponseAPI> createCall = batchService.createBatch(request, token).execute();
            assertEquals("testShouldCreateABatchSuccessfully: ", 200, createCall.code());
            System.out.println("testShouldCreateABatchSuccessfully: PASSED");
        } catch (Exception ex) {
            fail("testShouldCreateABatchSuccessfully: FAILED");
            ex.printStackTrace();
        }
    }

    @Test
    public void testShouldDeleteABatchSuccessfully() {
        try {
            Response<SuccessResponseAPI> call = batchService.deleteBatch(token, CREATE_BATCH_CODE).execute();
            assertEquals("testShouldDeleteABatchSuccessfully", 200, call.code());
            System.out.println("testShouldDeleteABatchSuccessfully: PASSED");
        } catch (Exception ex) {
            fail("testShouldDeleteABatchSuccessfully: FAILED");
            ex.printStackTrace();
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