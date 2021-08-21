package com.cb007787.timetabler.view.academic_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.snackbar.Snackbar;

public class AcademicAdminHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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
        } catch (JsonProcessingException e) {
            Log.i(AcademicAdminHome.class.getName(), "ERROR PARSING JSON");
        }

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void getReferences() {
        this.theDrawer = findViewById(R.id.academic_admin_home_layout);
        this.theToolbar = findViewById(R.id.toolbar);
        this.navigationView = findViewById(R.id.navigation);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //callback for the events raised when the user clicks on a nav menu item from the drawer.
        Intent navigationIntent = null;
        if (item.getItemId() == R.id.user_directory_menu) {
            //clicked user directory
            navigationIntent = new Intent(this, AcademicAdminUserManagement.class);
            startActivity(navigationIntent);
        } else if (item.getItemId() == R.id.timetable_management_menu) {
            //click timetable management
            navigationIntent = new Intent(this, AcademicAdminTimeTableManagement.class);
            startActivity(navigationIntent);
        } else if (item.getItemId() == R.id.module_management) {
            //clicked module management
            navigationIntent = new Intent(this, AcademicAdminModuleManagement.class);
            startActivity(navigationIntent);
        } else if (item.getItemId() == R.id.batch_management) {
            //clicked batch management
            navigationIntent = new Intent(this, AcademicAdministratorBatchManagement.class);
            startActivity(navigationIntent);
        } else if (item.getItemId() == R.id.my_account_academic) {
            //click my account
            navigationIntent = new Intent(this, SharedUserProfile.class);
            startActivity(navigationIntent); //navigate to profile view and edit page
        } else if (item.getItemId() == R.id.logout_academic) {
            //clicked logout.
            SharedPreferenceService.clearSharedPreferences(this, PreferenceInformation.PREFERENCE_NAME);
            Toast.makeText(this, "You have successfully logged out", Toast.LENGTH_LONG).show();
            navigationIntent = new Intent(this, CommonContainer.class);
            navigationIntent.putExtra("loadingPage", "login");
            startActivity(navigationIntent);
            finish();
            //clear token and logged in info and navigate to login.
        }
        if (theDrawer.isDrawerOpen(GravityCompat.START)) {
            //drawer open, close
            theDrawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }
}