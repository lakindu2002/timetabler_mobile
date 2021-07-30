package com.cb007787.timetabler.view.student;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.cb007787.timetabler.R;

public class StudentEnrolments extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_enrolments);
        getReferences();

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getReferences() {
        toolbar = findViewById(R.id.student_toolbar_enrolment);
    }
}