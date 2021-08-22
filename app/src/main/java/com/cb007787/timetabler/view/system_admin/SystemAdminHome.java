package com.cb007787.timetabler.view.system_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.service.UserService;
import com.cb007787.timetabler.view.common.CommonContainer;
import com.cb007787.timetabler.view.common.shared.SharedUserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SystemAdminHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout systemAdminDrawer;
    private Toolbar theToolbar;
    private NavigationView navigationView;
    private CardView academicAdminCard;
    private CardView lecturerCard;
    private CardView studentCard;
    private CardView classroomsCard;
    private MaterialTextView academicAdminCount;
    private MaterialTextView lecturerCount;
    private MaterialTextView studentCount;
    private MaterialTextView classroomCount;
    private LinearProgressIndicator linearProgressIndicator;
    private SwipeRefreshLayout swiper;

    private AuthReturn loggedInSystemAdmin;
    private UserService userService;
    private String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_admin_home);

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);
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

        swiper.setOnRefreshListener(this::loadCardContent);

        classroomsCard.setOnClickListener(v -> {
            //navigate to classroom management
            startActivity(new Intent(this, SystemAdminClassroomManagement.class));
        });

        academicAdminCard.setOnClickListener(v -> {
            //load academic admin on the fragment in user management
            Intent navigation = new Intent(this, SystemAdminUserManagement.class);
            navigation.putExtra("load", "academic-admin");
            startActivity(navigation);
        });

        studentCard.setOnClickListener(v -> {
            //load students on fragment in user management
            Intent navigation = new Intent(this, SystemAdminUserManagement.class);
            navigation.putExtra("load", "students");
            startActivity(navigation);
        });

        lecturerCard.setOnClickListener(v -> {
            //load lecturers in the fragment for user management
            Intent navigation = new Intent(this, SystemAdminUserManagement.class);
            navigation.putExtra("load", "lecturers");
            startActivity(navigation);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadCardContent();
        constructError("Click on a card to dive deeper", true);
    }

    private void getReferences() {
        this.systemAdminDrawer = findViewById(R.id.systemAdminLayout);
        this.theToolbar = findViewById(R.id.system_admin_toolbar);
        this.navigationView = findViewById(R.id.navigation_view_system_admin);
        View headerView = navigationView.getHeaderView(0);//retrieve header view
        TextView fullNameHeader = headerView.findViewById(R.id.fullName_header); //configure the full name on nav
        if (loggedInSystemAdmin != null) {
            fullNameHeader.setText(String.format("Welcome, %s %s", loggedInSystemAdmin.getFirstName(), loggedInSystemAdmin.getLastName()));
        }
        academicAdminCard = findViewById(R.id.academic_admin_card);
        studentCard = findViewById(R.id.students_card);
        lecturerCard = findViewById(R.id.lecturer_card);
        classroomsCard = findViewById(R.id.classrooms_card);
        academicAdminCount = findViewById(R.id.academic_admin_count);
        studentCount = findViewById(R.id.students_count);
        lecturerCount = findViewById(R.id.lecturer_count);
        classroomCount = findViewById(R.id.classrooms_count);
        linearProgressIndicator = findViewById(R.id.progress_bar);
        userService = APIConfigurer.getApiConfigurer().getUserService();
        swiper = findViewById(R.id.swiper);
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

    private void loadCardContent() {
        linearProgressIndicator.setVisibility(View.VISIBLE);
        userService.getContentForSystemAdminHome(token).enqueue(new Callback<HashMap<String, Integer>>() {
            @Override
            public void onResponse(@NonNull Call<HashMap<String, Integer>> call, @NonNull Response<HashMap<String, Integer>> response) {
                linearProgressIndicator.setVisibility(View.GONE);
                if (swiper.isRefreshing()) {
                    swiper.setRefreshing(false);
                }
                if (response.isSuccessful()) {
                    //show data on cards
                    HashMap<String, Integer> responseCount = response.body();
                    if (responseCount != null) {
                        academicAdminCount.setText(String.valueOf(responseCount.get("academic-admins")));
                        lecturerCount.setText(String.valueOf(responseCount.get("lecturers")));
                        studentCount.setText(String.valueOf(responseCount.get("students")));
                        classroomCount.setText(String.valueOf(responseCount.get("classrooms")));
                    }
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while loading dashboard information", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<HashMap<String, Integer>> call, @NonNull Throwable t) {
                linearProgressIndicator.setVisibility(View.GONE);
                if (swiper.isRefreshing()) {
                    swiper.setRefreshing(false);
                }
                constructError("We ran into an error while loading dashboard information", false);
            }
        });
    }

    private void constructError(String errorMessage, boolean isInfo) {
        Snackbar theSnackBar = Snackbar.make(theToolbar, errorMessage, Snackbar.LENGTH_LONG);
        if (isInfo) {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        } else {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        }
        View view = theSnackBar.getView();
        //retrieve the underling text view on the snack bar and increase the lines on it to display full message
        TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
        snackBarText.setMaxLines(5);
        theSnackBar.show();
    }
}