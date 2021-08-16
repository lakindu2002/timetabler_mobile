package com.cb007787.timetabler.view.academic_admin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class AcademicAdminHome extends AppCompatActivity {

    private AuthReturn loggedInAdmin;
    private DrawerLayout theDrawer;
    private Toolbar theToolbar;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_admin_home);
        getReferences();

        //validate jwt
        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);

        setSupportActionBar(theToolbar);

        //create a toggle to slide nav bar.
        ActionBarDrawerToggle theToggle = new ActionBarDrawerToggle(this, theDrawer, theToolbar, R.string.openDrawer, R.string.closeDrawer);
        theToggle.syncState();
        DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(this); //create white color hamburger
        drawerArrowDrawable.setColor(ActivityCompat.getColor(this, R.color.white));
        theToggle.setDrawerArrowDrawable(drawerArrowDrawable);
        theDrawer.addDrawerListener(theToggle);

        try {
            loggedInAdmin = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            //add the welcome message on the header
            View headerFile = navigationView.getHeaderView(0);//one header file so get first one.
            TextView fullName = headerFile.findViewById(R.id.fullName_header); //retrieve reference to text view on header file
            fullName.setText(String.format("Welcome, %s %s", loggedInAdmin.getFirstName(), loggedInAdmin.getLastName()));
            ;
        } catch (JsonProcessingException e) {
            Log.i(AcademicAdminHome.class.getName(), "ERROR PARSING JSON");
        }
    }

    private void getReferences() {
        this.theDrawer = findViewById(R.id.academic_admin_home_layout);
        this.theToolbar = findViewById(R.id.toolbar);
        this.navigationView = findViewById(R.id.navigation);


    }
}