package com.cb007787.timetabler.view.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturnDTO;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.common.CommonContainer;
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
        loadHome();

        try {
            loggedInStudent = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            showWelcomeMessage();
        } catch (JsonProcessingException e) {
            System.out.println("ERROR PARSING JSON");
        }

    }

    private void loadHome() {
        getSupportFragmentManager().beginTransaction().replace(R.id.student_fragment_holder, new StudentHome()).commit();
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
        } else if (item.getItemId() == R.id.student_logout) {
            SharedPreferenceService.clearSharedPreferences(this, PreferenceInformation.PREFERENCE_NAME);
            Intent loginIntent = new Intent(this, CommonContainer.class);
            loginIntent.putExtra("loadingPage", "LOGIN");
            startActivity(loginIntent);
            finish();

            Toast.makeText(
                    getApplicationContext(), "You Have Successfully Logged Out", Toast.LENGTH_LONG
            ).show();

            //log the user out
            return true;
        } else {
            return false; //do not select
        }
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {
        this.onNavigationItemSelected(item);
    }
}