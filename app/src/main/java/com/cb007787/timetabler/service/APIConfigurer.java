package com.cb007787.timetabler.service;

import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.PasswordReset;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class APIConfigurer {
    private static final APIConfigurer apiConfigurer = new APIConfigurer();

    private Retrofit theAPI;
    private static Converter<ResponseBody, ErrorResponseAPI> errorConverter;

    private APIConfigurer() {
        this.theAPI = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create()) //use Jackson to deserialize JSON to Java.
                .baseUrl("http://192.168.1.3:8080/api/") //add the base url for the api
                .build(); //build the Retrofit instance

        //create a Converter that accepts a ResponseBody and produces a ErrorResponseAPI.
        errorConverter = this.theAPI.responseBodyConverter(ErrorResponseAPI.class, new Annotation[]{});
    }

    /**
     * Method will accept the error response body from the response and convert it to the error object
     *
     * @param errorBody The response body for the error
     * @return The ErrorResponseAPI instance containing the error information
     * @throws IOException The exception thrown when JSON cannot be parsed into ErrorResponseAPI type.
     */
    public static ErrorResponseAPI getTheErrorReturned(ResponseBody errorBody) throws IOException {
        return errorConverter.convert(errorBody);
    }

    public static PasswordReset getPasswordResetObject(ResponseBody errorBody) throws IOException {
        return (PasswordReset) getApiConfigurer().getTheAPI().responseBodyConverter(PasswordReset.class, new Annotation[]{}).convert(errorBody);
    }

    public static APIConfigurer getApiConfigurer() {
        return apiConfigurer;
    }

    public Retrofit getTheAPI() {
        return theAPI;
    }

    public AuthService getAuthService() {
        //generate an instance of the AuthService interface by adding the required HTTP implementations and return it to caller.
        return getTheAPI().create(AuthService.class); //return an instance of the auth service.
    }

    public UserService getUserService() {
        //generate an instance of the UserService interface using retrofit. Retrofit adds the required underlying HTTP implementation
        return getTheAPI().create(UserService.class);
    }

    public ModuleService getModuleService() {
        //generate an instance of the ModuleService interface using retrofit. Retrofit adds the required underlying HTTP implementation
        return getTheAPI().create(ModuleService.class);
    }

    public LectureService getLectureService() {
        //generate an instance of the LectureService interface using retrofit. Retrofit adds the required underlying HTTP implementation
        return getTheAPI().create(LectureService.class);
    }

    public UserRoleService getUserRoleService() {
        //generate an instance of the UserRoleService interface using retrofit. Retrofit adds the required underlying HTTP implementation
        return getTheAPI().create(UserRoleService.class);
    }

    public ClassroomService getTheClassroomService() {
        //generate an instance of the ClassroomService interface using retrofit. Retrofit adds the required underlying HTTP implementation
        return getTheAPI().create(ClassroomService.class);
    }
}
