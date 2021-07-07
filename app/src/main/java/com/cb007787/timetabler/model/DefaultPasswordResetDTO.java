package com.cb007787.timetabler.model;

public class DefaultPasswordResetDTO {
    private String username;
    private String newPassword;

    public DefaultPasswordResetDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
