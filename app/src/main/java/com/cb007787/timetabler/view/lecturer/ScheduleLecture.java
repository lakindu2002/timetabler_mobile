package com.cb007787.timetabler.view.lecturer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.Classroom;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.LectureService;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleLecture extends AppCompatActivity {

    private int moduleIdPassedFromIntent;
    private String token;
    private List<Classroom> loadedClassrooms;
    private Module loadedModule;

    private LectureService lectureService;
    private Toolbar theToolbar;
    private LinearProgressIndicator loadingBar;
    private SwipeRefreshLayout swiper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_lecture);
        getReferences();

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);

        Intent intent = getIntent();
        if (intent != null) {
            this.moduleIdPassedFromIntent = intent.getIntExtra("theModuleId", 0);
        } else {
            this.moduleIdPassedFromIntent = 0;
        }

        setSupportActionBar(theToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24, null));
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getModuleForLectureSchedule();
            }
        });
    }

    private void getReferences() {
        this.lectureService = APIConfigurer.getApiConfigurer().getLectureService();
        this.theToolbar = findViewById(R.id.toolbar);
        this.loadingBar = findViewById(R.id.progress_bar);
        this.swiper = findViewById(R.id.swiper);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //load module and enrolled batch information and the classroom list to schedule lecture
        getModuleForLectureSchedule();
    }

    private void getModuleForLectureSchedule() {
        loadingBar.setVisibility(View.VISIBLE);

        Call<HashMap<String, Object>> classroomAndModuleCall = lectureService.getModuleInformationAndClassroomForSchedule(token, moduleIdPassedFromIntent);
        classroomAndModuleCall.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<HashMap<String, Object>> call, @NonNull Response<HashMap<String, Object>> response) {
                loadingBar.setVisibility(View.GONE);
                if (swiper.isRefreshing()) {
                    swiper.setRefreshing(false);
                }
                try {
                    handleOnResponse(response);
                } catch (IOException e) {
                    Log.e("onResponse", e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<HashMap<String, Object>> call, @NonNull Throwable t) {
                loadingBar.setVisibility(View.GONE);
                if (swiper.isRefreshing()) {
                    swiper.setRefreshing(false);
                }
                constructError(t.getLocalizedMessage(), false);
            }
        });
    }

    private void handleOnResponse(Response<HashMap<String, Object>> response) throws IOException {
        if (response.isSuccessful()) {
            //key present in the HashMap sent from API - /findModuleAndClassroomsForLectureSchedule/{moduleId}
            //Lecture API
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, Object> responseBody = response.body();
            //type reference used to obtain the type of the generic class.
            loadedClassrooms = objectMapper.convertValue(responseBody.get("theClassroomList"), new TypeReference<List<Classroom>>() {
            });
            loadedModule = objectMapper.convertValue(responseBody.get("theModuleInformation"), Module.class);

            showInView();
        } else {
            constructError(
                    APIConfigurer.getTheErrorReturned(response.errorBody()).getErrorMessage(), true
            );
        }
    }

    private void showInView() {
        //show module and classrooms in view and ask to schedule lecture.
    }

    private void constructError(String errorMessage, boolean isInfo) {
        Snackbar theSnackBar = Snackbar.make(theToolbar, errorMessage, Snackbar.LENGTH_LONG);
        if (!isInfo) {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        } else {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        }
        theSnackBar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //after press back go back to previous page.
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        //after press back go back to previous page.
        super.onBackPressed();
    }
}