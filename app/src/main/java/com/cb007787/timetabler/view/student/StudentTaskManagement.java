package com.cb007787.timetabler.view.student;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;

public class StudentTaskManagement extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView taskRecycler;
    private AuthReturn loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_task_management);

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);

        getReferences();

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //create back button
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getReferences() {
        toolbar = findViewById(R.id.tasks_toolbar);
        try {
            loggedInUser = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}