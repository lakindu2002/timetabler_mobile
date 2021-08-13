package com.cb007787.timetabler.view.system_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SystemAdminUserManagement extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private Toolbar theToolbar;
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
        navigationView.setSelectedItemId(R.id.user_admin);
    }

    private void getReferences() {
        this.theToolbar = findViewById(R.id.the_toolbar);
        navigationView = findViewById(R.id.bottom_navigation);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //in the newInstance method, it will create a bundle and set the arguments that can be retrieve in the fragment;
        //arguments passed are the user type that needs to be retrieved from the api call.
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        if (item.getItemId() == R.id.new_user) {
            startActivity(new Intent(this, SystemAdminCreateUser.class));
            return true;
        } else if (item.getItemId() == R.id.user_lecturer) {
            fragmentTransaction
                    .replace(R.id.fragment_container_view, UserLoadingFragment.newInstance("lecturer"))
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.user_student) {
            fragmentTransaction
                    .replace(R.id.fragment_container_view, UserLoadingFragment.newInstance("student"))
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.user_admin) {
            fragmentTransaction
                    .replace(R.id.fragment_container_view, UserLoadingFragment.newInstance("academic_administrator"))
                    .commit();
            return true;
        }
        return false;
    }
}
