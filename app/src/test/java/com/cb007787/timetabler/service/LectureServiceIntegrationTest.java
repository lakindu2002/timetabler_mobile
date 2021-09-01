package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.LectureCreate;
import com.cb007787.timetabler.model.LectureShow;
import com.cb007787.timetabler.model.SuccessResponseAPI;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LectureServiceIntegrationTest {
    private static AuthService authService;
    private static LectureService lectureService;
    private static String token;
    private static String TEST_DATE = "25-12-2021";

    @BeforeClass
    public static void beforeClass() throws Exception {
        authService = APIConfigurer.getApiConfigurer().getAuthService();
        lectureService = APIConfigurer.getApiConfigurer().getLectureService();

        doLoginAsAcademicAdministrator();
    }

    @Test
    public void testShouldNotAllowAcademicAdministratorToGetLecturesForThem() {
        try {
            //forbidden for admins.
            Response<List<LectureShow>> call = lectureService.getMyLectures(token, TEST_DATE).execute();
            assertEquals("testShouldNotAllowAcademicAdministratorToGetLecturesForThem", 403, call.code());
            System.out.println("testShouldNotAllowAcademicAdministratorToGetLecturesForThem: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldNotAllowAcademicAdministratorToGetLecturesForThem: FAILED");
            fail("testShouldNotAllowAcademicAdministratorToGetLecturesForThem: FAILED");
        }
    }

    @Test
    public void testShouldRejectCreateRequestDueToMissingData() {
        try {
            //bad request.
            Response<SuccessResponseAPI> call = lectureService.createLecture(new LectureCreate(), token).execute();
            assertEquals("testShouldRejectCreateRequestDueToMissingData", 400, call.code());
            System.out.println("testShouldRejectCreateRequestDueToMissingData: PASSED");
        } catch (Exception ex) {
            System.out.println("testShouldRejectCreateRequestDueToMissingData: FAILED");
            fail("testShouldRejectCreateRequestDueToMissingData: FAILED");
        }
    }

    @Test
    public void testScheduleLectureWithDateBeforeCurrentDateTime() {
        try {
            Calendar currentDateTime = Calendar.getInstance();
            currentDateTime.set(Calendar.DATE, currentDateTime.get(Calendar.DATE) - 1);

            LectureCreate create = new LectureCreate();
            create.setLectureDate(currentDateTime.getTime());
            create.setStartTime("08:30");
            create.setEndTime("09:30");
            create.setClassroomID(10);
            create.setModuleId(2);
            create.setBatchList(Collections.singletonList("HF2131SEENG").toArray(new String[0]));

            Response<SuccessResponseAPI> call = lectureService.createLecture(create, token).execute();
            assertEquals("testScheduleLectureWithDateBeforeCurrentDateTime", 400, call.code());
            System.out.println("testScheduleLectureWithDateBeforeCurrentDateTime: PASSED");
        } catch (Exception ex) {
            System.out.println("testScheduleLectureWithDateBeforeCurrentDateTime: FAILED");
            fail("testScheduleLectureWithDateBeforeCurrentDateTime: FAILED");
        }
    }

    @Test
    public void testScheduleLectureWithProvidingInvalidStartAndEndTime() {
        try {
            Calendar currentDateTime = Calendar.getInstance();
            currentDateTime.set(Calendar.DATE, currentDateTime.get(Calendar.DATE) - 1);

            LectureCreate create = new LectureCreate();
            create.setLectureDate(currentDateTime.getTime());
            create.setStartTime(null);
            create.setEndTime(null);
            create.setClassroomID(10);
            create.setModuleId(2);
            create.setBatchList(Collections.singletonList("HF2131SEENG").toArray(new String[0]));

            Response<SuccessResponseAPI> call = lectureService.createLecture(create, token).execute();
            assertEquals("testScheduleLectureWithProvidingInvalidStartAndEndTime", 400, call.code());
            System.out.println("testScheduleLectureWithProvidingInvalidStartAndEndTime: PASSED");
        } catch (Exception ex) {
            System.out.println("testScheduleLectureWithProvidingInvalidStartAndEndTime: FAILED");
            fail("testScheduleLectureWithProvidingInvalidStartAndEndTime: FAILED");
        }
    }

    @Test
    public void testScheduleLectureWithoutProvidingBatches() {
        try {
            Calendar currentDateTime = Calendar.getInstance();
            currentDateTime.set(Calendar.DATE, currentDateTime.get(Calendar.DATE) - 1);

            LectureCreate create = new LectureCreate();
            create.setLectureDate(currentDateTime.getTime());
            create.setStartTime("08:30");
            create.setEndTime("09:30");
            create.setClassroomID(10);
            create.setModuleId(2);
            create.setBatchList(new String[0]);

            Response<SuccessResponseAPI> call = lectureService.createLecture(create, token).execute();
            assertEquals("testScheduleLectureWithoutProvidingBatches", 400, call.code());
            System.out.println("testScheduleLectureWithoutProvidingBatches: PASSED");
        } catch (Exception ex) {
            System.out.println("testScheduleLectureWithoutProvidingBatches: FAILED");
            fail("testScheduleLectureWithoutProvidingBatches: FAILED");
        }
    }

    @Test
    public void testScheduleLectureWithoutProvidingPoorlyFormattedDate() {
        try {
            LectureCreate create = new LectureCreate();
            create.setLectureDate(null);
            create.setStartTime("08:30");
            create.setEndTime("09:30");
            create.setClassroomID(10);
            create.setModuleId(2);
            create.setBatchList(Collections.singletonList("HF2131SEENG").toArray(new String[0]));

            Response<SuccessResponseAPI> call = lectureService.createLecture(create, token).execute();
            assertEquals("testScheduleLectureWithoutProvidingPoorlyFormattedDate", 400, call.code());
            System.out.println("testScheduleLectureWithoutProvidingPoorlyFormattedDate: PASSED");
        } catch (Exception ex) {
            System.out.println("testScheduleLectureWithoutProvidingPoorlyFormattedDate: FAILED");
            fail("testScheduleLectureWithoutProvidingPoorlyFormattedDate: FAILED");
        }
    }

    public static void doLoginAsAcademicAdministrator() throws Exception {
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