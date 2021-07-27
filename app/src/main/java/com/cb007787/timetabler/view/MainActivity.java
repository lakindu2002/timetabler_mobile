package com.cb007787.timetabler.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturnDTO;
import com.cb007787.timetabler.model.PasswordReset;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.common.CommonContainer;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //validate the shared preferences.
    //if user is logged in, send to their respected activity, else load the login page.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AuthReturnDTO loggedInUser = null;
        PasswordReset resetInfo = null;

        try {
            loggedInUser = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            resetInfo = SharedPreferenceService.getResetInformation(this, PreferenceInformation.PREFERENCE_NAME);
        } catch (JsonProcessingException e) {
            System.out.println("ERROR PARSING JSON DURING RETRIEVAL");
        }

        if (resetInfo != null) {
            //a reset info user present
            //check if the reset token has expired, if so send to login
            if (resetInfo.getTokenExpirationTime() > new Date().getTime()) {
                //expiration token is valid,
                //send the password reset fragment
                Intent intent = new Intent(MainActivity.this, CommonContainer.class);
                intent.putExtra("loadingPage", "RESET");
                startActivity(intent);
            } else {
                //password reset token has expired, clear the password reset information
                SharedPreferenceService.clearSharedPreferences(this, PreferenceInformation.PREFERENCE_NAME);
                //send to login
                Intent intent = new Intent(MainActivity.this, CommonContainer.class);
                intent.putExtra("loadingPage", "LOGIN");
                startActivity(intent);
            }
            finish();

        } else if (loggedInUser != null) {
            //there is a user
            //check if their token has expired
            if (loggedInUser.getTokenExpiresIn() < new Date().getTime()) {
                //token has expired, remove the objects from shared preferences and send to login
                SharedPreferenceService.clearSharedPreferences(this, PreferenceInformation.PREFERENCE_NAME);

                Intent intent = new Intent(MainActivity.this, CommonContainer.class);
                intent.putExtra("loadingPage", "LOGIN");
                startActivity(intent);
                finish();
            } else {
                //check role and send to the respective home page
            }
        } else {
            //no session is present at all,
            //direct to login
            Intent intent = new Intent(MainActivity.this, CommonContainer.class);
            intent.putExtra("loadingPage", "LOGIN");
            startActivity(intent);
            finish();
        }
    }
}