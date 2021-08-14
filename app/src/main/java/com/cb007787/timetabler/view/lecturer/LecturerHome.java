package com.cb007787.timetabler.view.lecturer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.cb007787.timetabler.recyclers.LectureRecycler;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.LectureService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.common.CommonContainer;
import com.cb007787.timetabler.view.common.shared.SharedUserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AuthReturn loggedInLecturer;
    private String token;
    private LectureService lectureService;
    private List<LectureShow> lectureList = new ArrayList<>();

    private DrawerLayout lecturerDrawer;
    private NavigationView theLecturerNavigation;
    private Toolbar theToolbar;
    private TextView fullNameHeader; //variables used in header
    private CalendarView calendarView;
    private Button todayButton;
    private LinearProgressIndicator loadingBar;
    private RecyclerView loadedLectures;
    private LectureRecycler adapter;
    private TextView noLectureBanner;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_home);
        getReferences(); //retrieve layout references from the layout file.

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME); //if token is valid, else logout
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);

        try {
            loggedInLecturer = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            addLecturerInformationToNavHeader();
        } catch (JsonProcessingException e) {
            Log.i(LecturerHome.class.getName(), "ERROR PARSING JSON");
        }

        calendarView.setDate(new Date().getTime()); //initially set current date.

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                //when user selects a new date on the calendar.
                Calendar selectedDate = Calendar.getInstance();
                //set the selected date values and get lectures for the selected date
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                getLecturesForSelectedDate(selectedDate.getTime());
            }
        });

        todayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when calendar clicks today, set date to current date and get lectures for current date.
                Date currentDate = new Date();
                calendarView.setDate(currentDate.getTime());
                getLecturesForSelectedDate(currentDate);
            }
        });


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

        layoutManager = new LinearLayoutManager(this);
        adapter = new LectureRecycler(this, lectureList);
        LectureRecycler.setUserRole(loggedInLecturer.getRole()); //set user role to allow for deleting and updating.
        loadedLectures.setAdapter(adapter); //show the lectures below the calendar.
        loadedLectures.setLayoutManager(layoutManager); //assign a linear layout for the recycler view.
    }

    private void getReferences() {
        this.lecturerDrawer = findViewById(R.id.lecturerLayout);
        this.theLecturerNavigation = findViewById(R.id.lecturer_navigation);
        this.theToolbar = findViewById(R.id.lecturer_toolbar);
        calendarView = findViewById(R.id.lecturer_lecture_view);
        todayButton = findViewById(R.id.today_btn);
        lectureService = APIConfigurer.getApiConfigurer().getLectureService();
        loadingBar = findViewById(R.id.progress_bar);
        loadedLectures = findViewById(R.id.loaded_lectures);
        noLectureBanner = findViewById(R.id.no_lecture);

        View headerView = theLecturerNavigation.getHeaderView(0);//only one header present
        this.fullNameHeader = headerView.findViewById(R.id.fullName_header);
    }

    private void addLecturerInformationToNavHeader() {
        //set the logged in lecturer first name and last name to the nav header file.
        fullNameHeader.setText(String.format("Welcome, %s %s", loggedInLecturer.getFirstName(), loggedInLecturer.getLastName()));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (lecturerDrawer.isDrawerOpen(GravityCompat.START)) {
            lecturerDrawer.closeDrawer(GravityCompat.START);
        }
        Intent navigationIntent;

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
        } else if (item.getItemId() == R.id.lecturer_modules) {
            navigationIntent = new Intent(this, LecturerModules.class);
            startActivity(navigationIntent);
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //get lectures for selected date.
        getLecturesForSelectedDate(new Date());
    }

    public void getLecturesForSelectedDate(Date selectedDate) {
        calendarView.setDate(selectedDate.getTime());
        loadingBar.setVisibility(View.VISIBLE);
        Call<List<LectureShow>> myLectureCall = lectureService.getMyLectures(token, new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(selectedDate));
        myLectureCall.enqueue(new Callback<List<LectureShow>>() {
            @Override
            public void onResponse(@NonNull Call<List<LectureShow>> call, @NonNull Response<List<LectureShow>> response) {
                loadingBar.setVisibility(View.GONE);
                try {
                    handleOnResponse(response);
                } catch (IOException e) {
                    Log.e("onResponse", e.getLocalizedMessage());
                    constructError("We ran into an error while fetching your lectures");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LectureShow>> call, @NonNull Throwable t) {
                constructError(t.getLocalizedMessage());
                loadedLectures.setVisibility(View.GONE);
                noLectureBanner.setVisibility(View.VISIBLE);
                loadingBar.setVisibility(View.GONE);
            }
        });
    }

    private void handleOnResponse(Response<List<LectureShow>> response) throws IOException {
        loadedLectures.setVisibility(View.VISIBLE);
        noLectureBanner.setVisibility(View.GONE);

        if (response.isSuccessful()) {
            if (response.body().size() == 0) {
                //no lectures
                loadedLectures.setVisibility(View.GONE);
                noLectureBanner.setVisibility(View.VISIBLE);
            } else {
                //have lectures
                loadedLectures.setVisibility(View.VISIBLE);
                noLectureBanner.setVisibility(View.GONE);
                adapter.setTheLectureList(response.body());
            }
        } else {
            loadedLectures.setVisibility(View.GONE);
            noLectureBanner.setVisibility(View.VISIBLE);
            constructError(APIConfigurer.getTheErrorReturned(response.errorBody()).getErrorMessage());
        }
    }

    private void constructError(String errorMessage) {
        Snackbar theSnackBar = Snackbar.make(theToolbar, errorMessage, Snackbar.LENGTH_LONG);
        theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        View view = theSnackBar.getView();
        //retrieve the underling text view on the snack bar and increase the lines on it to display full message
        TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
        snackBarText.setMaxLines(5);
        theSnackBar.show();
    }
}