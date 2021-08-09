package com.cb007787.timetabler.view.lecturer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.common.CommonContainer;
import com.cb007787.timetabler.view.common.SharedUserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.navigation.NavigationView;

public class LecturerHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AuthReturn loggedInLecturer;
    private DrawerLayout lecturerDrawer;
    private NavigationView theLecturerNavigation;
    private Toolbar theToolbar;
    private TextView fullNameHeader; //variables used in header

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_home);
        getReferences(); //retrieve layout references from the layout file.
        try {
            loggedInLecturer = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            addLecturerInformationToNavHeader();
        } catch (JsonProcessingException e) {
            Log.i(LecturerHome.class.getName(), "ERROR PARSING JSON");
        }

        //create the nav toggle
        ActionBarDrawerToggle theNavToggle = new ActionBarDrawerToggle(
                this, lecturerDrawer, theToolbar, R.string.openDrawer, R.string.closeDrawer
        );
        theNavToggle.syncState();

        //create a new hamburger icon
        DrawerArrowDrawable hamburger = new DrawerArrowDrawable(this);
        hamburger.setColor(getColor(R.color.white));
        theNavToggle.setDrawerArrowDrawable(hamburger);
        lecturerDrawer.addDrawerListener(theNavToggle); //add the toggle as a drawer listener to create the animation effect.

        theLecturerNavigation.setNavigationItemSelectedListener(this);

    }

    private void getReferences() {
        this.lecturerDrawer = findViewById(R.id.lecturerLayout);
        this.theLecturerNavigation = findViewById(R.id.lecturer_navigation);
        this.theToolbar = findViewById(R.id.lecturer_toolbar);

        View headerView = theLecturerNavigation.getHeaderView(0);//only one header present
        this.fullNameHeader = headerView.findViewById(R.id.fullName_header);
    }

    private void addLecturerInformationToNavHeader() {
        //set the logged in lecturer first name and last name to the nav header file.
        fullNameHeader.setText(String.format("Welcome, %s %s", loggedInLecturer.getFirstName(), loggedInLecturer.getLastName()));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent navigationIntent = null;

        if (item.getItemId() == R.id.lecturer_logout) {
            Toast.makeText(this, "You have successfully logged out", Toast.LENGTH_LONG).show();
            navigationIntent = new Intent(this, CommonContainer.class);
            navigationIntent.putExtra("loadingPage", "LOGIN");

            //clear logged in user and token.
            SharedPreferenceService.clearSharedPreferences(this, PreferenceInformation.PREFERENCE_NAME);

            startActivity(navigationIntent);
            finish();

            return true;

        } else if (item.getItemId() == R.id.lecturer_profile) {
            navigationIntent = new Intent(this, SharedUserProfile.class);
            startActivity(navigationIntent);
            return true;
        }

        return false;
    }
}