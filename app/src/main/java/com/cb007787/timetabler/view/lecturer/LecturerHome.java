package com.cb007787.timetabler.view.lecturer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturnDTO;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.system_admin.SystemAdminHome;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.snackbar.Snackbar;

public class LecturerHome extends AppCompatActivity {

    private AuthReturnDTO loggedInLecturer;
    private ViewGroup lecturerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_home);
        getReferences();
        try {
            loggedInLecturer = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            showWelcomeMessage();
        } catch (JsonProcessingException e) {
            Log.i(LecturerHome.class.getName(), "ERROR PARSING JSON");
        }
    }

    private void getReferences() {
        this.lecturerLayout = findViewById(R.id.lecturerLayout);
    }

    private void showWelcomeMessage() {
        Snackbar theSnackbar = Snackbar.make(
                lecturerLayout,
                String.format("Welcome, %s %s", loggedInLecturer.getFirstName(), loggedInLecturer.getLastName()),
                Snackbar.LENGTH_LONG);
        theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        theSnackbar.show();
    }
}