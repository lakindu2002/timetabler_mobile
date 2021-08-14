package com.cb007787.timetabler.view.system_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
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
import com.cb007787.timetabler.view.common.shared.SharedUserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.navigation.NavigationView;

public class SystemAdminHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout systemAdminDrawer;
    private Toolbar theToolbar;
    private NavigationView navigationView;

    private AuthReturn loggedInSystemAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_admin_home);

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);

        try {
            loggedInSystemAdmin = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
        } catch (JsonProcessingException e) {
            Log.i(SystemAdminHome.class.getName(), "ERROR PARSING JSON");
        }

        getReferences();

        ActionBarDrawerToggle theToggle = new ActionBarDrawerToggle(this, systemAdminDrawer, theToolbar, R.string.openDrawer, R.string.closeDrawer);
        DrawerArrowDrawable theDrawerArrowDrawable = new DrawerArrowDrawable(this);
        theDrawerArrowDrawable.setColor(getResources().getColor(R.color.white, null));
        theToggle.setDrawerArrowDrawable(theDrawerArrowDrawable);
        theToggle.syncState();
        systemAdminDrawer.addDrawerListener(theToggle);

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void getReferences() {
        this.systemAdminDrawer = findViewById(R.id.systemAdminLayout);
        this.theToolbar = findViewById(R.id.system_admin_toolbar);
        this.navigationView = findViewById(R.id.navigation_view_system_admin);
        View headerView = navigationView.getHeaderView(0);//retrieve header view
        TextView fullNameHeader = headerView.findViewById(R.id.fullName_header); //configure the full name on nav
        fullNameHeader.setText(String.format("Welcome, %s %s", loggedInSystemAdmin.getFirstName(), loggedInSystemAdmin.getLastName()));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //call back executed when user clicks on any item in the navigation view.
        Intent navigationIntent;
        if (item.getItemId() == R.id.menu_logout) {
            //system admin clicked logout
            //clear all logged in user information.
            closeDrawer();
            SharedPreferenceService.clearSharedPreferences(this, PreferenceInformation.PREFERENCE_NAME);

            //navigate to login page.
            navigationIntent = new Intent(this, CommonContainer.class);
            navigationIntent.putExtra("loadingPage", "login");
            startActivity(navigationIntent);
            finish();

            Toast.makeText(this, "You have successfully logged out", Toast.LENGTH_LONG).show();
            return true;
        } else if (item.getItemId() == R.id.menu_my_account) {
            //system clicked view account details.
            closeDrawer();
            navigationIntent = new Intent(this, SharedUserProfile.class);
            startActivity(navigationIntent);
            return true;
        } else if (item.getItemId() == R.id.menu_user_management) {
            //system admin clicked user management
            closeDrawer();
            navigationIntent = new Intent(this, SystemAdminUserManagement.class);
            startActivity(navigationIntent);
            return true;
        } else if (item.getItemId() == R.id.menu_classroom_management) {
            //system admin has clicked on classroom management
            closeDrawer();
            navigationIntent = new Intent(this, SystemAdminClassroomManagement.class);
            startActivity(navigationIntent);
            return true;
        }
        return false;
    }

    private void closeDrawer() {
        //if drawer is open, close it
        if (systemAdminDrawer.isDrawerOpen(GravityCompat.START)) {
            systemAdminDrawer.closeDrawer(GravityCompat.START);
        }
    }
}