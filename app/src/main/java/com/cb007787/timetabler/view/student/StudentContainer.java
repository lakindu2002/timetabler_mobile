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
import android.view.Gravity;
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

public class StudentContainer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout studentLayout;
    private AuthReturnDTO loggedInStudent;
    private Toolbar studentToolBar;


    private NavigationView theNavigation;
    private TextView usernameNavHeader;
    private TextView fullNameNavHeader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_container);
        getReferences();
        theNavigation.setNavigationItemSelectedListener(this);

        constructToggle();

        try {
            loggedInStudent = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            showWelcomeMessageAndAssignHeadersInNav();
        } catch (JsonProcessingException e) {
            System.out.println("ERROR PARSING JSON");
        }

    }

    private void constructToggle() {
        ActionBarDrawerToggle theToggle = new ActionBarDrawerToggle(
                this, this.studentLayout, this.studentToolBar, R.string.openDrawer, R.string.closeDrawer
        );
        theToggle.syncState();
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
        this.usernameNavHeader = theHeader.findViewById(R.id.username_header);
        this.fullNameNavHeader = theHeader.findViewById(R.id.fullName_header);
    }

    private void showWelcomeMessageAndAssignHeadersInNav() {
        Snackbar theSnackbar = Snackbar.make(
                studentLayout,
                String.format("Welcome, %s %s", loggedInStudent.getFirstName(), loggedInStudent.getLastName()),
                Snackbar.LENGTH_LONG);
        theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        theSnackbar.show();

        usernameNavHeader.setText(loggedInStudent.getUsername());
        fullNameNavHeader.setText(String.format("%s %s", loggedInStudent.getFirstName(), loggedInStudent.getLastName()));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent navigationIntent = null;
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        boolean isSelected = false;

        if (item.getItemId() == R.id.student_logout) {
            navigationIntent = new Intent(this, CommonContainer.class);
            navigationIntent.putExtra("loadingPage", "LOGIN");

            Toast.makeText(this, "You have successfully logged out", Toast.LENGTH_LONG).show();

            isSelected = true;

            startActivity(navigationIntent);
            finish(); //remove current activity from stack

        } else if (item.getItemId() == R.id.student_profile) {
            supportFragmentManager.beginTransaction().replace(R.id.student_fragment_holder, new StudentProfile()).commit();
            studentToolBar.setTitle(R.string.profile);
            isSelected = true;
        } else if (item.getItemId() == R.id.student_enrolments) {
            supportFragmentManager.beginTransaction().replace(R.id.student_fragment_holder, new StudentEnrolments()).commit();
            studentToolBar.setTitle(R.string.student_enrolments);
            isSelected = true;
        } else if (item.getItemId() == R.id.student_home) {
            supportFragmentManager.beginTransaction().replace(R.id.student_fragment_holder, new StudentHome()).commit();
            studentToolBar.setTitle(R.string.student_home);
            isSelected = true;
        }

        if (studentLayout.isDrawerOpen(GravityCompat.START)) {
            //layout open in start (left side), close the drawer
            studentLayout.closeDrawer(GravityCompat.START); //close towards left side
        }
        return isSelected;
    }
}