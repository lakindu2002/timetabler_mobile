package com.cb007787.timetabler.view.system_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentContainerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class SystemAdminUserManagement extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private Toolbar theToolbar;
    private FragmentContainerView fragmentContainerView;
    private BottomNavigationView navigationView;

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

        navigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //once view renders, initially show all academic admins.
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, new AllAcademicAdministrators())
                .commit();

        navigationView.setSelectedItemId(R.id.user_admin);
    }

    private void getReferences() {
        this.theToolbar = findViewById(R.id.the_toolbar);
        fragmentContainerView = findViewById(R.id.fragment_container_view);
        navigationView = findViewById(R.id.bottom_navigation);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.new_user) {
            startActivity(new Intent(this, SystemAdminCreateUser.class));
            return true;
        }
        return false;
    }
}
