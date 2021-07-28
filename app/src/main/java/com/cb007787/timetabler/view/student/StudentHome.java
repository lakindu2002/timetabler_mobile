package com.cb007787.timetabler.view.student;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturnDTO;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.snackbar.Snackbar;

public class StudentHome extends AppCompatActivity {

    private ViewGroup studentLayout;
    private AuthReturnDTO loggedInStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);
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
    }

    private void showWelcomeMessage() {
        Snackbar theSnackbar = Snackbar.make(
                studentLayout,
                String.format("Welcome, %s %s", loggedInStudent.getFirstName(), loggedInStudent.getLastName()),
                Snackbar.LENGTH_LONG);
        theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        theSnackbar.show();
    }
}