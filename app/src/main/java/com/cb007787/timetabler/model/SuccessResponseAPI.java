package com.cb007787.timetabler.model;

/**
 * POJO class will carry the basic success 200 response given by the API.
 *
 * @author Lakindu Hewawasam
 */
public class SuccessResponseAPI {
    private String message;
    private int code;

    public SuccessResponseAPI() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
