package com.cb007787.timetabler.view.system_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.common.shared.UserLoadingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SystemAdminUserManagement extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private Toolbar theToolbar;
    private BottomNavigationView navigationView;
    private UserLoadingFragment theLoadedFragment;
    private String loadingFromDashboard; //variable to identify if user is coming to activity from a card in the dashboard.

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

        Intent intent = getIntent();
        if (intent != null) {
            loadingFromDashboard = intent.getStringExtra("load");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //once view renders, initially show all academic admins.
        navigationView.setSelectedItemId(R.id.user_admin);
        if (loadingFromDashboard != null) {
            if (loadingFromDashboard.equalsIgnoreCase("academic-admin")) {
                navigationView.setSelectedItemId(R.id.user_admin);
            }
            if (loadingFromDashboard.equalsIgnoreCase("students")) {
                navigationView.setSelectedItemId(R.id.user_student);
            }
            if (loadingFromDashboard.equalsIgnoreCase("lecturers")) {
                navigationView.setSelectedItemId(R.id.user_lecturer);
            }
        }
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
            theLoadedFragment = UserLoadingFragment.newInstance("lecturer");
            fragmentTransaction
                    .replace(R.id.fragment_container_view, theLoadedFragment)
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.user_student) {
            theLoadedFragment = UserLoadingFragment.newInstance("student");
            fragmentTransaction
                    .replace(R.id.fragment_container_view, theLoadedFragment)
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.user_admin) {
            theLoadedFragment = UserLoadingFragment.newInstance("academic_administrator");
            fragmentTransaction
                    .replace(R.id.fragment_container_view, theLoadedFragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search); //the search menu item will be instantiated as a SearchView as defined in menu file
        SearchView actionView = (SearchView) searchMenuItem.getActionView(); //retrieve the underlying SearchView for the menu item.
        actionView.setBackgroundColor(getResources().getColor(R.color.white, null)); //set background to white.
        actionView.setQueryHint("Provide Username");

        actionView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //text input submitted
                theLoadedFragment.search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //keyboard click detected
                theLoadedFragment.search(newText);
                return true;
            }
        });
        return true;
    }

}
