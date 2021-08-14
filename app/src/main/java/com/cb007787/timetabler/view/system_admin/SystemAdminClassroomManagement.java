package com.cb007787.timetabler.view.system_admin;

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
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.model.Classroom;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.recyclers.ClassroomRecycler;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.ClassroomService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SystemAdminClassroomManagement extends AppCompatActivity {

    private Toolbar theToolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private LinearProgressIndicator progressIndicator;

    private String token;
    private ClassroomService classroomService;
    private List<Classroom> loadedClassrooms = new ArrayList<>();
    private ClassroomRecycler adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_admin_classroom_management);
        getReferences();
        //validate the token to ensure access can be given.
        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);


        setSupportActionBar(theToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        adapter = new ClassroomRecycler(this);
        layoutManager = new LinearLayoutManager(this); //create linear layout for recycler view
        //set initial empty list to adapter.
        adapter.setClassroomList(loadedClassrooms); //will trigger update in recycler view.
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter); //assign the layout and the adapter so recycler view can render data

        //attach the delete callback functions so recycler view can response to changes when deletion occurs.
        adapter.setOnDeleteCallbacks(new DeleteCallbacks() {
            @Override
            public void onDeleteSuccessResponse(SuccessResponseAPI theSuccessObject) {
                progressIndicator.setVisibility(View.GONE);
                //deleted successfully
                Snackbar theSnackBar = Snackbar.make(theToolbar, theSuccessObject.getMessage(), Snackbar.LENGTH_LONG);
                theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_success, null));
                View view = theSnackBar.getView();
                //retrieve the underling text view on the snack bar and increase the lines on it to display full message
                TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
                snackBarText.setMaxLines(5);
                theSnackBar.show();

                //update recycler
                getAllClassrooms();
            }

            @Override
            public void onDeleteFailure(String message) {
                //did not delete successfully
                progressIndicator.setVisibility(View.GONE);
                constructError(message, false);
            }

            @Override
            public void onDeleteCalled() {
                //when delete is called, make sure to display the loading indicator
                progressIndicator.setVisibility(View.VISIBLE);
            }
        });

        //when user refreshes
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getAllClassrooms();
        });
    }

    private void getReferences() {
        theToolbar = findViewById(R.id.the_toolbar);
        swipeRefreshLayout = findViewById(R.id.swiper);
        recyclerView = findViewById(R.id.classroom_recycler);
        progressIndicator = findViewById(R.id.progress_bar);
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);
        classroomService = APIConfigurer.getApiConfigurer().getTheClassroomService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getAllClassrooms();
    }

    private void getAllClassrooms() {
        //method will get all classrooms
        progressIndicator.setVisibility(View.VISIBLE);
        Call<List<Classroom>> getAllClassroomCall = classroomService.getAllClassrooms(token);

        getAllClassroomCall.enqueue(new Callback<List<Classroom>>() {
            @Override
            public void onResponse(@NonNull Call<List<Classroom>> call, @NonNull Response<List<Classroom>> response) {
                if (swipeRefreshLayout.isRefreshing()) {
                    //stop refreshing.
                    swipeRefreshLayout.setRefreshing(false);
                }
                progressIndicator.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    //data retrieved successfully
                    loadedClassrooms = response.body();
                    //set the response body to the adapter and notify the data change.
                    adapter.setClassroomList(loadedClassrooms);
                    if (loadedClassrooms.size() == 0) {
                        //no classrooms
                        constructError("There are no classrooms at TimeTabler", true);
                    }
                } else {
                    //data could not be retrieved
                    try {
                        ErrorResponseAPI apiError = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(apiError.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while fetching the classrooms", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Classroom>> call, @NonNull Throwable t) {
                //failed parsing response or could not send api call
                if (swipeRefreshLayout.isRefreshing()) {
                    //stop refreshing.
                    swipeRefreshLayout.setRefreshing(false);
                }
                progressIndicator.setVisibility(View.GONE);
                constructError("We ran into an error while fetching the classrooms", false);
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