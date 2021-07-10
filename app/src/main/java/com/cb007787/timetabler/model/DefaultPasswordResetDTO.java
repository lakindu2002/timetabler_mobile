package com.cb007787.timetabler.model;

/**
 * POJO Class used to reset the default password of the first time login user.
 * <p>
 * Class will carry the JSON Request to the Server.
 *
 * @author Lakindu Hewawasam
 */
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
