package com.cb007787.timetabler.model;

import java.util.ArrayList;

/**
 * POJO Class used to represent the error object returned from the RESTful API.
 */
public class ErrorResponseAPI {
    private String displayMessage;
    private String errorMessage;
    private ArrayList<String> validationErrors;

    public ErrorResponseAPI() {
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ArrayList<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(ArrayList<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    @Override
    public String toString() {
        return "ErrorResponseAPI{" +
                "displayMessage='" + displayMessage + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", validationErrors=" + validationErrors +
                '}';
    }
}
