package com.cb007787.timetabler.view.academic_admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturnDTO;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.snackbar.Snackbar;

public class AcademicAdminHome extends AppCompatActivity {

    private AuthReturnDTO loggedInAdmin;
    private ViewGroup academicAdminLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_admin_home);
        getReferences();
        try {
            loggedInAdmin = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            showWelcomeMessage();
        } catch (JsonProcessingException e) {
            System.out.println("ERROR PARSING JSON");
        }
    }

    private void getReferences() {
        this.academicAdminLayout = findViewById(R.id.academic_admin_home_layout);
    }

    private void showWelcomeMessage() {
        Snackbar theSnackbar = Snackbar.make(
                academicAdminLayout,
                String.format("Welcome, %s %s", loggedInAdmin.getFirstName(), loggedInAdmin.getLastName()),
                Snackbar.LENGTH_LONG);
        theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        theSnackbar.show();
    }
}