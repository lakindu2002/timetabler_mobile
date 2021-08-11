package com.cb007787.timetabler.view.lecturer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cb007787.timetabler.R;
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

public class LecturerModules extends AppCompatActivity {

    private Toolbar theToolbar;
    private LinearProgressIndicator progressIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ModuleRecycler moduleAdapter;

    private ModuleService moduleService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_modules);
        getReferences();

        setSupportActionBar(theToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        swipeRefreshLayout.setOnRefreshListener(() -> getAllModulesForLecturer());
    }

    @Override
    protected void onStart() {
        super.onStart();
        getAllModulesForLecturer();
    }

    private void getAllModulesForLecturer() {
        progressIndicator.setVisibility(View.VISIBLE);
        Call<List<Module>> moduleCall = moduleService.getAllModulesForUser(token);

        moduleCall.enqueue(new Callback<List<Module>>() {
            @Override
            public void onResponse(Call<List<Module>> call, Response<List<Module>> response) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                progressIndicator.setVisibility(View.GONE);
                handleOnResponse(response);
            }

            @Override
            public void onFailure(Call<List<Module>> call, Throwable t) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                progressIndicator.setVisibility(View.GONE);
                handleOnFailure(t);
            }
        });
    }

    private void handleOnFailure(Throwable t) {
        constructError("An unexpected error occurred while processing your request.", false);
    }

    private void constructError(String errorMessage, boolean isInfo) {
        Snackbar theSnackBar = Snackbar.make(theToolbar, errorMessage, Snackbar.LENGTH_LONG);
        if (isInfo) {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        } else {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        }
        theSnackBar.show();
    }

    private void handleOnResponse(Response<List<Module>> response) {
        if (response.isSuccessful()) {
            moduleAdapter = new ModuleRecycler(response.body(), this);
            recyclerView.setAdapter(moduleAdapter);
            if (response.body().size() == 0) {
                constructError(
                        "You have not yet been assigned any modules", false
                );
            }
        } else {
            try {
                constructError(
                        APIConfigurer.getTheErrorReturned(response.errorBody()).getErrorMessage(), false
                );
            } catch (IOException e) {
                Log.e("handleOnResponse", "ERROR PARSING JSON");
            }
        }

    }

    private void getReferences() {
        this.theToolbar = findViewById(R.id.lecturer_module_toolbar);
        this.progressIndicator = findViewById(R.id.progress_bar);
        this.moduleService = APIConfigurer.getApiConfigurer().getModuleService();
        this.swipeRefreshLayout = findViewById(R.id.lecturer_module_swipe);
        this.token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);
        this.recyclerView = findViewById(R.id.lecturer_module_recycler);
    }
}