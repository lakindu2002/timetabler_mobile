package com.cb007787.timetabler.model;

/**
 * POJO Class used to hold the login request that will be sent to the TimeTabler API.
 *
 * @author Lakindu Hewawasam
 */
public class AuthRequestDTO {
    private String username;
    private String password;

    public AuthRequestDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
