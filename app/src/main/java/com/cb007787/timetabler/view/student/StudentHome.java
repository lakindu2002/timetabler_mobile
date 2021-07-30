package com.cb007787.timetabler.view.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturnDTO;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.common.CommonContainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class StudentHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout studentLayout;
    private AuthReturnDTO loggedInStudent;
    private Toolbar studentToolBar;


    private NavigationView theNavigation;
    private TextView fullNameNavHeader;
    private TextView batchCodeHeader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);
        getReferences();
        theNavigation.setNavigationItemSelectedListener(this);

        constructToggle();

        try {
            loggedInStudent = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            assignHeadersInNav();
        } catch (JsonProcessingException e) {
            System.out.println("ERROR PARSING JSON");
        }

    }

    private void constructToggle() {
        ActionBarDrawerToggle theToggle = new ActionBarDrawerToggle(
                this, this.studentLayout, this.studentToolBar, R.string.openDrawer, R.string.closeDrawer
        );
        theToggle.syncState();
        DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(this);
        drawerArrowDrawable.setColor(getResources().getColor(R.color.white, null));
        theToggle.setDrawerArrowDrawable(drawerArrowDrawable);
        //when drawer opens or closes, toggle the hamburger
        this.studentLayout.addDrawerListener(theToggle);
    }

    private void getReferences() {
        this.studentToolBar = findViewById(R.id.student_toolbar);
        this.studentLayout = findViewById(R.id.student_drawer_layout);
        this.theNavigation = findViewById(R.id.student_navigation);

        //to retrieve references for the text view in header
        //inflate the Nav Header Layout.
        //navigation is passed as it is the parent view group for nav header because nav header is placed in navigation.
        //use the inflated view as the view for the content for the text fields.
        View theHeader = theNavigation.getHeaderView(0); //header 0 to get the header file for the navigation
        this.fullNameNavHeader = theHeader.findViewById(R.id.fullName_header);
        this.batchCodeHeader = theHeader.findViewById(R.id.student_header_batch);
        batchCodeHeader.setVisibility(View.VISIBLE);
    }

    private void assignHeadersInNav() {
        fullNameNavHeader.setText(String.format("Welcome, %s %s", loggedInStudent.getFirstName(), loggedInStudent.getLastName()));
        batchCodeHeader.setText(String.format("Batch Code - %s", loggedInStudent.getBatchCode()));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent navigationIntent = null;
        boolean isSelected = false;

        if (item.getItemId() == R.id.student_logout) {
            navigationIntent = new Intent(this, CommonContainer.class);
            navigationIntent.putExtra("loadingPage", "LOGIN");

            Toast.makeText(this, "You have successfully logged out", Toast.LENGTH_LONG).show();

            isSelected = true;

        } else if (item.getItemId() == R.id.student_profile) {
            navigationIntent = new Intent(this, StudentProfile.class);
            isSelected = true;
        } else if (item.getItemId() == R.id.student_enrolments) {
            navigationIntent = new Intent(this, StudentEnrolments.class);
            isSelected = true;
        }
        if (studentLayout.isDrawerOpen(GravityCompat.START)) {
            //layout open in start (left side), close the drawer
            studentLayout.closeDrawer(GravityCompat.START); //close towards left side
        }

        startActivity(navigationIntent);

        return isSelected;
    }
}