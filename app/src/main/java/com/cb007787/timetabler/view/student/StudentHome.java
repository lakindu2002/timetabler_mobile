package com.cb007787.timetabler.view.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.LectureShow;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.LectureService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.common.CommonContainer;
import com.cb007787.timetabler.view.common.SharedUserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout studentLayout;
    private Toolbar studentToolBar;
    private NavigationView theNavigation;
    private TextView fullNameNavHeader;
    private TextView batchCodeHeader;
    private LinearProgressIndicator progressIndicator;
    private CalendarView theCalendar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button todayButton;

    private LectureService lectureService;
    private String jwtToken;
    private AuthReturn loggedInStudent;

    private Date selectedDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);
        getReferences();

        //check if token is valid
        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        jwtToken = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);

        theNavigation.setNavigationItemSelectedListener(this);

        constructToggle();
        selectedDate = new Date();
        swipeRefreshLayout.setOnRefreshListener(() -> loadMyLectures(selectedDate));

        todayButton.setOnClickListener(theView -> theCalendar.setDate(new Date().getTime()));

        try {
            loggedInStudent = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            assignHeadersInNav();
        } catch (JsonProcessingException e) {
            Log.i(StudentHome.class.getName(), "ERROR PARSING JSON");
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
        swipeRefreshLayout = findViewById(R.id.student_home_swiper);

        //to retrieve references for the text view in header
        //inflate the Nav Header Layout.
        //navigation is passed as it is the parent view group for nav header because nav header is placed in navigation.
        //use the inflated view as the view for the content for the text fields.
        View theHeader = theNavigation.getHeaderView(0); //header 0 to get the header file for the navigation
        this.fullNameNavHeader = theHeader.findViewById(R.id.fullName_header);
        this.batchCodeHeader = theHeader.findViewById(R.id.student_header_batch);
        theCalendar = findViewById(R.id.student_lecture_view);
        progressIndicator = findViewById(R.id.progress_bar);
        batchCodeHeader.setVisibility(View.VISIBLE);
        lectureService = APIConfigurer.getApiConfigurer().getLectureService();
        todayButton = findViewById(R.id.today_btn);
    }

    private void assignHeadersInNav() {
        fullNameNavHeader.setText(String.format("Welcome, %s %s", loggedInStudent.getFirstName(), loggedInStudent.getLastName()));
        batchCodeHeader.setText(String.format("Batch Code - %s", loggedInStudent.getBatchCode()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadMyLectures(selectedDate);
    }

    private void loadMyLectures(Date selectedDate) {
        progressIndicator.setVisibility(View.VISIBLE);

        Call<List<LectureShow>> myLectureCall = lectureService.getMyLectures(jwtToken, new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(selectedDate));
        myLectureCall.enqueue(new Callback<List<LectureShow>>() {
            @Override
            public void onResponse(@NonNull Call<List<LectureShow>> call, @NonNull Response<List<LectureShow>> response) {
                try {
                    handleOnResponse(response);
                    progressIndicator.setVisibility(View.GONE);

                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                } catch (IOException e) {
                    Log.e("loadMyLectures", "FAILED PARSING ERROR BODY");
                    progressIndicator.setVisibility(View.GONE);

                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LectureShow>> call, @NonNull Throwable t) {
                handleError(t);
                progressIndicator.setVisibility(View.GONE);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void handleOnResponse(Response<List<LectureShow>> response) throws IOException {
        if (!response.isSuccessful()) {
            constructError(APIConfigurer.getTheErrorReturned(response.errorBody()).getErrorMessage());
        }
    }

    private void handleError(Throwable t) {
        constructError("An unexpected error occurred while processing your request.");
    }

    private void constructError(String errorMessage) {
        Snackbar theSnackBar = Snackbar.make(theNavigation, errorMessage, Snackbar.LENGTH_LONG);
        theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        theSnackBar.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent navigationIntent = null;
        boolean isSelected = false;

        if (item.getItemId() == R.id.student_logout) {
            navigationIntent = new Intent(this, CommonContainer.class);
            navigationIntent.putExtra("loadingPage", "LOGIN");

            Toast.makeText(this, "You have successfully logged out", Toast.LENGTH_LONG).show();
            SharedPreferenceService.clearSharedPreferences(this, PreferenceInformation.PREFERENCE_NAME);

            isSelected = true;

        } else if (item.getItemId() == R.id.student_profile) {
            navigationIntent = new Intent(this, SharedUserProfile.class);
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