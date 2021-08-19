package com.cb007787.timetabler.view.academic_admin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;

import com.cb007787.timetabler.R;

public class AcademicAdminViewLecturesPerBatch extends AppCompatActivity {

    private String batchCode;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_admin_view_lectures_per_batch);
        getReferences();

        Intent intent = getIntent();
        if (intent != null) {
            batchCode = intent.getStringExtra("batchCode");
        }

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        }
    }

    private void getReferences() {
        toolbar = findViewById(R.id.tool_bar);
    }
}