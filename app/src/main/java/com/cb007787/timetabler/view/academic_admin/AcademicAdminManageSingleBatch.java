package com.cb007787.timetabler.view.academic_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.BatchShow;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.recyclers.ModuleRecyclerBatchOperation;
import com.cb007787.timetabler.recyclers.UserRecycler;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.BatchService;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademicAdminManageSingleBatch extends AppCompatActivity {

    private BatchService batchService;
    private String token;
    private String batchCodeToLoad = "";
    private BatchShow loadedBatch;
    private AuthReturn loggedInUser;

    private LinearProgressIndicator linearProgressIndicator;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialTextView batchCode;
    private MaterialTextView batchName;
    private CardView moduleCard;
    private CardView studentCard;
    private MaterialTextView moduleCount;
    private MaterialTextView studentCount;

    private RecyclerView studentList;
    private UserRecycler studentAdapter;
    private RecyclerView moduleList;
    private ModuleRecyclerBatchOperation moduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_admin_manage_single_batch);

        getReferences();
        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        try {
            loggedInUser = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
        } catch (JsonProcessingException e) {
            Log.e("JsonProcessingError", e.getLocalizedMessage());
        }
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);

        Intent intent = getIntent();
        if (intent != null) {
            batchCodeToLoad = intent.getStringExtra("batchCode");
        }

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //create the back button to navigate to home.
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        swipeRefreshLayout.setOnRefreshListener(this::getBatchInformation);

        studentAdapter = new UserRecycler(this, loggedInUser.getRole());
        studentAdapter.setBatchViewMode(true); //hide certain elements of the rendered card for recycler.
        studentAdapter.setUserList(new ArrayList<>());
        studentList.setLayoutManager(new LinearLayoutManager(this));
        studentList.setAdapter(studentAdapter);

        studentAdapter.setOnDeleteCallbacks(new DeleteCallbacks() {
            @Override
            public void onDeleteSuccessResponse(SuccessResponseAPI theSuccessObject) {
                linearProgressIndicator.setVisibility(View.GONE);
                Snackbar theSnackBar = Snackbar.make(toolbar, theSuccessObject.getMessage(), Snackbar.LENGTH_LONG);
                theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_success, null));
                View view = theSnackBar.getView();
                //retrieve the underling text view on the snack bar and increase the lines on it to display full message
                TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
                snackBarText.setMaxLines(5);
                theSnackBar.show();

                getBatchInformation(); //refresh batch after de-assigning student.
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


        moduleAdapter = new ModuleRecyclerBatchOperation(this, batchCodeToLoad);
        moduleAdapter.setModuleList(new ArrayList<>());
        moduleList.setAdapter(moduleAdapter);
        moduleList.setLayoutManager(new LinearLayoutManager(this));

        moduleAdapter.setDeleteCallbacks(new DeleteCallbacks() {
            @Override
            public void onDeleteSuccessResponse(SuccessResponseAPI theSuccessObject) {
                linearProgressIndicator.setVisibility(View.GONE);
                Snackbar theSnackBar = Snackbar.make(toolbar, theSuccessObject.getMessage(), Snackbar.LENGTH_LONG);
                theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_success, null));
                View view = theSnackBar.getView();
                //retrieve the underling text view on the snack bar and increase the lines on it to display full message
                TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
                snackBarText.setMaxLines(5);
                theSnackBar.show();

                getBatchInformation(); //refresh batch after de-assigning.
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
    }

    private void getReferences() {
        batchService = APIConfigurer.getApiConfigurer().getBatchService();
        linearProgressIndicator = findViewById(R.id.progress_bar);
        toolbar = findViewById(R.id.single_batch_toolbar);
        swipeRefreshLayout = findViewById(R.id.swiper);
        batchCode = findViewById(R.id.batch_code);
        batchName = findViewById(R.id.batch_name);
        moduleCard = findViewById(R.id.modules_enrolled_card);
        studentCard = findViewById(R.id.students_enrolled_card);
        moduleCount = findViewById(R.id.modules_enrolled_count);
        studentCount = findViewById(R.id.students_enrolled_count);
        studentList = findViewById(R.id.students_enrolled_recycler);
        moduleList = findViewById(R.id.modules_enrolled_recycler);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getBatchInformation();
    }

    private void getBatchInformation() {
        moduleCard.setVisibility(View.VISIBLE);
        studentCard.setVisibility(View.VISIBLE);

        linearProgressIndicator.setVisibility(View.VISIBLE);
        Call<BatchShow> getBatchCall = batchService.getBatchInformation(token, batchCodeToLoad);
        getBatchCall.enqueue(new Callback<BatchShow>() {
            @Override
            public void onResponse(@NonNull Call<BatchShow> call, @NonNull Response<BatchShow> response) {
                linearProgressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (response.isSuccessful()) {
                    //response successful, update view
                    loadedBatch = response.body();
                    showBatchInformationInView();
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while loading the batch information", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BatchShow> call, @NonNull Throwable t) {
                linearProgressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                constructError("We ran into an error while loading the batch information", false);
            }
        });
    }

    /**
     * Executed as callback on OnResponse to update UI
     */
    private void showBatchInformationInView() {
        batchCode.setText(String.format("Batch Code: %s", loadedBatch.getBatchCode()));
        batchName.setText(String.format("Batch Name: %s", loadedBatch.getBatchName()));

        if (loadedBatch.getStudentList().size() == 0) {
            studentCard.setVisibility(View.GONE);
            constructError("There are no students enrolled to the batch", true);
        } else {
            studentCount.setText(String.format(Locale.ENGLISH, "Students Enrolled To Batch - %d", loadedBatch.getStudentList().size()));
            studentAdapter.setUserList(loadedBatch.getStudentList());
        }

        if (loadedBatch.getModuleList().size() == 0) {
            moduleCard.setVisibility(View.GONE);
            constructError("There are no modules enrolled to batch", true);
        } else {
            moduleCount.setText(String.format(Locale.ENGLISH, "Modules Enrolled To Batch - %d", loadedBatch.getModuleList().size()));
            moduleAdapter.setModuleList(loadedBatch.getModuleList());
        }
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