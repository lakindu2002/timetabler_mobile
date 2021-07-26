package com.cb007787.timetabler.model;

public class PasswordReset {
    private String usernameNeedingReset;
    private long tokenExpirationTime;

    public PasswordReset() {
    }

    public String getUsernameNeedingReset() {
        return usernameNeedingReset;
    }

    public void setUsernameNeedingReset(String usernameNeedingReset) {
        this.usernameNeedingReset = usernameNeedingReset;
    }

    public long getTokenExpirationTime() {
        return tokenExpirationTime;
    }

    public void setTokenExpirationTime(long tokenExpirationTime) {
        this.tokenExpirationTime = tokenExpirationTime;
    }
}
