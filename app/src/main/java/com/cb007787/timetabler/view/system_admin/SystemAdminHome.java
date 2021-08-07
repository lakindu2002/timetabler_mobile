package com.cb007787.timetabler.view.system_admin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.snackbar.Snackbar;

public class SystemAdminHome extends AppCompatActivity {

    private ViewGroup systemAdminLayout;
    private AuthReturn loggedInSystemAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_admin_home);
        getReferences();

        try {
            loggedInSystemAdmin = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            showWelcomeMessage();
        } catch (JsonProcessingException e) {
            Log.i(SystemAdminHome.class.getName(), "ERROR PARSING JSON");
        }
    }

    private void getReferences() {
        this.systemAdminLayout = findViewById(R.id.systemAdminLayout);
    }

    private void showWelcomeMessage() {
        Snackbar theSnackbar = Snackbar.make(
                systemAdminLayout,
                String.format("Welcome, %s %s", loggedInSystemAdmin.getFirstName(), loggedInSystemAdmin.getLastName()),
                Snackbar.LENGTH_LONG);
        theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        theSnackbar.show();
    }
}