package com.cb007787.timetabler.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.cb007787.timetabler.model.AuthReturnDTO;
import com.cb007787.timetabler.model.PasswordReset;
import com.cb007787.timetabler.view.common.CommonContainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Date;
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

    public static boolean isTokenValid(Context theContext, PreferenceInformation fileName) {
        try {
            long tokenExpirationTime = getLoggedInUser(theContext, fileName).getTokenExpiresIn();
            Date currentDate = new Date(System.currentTimeMillis());

            if (tokenExpirationTime > currentDate.getTime()) {
                //token is valid since current time is less than token expiration time in MS
                return true;
            } else {
                //token is expired, redirect to login and show session expired message
                Intent expiredLogin = new Intent(theContext, CommonContainer.class);
                expiredLogin.putExtra("loadingPage", "LOGIN");
                theContext.startActivity(expiredLogin);
                ((Activity) theContext).finish(); //activity is part of app context
                //so type cast context to activity and finish the activity so back button does not navigate user back
                clearSharedPreferences(theContext, fileName); //clear user data in shared preferences
                return false;
            }

        } catch (JsonProcessingException e) {
            LOGGER.warning("ERROR PARSING JSON");
            return false;
        }
    }
}
