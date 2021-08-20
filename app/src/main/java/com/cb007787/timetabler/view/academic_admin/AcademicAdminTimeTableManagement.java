package com.cb007787.timetabler.view.academic_admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cb007787.timetabler.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

public class AcademicAdminTimeTableManagement extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar theToolbar;
    private MaterialTextView timeTableType;

    private AcademicAdminLectureView academicAdminLectureView;
    private boolean loadModuleForSchedule = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_admin_time_table_management);

        getReferences();

        setSupportActionBar(theToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //create a back button
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            //on click listener for nav items.
            if (item.getItemId() == R.id.batch_timetables) {
                //load batches with lectures scheduled
                timeTableType.setText("Batches With Lectures");
                //construct to show batches with lectures
                academicAdminLectureView = AcademicAdminLectureView.newInstance("batch");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, academicAdminLectureView).commit();
            } else if (item.getItemId() == R.id.lecturer_timetables) {
                //lecturer timetables
                timeTableType.setText("Lecturer Timetables");
                //construct to show lecturer all lecturers to view timetables for.
                academicAdminLectureView = AcademicAdminLectureView.newInstance("lecturer");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, academicAdminLectureView).commit();
            } else if (item.getItemId() == R.id.schedule_a_lecture) {
                //academic admin clicked schedule lecture
                timeTableType.setText("Modules To Schedule For");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, AcademicAdminModulesWithLecturersAndAdmins.newInstance()).commit();
            }
            return true;
        });

        Intent intent = getIntent();
        if (intent != null) {
            //check if academic admin has scheduled a lecture
            //this is done by passing a boolean for "loadModulesForSchedule" in the ScheduleLecture activity in the common pacakage
            //the is added inside the handleOnResponse method after lecture has been saved
            if (intent.getBooleanExtra("loadModulesForSchedule", false)) {
                //if loadModulesForSchedule is true, show lecture schedule helper
                loadModuleForSchedule = intent.getBooleanExtra("loadModulesForSchedule", false);
            }
        }
    }

    private void getReferences() {
        bottomNavigationView = findViewById(R.id.bottom_nav);
        theToolbar = findViewById(R.id.tool_bar);
        timeTableType = findViewById(R.id.timetable_type);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!loadModuleForSchedule) {
            bottomNavigationView.setSelectedItemId(R.id.batch_timetables); //load batch timetables initially.
        } else {
            bottomNavigationView.setSelectedItemId(R.id.schedule_a_lecture); //load schedule modules.
        }
    }
}