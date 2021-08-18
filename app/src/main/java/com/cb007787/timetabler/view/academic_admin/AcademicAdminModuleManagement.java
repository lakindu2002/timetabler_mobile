package com.cb007787.timetabler.view.academic_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.interfaces.UpdateCallbacks;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.recyclers.BatchRecycler;
import com.cb007787.timetabler.recyclers.ModuleRecyclerAcademicAdmin;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademicAdminModuleManagement extends AppCompatActivity {

    private Toolbar theToolbar;
    private LinearProgressIndicator linearProgressIndicator;
    private RecyclerView recyclerView;
    private ModuleRecyclerAcademicAdmin adapter; //adapter for modules for academic admin
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton floatingActionButton;

    private String token;
    private ModuleService moduleService;
    private List<Module> loadedModules = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_admin_module_management);
        getReferences();
        //validate jwt validity
        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);

        setSupportActionBar(theToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //add navigate to home
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        //assign a layout manager and the initial adapter state for the recycler
        adapter = new ModuleRecyclerAcademicAdmin(this);
        adapter.setModulesList(loadedModules);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //add refresh listener to swipe refresh layout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getAllModules();
        });

        floatingActionButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AcademicAdminCreateModule.class));
        });

        adapter.setCallbacks(new DeleteCallbacks() {
            @Override
            public void onDeleteSuccessResponse(SuccessResponseAPI theSuccessObject) {
                linearProgressIndicator.setVisibility(View.GONE);
                Snackbar theSnackBar = Snackbar.make(theToolbar, theSuccessObject.getMessage(), Snackbar.LENGTH_LONG);
                theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_success, null));
                View view = theSnackBar.getView();
                //retrieve the underling text view on the snack bar and increase the lines on it to display full message
                TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
                snackBarText.setMaxLines(5);
                theSnackBar.show();

                getAllModules(); //refresh after successful deleting.
            }

            @Override
            public void onDeleteFailure(String message) {
                linearProgressIndicator.setVisibility(View.GONE);
                constructError(message, false);
            }

            @Override
            public void onDeleteCalled() {
                linearProgressIndicator.setVisibility(View.VISIBLE);
            }
        });

        adapter.setUpdateCallbacks(new UpdateCallbacks() {
            @Override
            public void onUpdate() {
                //update triggered
                linearProgressIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onUpdateCompleted(SuccessResponseAPI theResponse) {
                linearProgressIndicator.setVisibility(View.GONE);
                Snackbar theSnackBar = Snackbar.make(theToolbar, theResponse.getMessage(), Snackbar.LENGTH_LONG);
                theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_success, null));
                View view = theSnackBar.getView();
                //retrieve the underling text view on the snack bar and increase the lines on it to display full message
                TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
                snackBarText.setMaxLines(5);
                theSnackBar.show();

                getAllModules(); //refresh after successful update.
            }

            @Override
            public void onUpdateFailed(String errorMessage) {
                linearProgressIndicator.setVisibility(View.GONE);
                constructError(errorMessage, false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate a search menu on the toolbar.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu); //inflate a search menu to the options menu

        MenuItem theSearchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) theSearchMenuItem.getActionView();//retrieve the underlying created search view for this ID (located in search.xml in menu)
        searchView.setBackgroundColor(getResources().getColor(R.color.white, null)); //set white background for search
        searchView.setQueryHint("Provide Module Name");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //user input new text
                doSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //user submit search query
                doSearch(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void getReferences() {
        theToolbar = findViewById(R.id.tool_bar);
        moduleService = APIConfigurer.getApiConfigurer().getModuleService();
        linearProgressIndicator = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler);
        swipeRefreshLayout = findViewById(R.id.swiper);
        floatingActionButton = findViewById(R.id.floating_action_button);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getAllModules();
    }

    private void getAllModules() {
        //fetch all modules of timetabler.
        linearProgressIndicator.setVisibility(View.VISIBLE);
        Call<List<Module>> apiCall = moduleService.getAllModulesAtTimetabler(token);

        apiCall.enqueue(new Callback<List<Module>>() {
            @Override
            public void onResponse(@NonNull Call<List<Module>> call, @NonNull Response<List<Module>> response) {
                linearProgressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (response.isSuccessful()) {
                    loadedModules = response.body();
                    adapter.setModulesList(loadedModules); ///will trigger update on the dataset of the recycler
                    //due to notify data set change
                    if (response.body().size() == 0) {
                        constructError("There are no modules available at TimeTabler", true);
                    }
                } else {
                    //failed to get all modules
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while fetching all modules at TimeTabler", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Module>> call, @NonNull Throwable t) {
                //call did not go or failed to parse response
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                linearProgressIndicator.setVisibility(View.GONE);
                constructError("We ran into an error while fetching all modules at TimeTabler", false);
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

    private void doSearch(String key) {
        key = key.trim().toLowerCase();
        if (key.length() == 0) {
            //user deleted text
            adapter.setModulesList(loadedModules); //load all modules
        } else {
            //user has text, filter it
            String finalKey = key;
            List<Module> filteredList = loadedModules
                    .stream()
                    .filter((eachModule) -> eachModule.getModuleName().trim().toLowerCase().contains(finalKey))
                    .collect(Collectors.toList());

            adapter.setModulesList(filteredList); //refresh adapter after search.

            if (filteredList.size() == 0) {
                constructError("There are no modules available for your search with that name", true);
            }
        }
    }
}