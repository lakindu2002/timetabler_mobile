package com.cb007787.timetabler.view.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturnDTO;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class StudentContainer extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemReselectedListener {

    private ViewGroup studentLayout;
    private AuthReturnDTO loggedInStudent;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_container);
        getReferences();

        try {
            loggedInStudent = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            showWelcomeMessage();
        } catch (JsonProcessingException e) {
            System.out.println("ERROR PARSING JSON");
        }

    }

    private void getReferences() {
        this.studentLayout = findViewById(R.id.studentLayout);
        this.bottomNavigationView = findViewById(R.id.student_navigation);
        this.bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    private void showWelcomeMessage() {
        Snackbar theSnackbar = Snackbar.make(
                studentLayout,
                String.format("Welcome, %s %s", loggedInStudent.getFirstName(), loggedInStudent.getLastName()),
                Snackbar.LENGTH_LONG);
        theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        theSnackbar.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();

        if (item.getItemId() == R.id.student_home) {
            supportFragmentManager.beginTransaction().replace(R.id.student_fragment_holder, new StudentHome()).commit();
            return true; //select nav item
        } else if (item.getItemId() == R.id.student_enrolments) {
            supportFragmentManager.beginTransaction().replace(R.id.student_fragment_holder, new StudentEnrolments()).commit();
            return true;  //select nav item
        } else if (item.getItemId() == R.id.student_profile) {
            supportFragmentManager.beginTransaction().replace(R.id.student_fragment_holder, new StudentProfile()).commit();
            return true; //select nav item
        } else {
            return false; //do not select
        }
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {
        this.onNavigationItemSelected(item);
    }
}