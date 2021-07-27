package com.cb007787.timetabler.service;

public enum PreferenceInformation {
    PREFERENCE_NAME("TIMETABLER_FILE"),
    LOGGED_IN_USER("LOGGED_IN_USER"),
    LOGGED_IN_SESSION_PERIOD("TOKEN_EXPIRY"),
    JWT_TOKEN("TOKEN"),
    USER_RESET_INFO("RESETTING_INFORMATION");

    private final String labelForIdentifier;

    PreferenceInformation(String labelForIdentifier) {
        this.labelForIdentifier = labelForIdentifier;
    }

    public String getLabelForIdentifier() {
        return labelForIdentifier;
    }
}
