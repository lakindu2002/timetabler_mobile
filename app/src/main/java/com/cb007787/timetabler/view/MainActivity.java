package com.cb007787.timetabler.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.PasswordReset;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.academic_admin.AcademicAdminHome;
import com.cb007787.timetabler.view.common.CommonContainer;
import com.cb007787.timetabler.view.lecturer.LecturerHome;
import com.cb007787.timetabler.view.student.StudentHome;
import com.cb007787.timetabler.view.system_admin.SystemAdminHome;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //validate the shared preferences.
    //if user is logged in, send to their respected activity, else load the login page.
    private final String[] permissionNeeded = new String[1];

    @Override
    protected void onStart() {
        super.onStart();
        //check for permission for phone call.
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //need permission for phone call.
            ActivityCompat.requestPermissions(MainActivity.this, permissionNeeded, 0);
        } else {
            //permissions granted, launch the app
            launchApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            //request code is the code passed to request permissions method.
            launchApp(); //user has responded to my permission request, launch the app.
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionNeeded[0] = "android.permission.CALL_PHONE"; //phone call permission
    }

    private void navigateToHome(String role) {
        Intent theRoleIntent = null;

        switch (role.toLowerCase().trim()) {
            case "academic administrator": {
                theRoleIntent = new Intent(this, AcademicAdminHome.class);
                break;
            }
            case "system administrator": {
                theRoleIntent = new Intent(this, SystemAdminHome.class);
                break;
            }
            case "lecturer": {
                theRoleIntent = new Intent(this, LecturerHome.class);
                break;
            }
            case "student": {
                theRoleIntent = new Intent(this, StudentHome.class);
                break;
            }
        }
        startActivity(theRoleIntent);
        finish(); //remove the splash screen from activity back trace.
    }

    public void launchApp() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AuthReturn loggedInUser = null;
                PasswordReset resetInfo = null;

                try {
                    loggedInUser = SharedPreferenceService.getLoggedInUser(getApplicationContext(), PreferenceInformation.PREFERENCE_NAME);
                    resetInfo = SharedPreferenceService.getResetInformation(getApplicationContext(), PreferenceInformation.PREFERENCE_NAME);
                } catch (JsonProcessingException e) {
                    Log.i(MainActivity.class.getName(), "ERROR PARSING JSON DURING RETRIEVAL");
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
                        SharedPreferenceService.clearSharedPreferences(getApplicationContext(), PreferenceInformation.PREFERENCE_NAME);
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
                        SharedPreferenceService.clearSharedPreferences(getApplicationContext(), PreferenceInformation.PREFERENCE_NAME);

                        Intent intent = new Intent(MainActivity.this, CommonContainer.class);
                        intent.putExtra("loadingPage", "LOGIN");
                        startActivity(intent);
                        finish();
                    } else {
                        //check role and send to the respective home page
                        navigateToHome(loggedInUser.getRole());
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
        }, 1000);
    }
}