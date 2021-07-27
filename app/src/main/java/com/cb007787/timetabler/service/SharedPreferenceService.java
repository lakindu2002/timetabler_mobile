package com.cb007787.timetabler.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.cb007787.timetabler.model.AuthReturnDTO;
import com.cb007787.timetabler.model.PasswordReset;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Logger;

public class SharedPreferenceService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = Logger.getLogger(SharedPreferenceService.class.getName());

    public static void setLoginSuccessVariables(Context theContext, AuthReturnDTO authReturnDTO, String token, PreferenceInformation fileName) throws JsonProcessingException {
        SharedPreferences.Editor edit = theContext.getSharedPreferences(fileName.getLabelForIdentifier(), Context.MODE_PRIVATE).edit();

        edit.putString(PreferenceInformation.JWT_TOKEN.getLabelForIdentifier(), token);
        edit.putString(PreferenceInformation.LOGGED_IN_USER.getLabelForIdentifier(), objectMapper.writeValueAsString(authReturnDTO));
        edit.putLong(PreferenceInformation.LOGGED_IN_SESSION_PERIOD.getLabelForIdentifier(), authReturnDTO.getTokenExpiresIn());
        edit.apply();

        LOGGER.info("SAVED LOGGED IN USER INFO ON SHARED PREFERENCES");
    }

    public static void setResetUserInformation(Context theContext, PasswordReset passwordReset, String token, PreferenceInformation fileName) throws JsonProcessingException {
        SharedPreferences.Editor edit = theContext.getSharedPreferences(fileName.getLabelForIdentifier(), Context.MODE_PRIVATE).edit();

        edit.putString(PreferenceInformation.JWT_TOKEN.getLabelForIdentifier(), token);
        edit.putString(PreferenceInformation.USER_RESET_INFO.getLabelForIdentifier(), objectMapper.writeValueAsString(passwordReset));
        edit.putLong(PreferenceInformation.LOGGED_IN_SESSION_PERIOD.getLabelForIdentifier(), passwordReset.getTokenExpirationTime());
        edit.apply();

        LOGGER.info("SAVED RESET USER INFORMATION ON SHARED PREFERENCE");
    }

    public static void clearSharedPreferences(Context theContext, PreferenceInformation fileName) {
        SharedPreferences.Editor edit = theContext.getSharedPreferences(fileName.getLabelForIdentifier(), Context.MODE_PRIVATE).edit();
        edit.clear();
        edit.apply();

        LOGGER.info("CLEARED SHARED PREFERENCES FOR FILE: " + fileName.getLabelForIdentifier().toUpperCase());
    }

    public static AuthReturnDTO getLoggedInUser(Context theContext, PreferenceInformation fileName) throws JsonProcessingException {
        SharedPreferences sharedPreferences = theContext.getSharedPreferences(fileName.getLabelForIdentifier(), Context.MODE_PRIVATE);
        String loggedInUserJSON = sharedPreferences.getString(PreferenceInformation.LOGGED_IN_USER.getLabelForIdentifier(), null);

        if (loggedInUserJSON == null) {
            LOGGER.info("NO LOGGED IN USER FOUND, RETURNING NULL");
            return null;
        } else {
            LOGGER.info("FOUND LOGGED IN USER AND RETURNING LOGGED IN USER");
            return objectMapper.readValue(loggedInUserJSON, AuthReturnDTO.class);
        }
    }

    public static PasswordReset getResetInformation(Context theContext, PreferenceInformation fileName) throws JsonProcessingException {
        SharedPreferences sharedPreferences = theContext.getSharedPreferences(fileName.getLabelForIdentifier(), Context.MODE_PRIVATE);
        String resetInfoJSON = sharedPreferences.getString(PreferenceInformation.USER_RESET_INFO.getLabelForIdentifier(), null);

        if (resetInfoJSON == null) {
            LOGGER.info("NO RESET USER FOUND, RETURNING NULL");
            return null;
        } else {
            LOGGER.info("FOUND RESETTING USER, RETURNING RESET USER");
            return objectMapper.readValue(resetInfoJSON, PasswordReset.class);
        }
    }

    public static String getToken(Context theContext, PreferenceInformation fileName) {
        return theContext.getSharedPreferences(fileName.getLabelForIdentifier(), Context.MODE_PRIVATE).getString(PreferenceInformation.JWT_TOKEN.getLabelForIdentifier(), null);
    }
}
