package com.cb007787.timetabler.view.system_admin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentContainerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class SystemAdminUserManagement extends AppCompatActivity {

    private Toolbar theToolbar;
    private FloatingActionButton createButton;
    private FragmentContainerView fragmentContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_admin_user_management);
        getReferences();

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);

        setSupportActionBar(theToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //create the custom navigate home button on the action bar.
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);
        }

        createButton.setOnClickListener(v -> {
            //once user clicks floating action button, show the create user dialog fragment.
            startActivity(new Intent(this, SystemAdminCreateUser.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //once view renders, initially show all academic admins.
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, new AllAcademicAdministrators())
                .commit();
    }

    private void getReferences() {
        this.theToolbar = findViewById(R.id.the_toolbar);
        this.createButton = findViewById(R.id.create_action_button);
        fragmentContainerView = findViewById(R.id.fragment_container_view);
    }
}
