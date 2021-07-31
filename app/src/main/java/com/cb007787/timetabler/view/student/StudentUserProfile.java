package com.cb007787.timetabler.view.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.User;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentUserProfile extends AppCompatActivity {

    private Toolbar toolbar;
    private AuthReturn loggedInUser;
    private String jwtToken;
    private UserService theUserService;
    private ProgressBar loadingBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        try {
            loggedInUser = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            jwtToken = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);
        } catch (JsonProcessingException e) {
            Log.i(StudentUserProfile.class.getName(), "Failed Parsing JSON");
        }

        getReferences();
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoggedInUserInformation();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLoggedInUserInformation();
    }

    private void getLoggedInUserInformation() {
        loadingBar.setVisibility(View.VISIBLE);

        Call<User> userInformation = theUserService.getUserInformation(jwtToken, loggedInUser.getUsername());
        userInformation.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                //the response given by rest api
                try {
                    handleOnResponse(response);
                } catch (IOException e) {
                    Log.e(StudentUserProfile.class.getName(), "FAILED PARSING JSON ON API CALL BACK");
                    stopRefreshing();
                    loadingBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                handleOnFailure(t);
                stopRefreshing();
                //in case of network error or response processing error
            }
        }); //execute the operation asynchronously because http methods needs to occur on secondary thread
    }

    private void handleOnFailure(Throwable t) {
        constructError("An unexpected error occurred while fetching account information.");
        Log.e(StudentUserProfile.class.getName(), t.getLocalizedMessage());
        loadingBar.setVisibility(View.GONE);
    }

    private void handleOnResponse(Response<User> response) throws IOException {
        if (response.isSuccessful()) {
            //returned response code is 200 to 300
            User userInformation = response.body(); //the returned success body. API only returns success with HTTP OK (200)
            Log.i("User", userInformation.toString());
        } else {
            if (response.code() == 404) {
                //endpoint not found
                constructError("The resource you are trying to access does not exist");
            } else {
                //returned an error
                ErrorResponseAPI errorResponseAPI = APIConfigurer.getTheErrorReturned(response.errorBody());
                constructError(errorResponseAPI.getErrorMessage());
            }
        }
        loadingBar.setVisibility(View.GONE);
    }

    private void constructError(String errorMessage) {
        Snackbar theSnackBar = Snackbar.make(toolbar, errorMessage, Snackbar.LENGTH_LONG);
        theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        theSnackBar.show();
    }

    private void stopRefreshing() {
        if (swipeRefreshLayout.isRefreshing()) {
            //the swipe refresh is active, stop it
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void getReferences() {
        toolbar = findViewById(R.id.student_toolbar_profile);
        //retrieve an instance of the HTTP compiled user service using retrofit
        theUserService = APIConfigurer.getApiConfigurer().getUserService();
        loadingBar = findViewById(R.id.progress_bar);
        swipeRefreshLayout = findViewById(R.id.swiper);
    }
}