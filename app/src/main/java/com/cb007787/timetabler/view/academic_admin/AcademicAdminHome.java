package com.cb007787.timetabler.view.academic_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

public class AcademicAdminHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout theDrawer;
    private Toolbar theToolbar;
    private NavigationView navigationView;
    private CardView studentCard;
    private CardView lecturerCard;
    private CardView moduleCard;
    private CardView batchCard;
    private MaterialTextView studentCount;
    private MaterialTextView lecturerCount;
    private MaterialTextView moduleCount;
    private MaterialTextView batchCount;
    private LinearProgressIndicator linearProgressIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;

    private UserService userService;
    private AuthReturn loggedInAdmin;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_admin_home);
        getReferences();

        //validate jwt
        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);
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
            if (loggedInAdmin != null) {
                View headerFile = navigationView.getHeaderView(0);//one header file so get first one.
                TextView fullName = headerFile.findViewById(R.id.fullName_header); //retrieve reference to text view on header file
                fullName.setText(String.format("Welcome, %s %s", loggedInAdmin.getFirstName(), loggedInAdmin.getLastName()));
            }
        } catch (JsonProcessingException e) {
            Log.i(AcademicAdminHome.class.getName(), "ERROR PARSING JSON");
        }

        navigationView.setNavigationItemSelectedListener(this);

        swipeRefreshLayout.setOnRefreshListener(this::loadDashboard);

        studentCard.setOnClickListener(v -> {
            Intent navigationIntent = new Intent(this, AcademicAdminUserManagement.class);
            navigationIntent.putExtra("loading", "student");
            startActivity(navigationIntent);
        });

        moduleCard.setOnClickListener(v -> {
            startActivity(new Intent(this, AcademicAdminModuleManagement.class));
        });

        lecturerCard.setOnClickListener(v -> {
            Intent navigationIntent = new Intent(this, AcademicAdminUserManagement.class);
            navigationIntent.putExtra("loading", "lecturer");
            startActivity(navigationIntent);
        });

        batchCard.setOnClickListener(v -> {
            startActivity(new Intent(this, AcademicAdministratorBatchManagement.class));
        });
    }

    private void getReferences() {
        this.theDrawer = findViewById(R.id.academic_admin_home_layout);
        this.theToolbar = findViewById(R.id.toolbar);
        this.navigationView = findViewById(R.id.navigation);
        userService = APIConfigurer.getApiConfigurer().getUserService();
        linearProgressIndicator = findViewById(R.id.progress_bar);
        studentCard = findViewById(R.id.student_card);
        lecturerCard = findViewById(R.id.lecturer_card);
        moduleCard = findViewById(R.id.modules_card);
        batchCard = findViewById(R.id.batches_card);
        studentCount = findViewById(R.id.student_count);
        lecturerCount = findViewById(R.id.lecturer_count);
        moduleCount = findViewById(R.id.modules_count);
        batchCount = findViewById(R.id.batch_count);
        swipeRefreshLayout = findViewById(R.id.swiper);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadDashboard();
        Toast.makeText(this, "Click on a card to dive deeper", Toast.LENGTH_SHORT).show();
    }

    private void loadDashboard() {
        linearProgressIndicator.setVisibility(View.VISIBLE);
        userService.getContentForAcademicAdminHome(token).enqueue(new Callback<HashMap<String, Integer>>() {
            @Override
            public void onResponse(@NonNull Call<HashMap<String, Integer>> call, @NonNull Response<HashMap<String, Integer>> response) {
                linearProgressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful()) {
                    HashMap<String, Integer> content = response.body();
                    if (content != null) {
                        studentCount.setText(String.valueOf(content.get("students")));
                        batchCount.setText(String.valueOf(content.get("batches")));
                        lecturerCount.setText(String.valueOf(content.get("lecturers")));
                        moduleCount.setText(String.valueOf(content.get("modules")));
                    }
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while loading the dashboard content", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<HashMap<String, Integer>> call, @NonNull Throwable t) {
                linearProgressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                constructError("We ran into an error while loading the dashboard content", false);
            }
        });
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