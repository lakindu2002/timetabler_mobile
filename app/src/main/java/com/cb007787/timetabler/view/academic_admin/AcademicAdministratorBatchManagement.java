package com.cb007787.timetabler.view.academic_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.model.BatchShow;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.recyclers.BatchRecycler;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.BatchService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademicAdministratorBatchManagement extends AppCompatActivity {

    private RecyclerView recycler;
    private SwipeRefreshLayout swiper;
    private Toolbar toolbar;
    private LinearProgressIndicator progressIndicator;

    private BatchRecycler adapter;
    private String token;
    private BatchService batchService;
    private List<BatchShow> batchList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_administrator_batch_management);

        getReferences();

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //set white color back button
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);

        adapter = new BatchRecycler(this);
        adapter.setBatchList(batchList);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        swiper.setOnRefreshListener(() -> {
            getAllBatches();
        });

        //assign delete callbacks for batch recycler
        adapter.setDeleteCallbacks(new DeleteCallbacks() {
            @Override
            public void onDeleteSuccessResponse(SuccessResponseAPI theSuccessObject) {
                progressIndicator.setVisibility(View.GONE);
                Snackbar theSnackBar = Snackbar.make(toolbar, theSuccessObject.getMessage(), Snackbar.LENGTH_LONG);
                theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_success, null));
                View view = theSnackBar.getView();
                //retrieve the underling text view on the snack bar and increase the lines on it to display full message
                TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
                snackBarText.setMaxLines(5);
                theSnackBar.show();

                getAllBatches();
            }

            @Override
            public void onDeleteFailure(String message) {
                constructError(message, false);
                progressIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onDeleteCalled() {
                progressIndicator.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getReferences() {
        recycler = findViewById(R.id.recycler);
        swiper = findViewById(R.id.swiper);
        toolbar = findViewById(R.id.tool_bar);
        batchService = APIConfigurer.getApiConfigurer().getBatchService();
        progressIndicator = findViewById(R.id.progress_bar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate a search view.
        getMenuInflater().inflate(R.menu.search, menu);
        SearchView theSearchBar = (SearchView) menu.findItem(R.id.search).getActionView();
        theSearchBar.setBackgroundColor(getResources().getColor(R.color.white, null));
        theSearchBar.setQueryHint("Provide Batch Code");

        theSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                doSearch(newText);
                return true;
            }
        });
        return true;
    }

    private void doSearch(String query) {
        query = query.toLowerCase().trim();
        if (query.length() == 0) {
            adapter.setBatchList(batchList);
        } else {
            //filter for the input text against the batch code.
            String finalQuery = query;
            List<BatchShow> filteredBatches =
                    batchList
                            .stream()
                            .filter((eachBatch) -> eachBatch.getBatchCode().trim().toLowerCase().contains(finalQuery))
                            .collect(Collectors.toList());

            adapter.setBatchList(filteredBatches);
            if (filteredBatches.size() == 0) {
                //did not match criteria
                constructError("There are no batches for the entered batch code", true);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getAllBatches();
    }

    private void getAllBatches() {
        progressIndicator.setVisibility(View.VISIBLE);
        Call<List<BatchShow>> batchListCall = batchService.getAllBatches(token);
        batchListCall.enqueue(new Callback<List<BatchShow>>() {
            @Override
            public void onResponse(@NonNull Call<List<BatchShow>> call, @NonNull Response<List<BatchShow>> response) {
                progressIndicator.setVisibility(View.GONE);
                if (swiper.isRefreshing()) {
                    swiper.setRefreshing(false);
                }
                if (response.isSuccessful()) {
                    batchList = response.body();
                    adapter.setBatchList(batchList);
                    if (batchList.size() == 0) {
                        constructError("There are no batches available at TimeTabler", false);
                    }
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while fetching all batches", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BatchShow>> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                if (swiper.isRefreshing()) {
                    swiper.setRefreshing(false);
                }
                constructError("We ran into an error while fetching all batches", false);
            }
        });
    }

    private void constructError(String errorMessage, boolean isInfo) {
        Snackbar theSnackBar = Snackbar.make(toolbar, errorMessage, Snackbar.LENGTH_LONG);
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