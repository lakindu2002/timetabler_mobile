package com.cb007787.timetabler.view.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.recyclers.ModuleRecycler;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentEnrolments extends AppCompatActivity {

    private Toolbar toolbar;
    private String token;
    private ModuleService moduleService;
    private LinearProgressIndicator progressIndicator;

    private SwipeRefreshLayout swiper;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_enrolments);
        getReferences();

        //check if token is valid
        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);

        this.token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);

        swiper.setOnRefreshListener(() -> {
            //when swipe is done
            getStudentModulesInBatch();
        });

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        //get the modules followed for the student
        super.onStart();
        getStudentModulesInBatch();
    }

    private void getStudentModulesInBatch() {
        progressIndicator.setVisibility(View.VISIBLE);

        Call<List<Module>> allModulesForUser = moduleService.getAllModulesForUser(token);
        allModulesForUser.enqueue(new Callback<List<Module>>() {
            @Override
            public void onResponse(@NonNull Call<List<Module>> call, @NonNull Response<List<Module>> response) {
                //code sent from api
                try {
                    handleOnResponse(response);
                    if (swiper.isRefreshing()) {
                        swiper.setRefreshing(false);
                    }
                } catch (IOException e) {
                    Log.e(StudentEnrolments.class.getName(), "FAILED PARSING JSON ERROR");
                    progressIndicator.setVisibility(View.GONE);
                    constructError("An unexpected error occurred while fetching modules your batch has.");
                    if (swiper.isRefreshing()) {
                        swiper.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Module>> call, @NonNull Throwable t) {
                //error send or parsing response
                handleError(t);
                progressIndicator.setVisibility(View.GONE);
                if (swiper.isRefreshing()) {
                    swiper.setRefreshing(false);
                }
            }
        });
    }

    private void handleError(Throwable t) {
        constructError("An unexpected error occurred while fetching modules your batch has.");
    }

    private void constructError(String errorMessage) {
        Snackbar theSnackBar = Snackbar.make(toolbar, errorMessage, Snackbar.LENGTH_LONG);
        theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        View view = theSnackBar.getView();
        //retrieve the underling text view on the snack bar and increase the lines on it to display full message
        TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
        snackBarText.setMaxLines(5);
        theSnackBar.show();
    }

    private void handleOnResponse(Response<List<Module>> response) throws IOException {
        if (response.isSuccessful()) {
            //modules received
            List<Module> theModulesInStudentsBatch = response.body();
            if (theModulesInStudentsBatch.size() > 0) {
                showRecycler(theModulesInStudentsBatch);
            } else {
                Snackbar theSnackBar = Snackbar.make(toolbar, "There are no modules enrolled to your batch.", Snackbar.LENGTH_LONG);
                theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
                theSnackBar.show();
            }
        } else {
            //error from api
            ErrorResponseAPI theErrorResponse = APIConfigurer.getTheErrorReturned(response.errorBody());
            constructError(theErrorResponse.getErrorMessage());
        }
        progressIndicator.setVisibility(View.GONE);
    }

    private void showRecycler(List<Module> theModulesInStudentsBatch) {
        ModuleRecycler moduleAdapter = new ModuleRecycler(theModulesInStudentsBatch, this);
        recyclerView.setAdapter(moduleAdapter);
        recyclerView.setLayoutManager(recyclerLayout);
    }

    private void getReferences() {
        toolbar = findViewById(R.id.student_toolbar_enrolment);
        moduleService = APIConfigurer.getApiConfigurer().getModuleService();
        progressIndicator = findViewById(R.id.progress_bar);
        swiper = findViewById(R.id.enrolment_swiper);
        recyclerView = findViewById(R.id.module_recycler);
        recyclerLayout = new LinearLayoutManager(this);
    }
}