package com.cb007787.timetabler.view.student;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;

public class StudentUserProfile extends AppCompatActivity {

    private Toolbar toolbar;
    private AuthReturn loggedInUser;
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        try {
            loggedInUser = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            jwtToken = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);
        } catch (JsonProcessingException e) {
            Log.i(StudentUserProfile.class.getName(), "Failed Parsing JSON");
        }

        getReferences();
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);
        }
    }

    private void getReferences() {
        toolbar = findViewById(R.id.student_toolbar_profile);
    }
}