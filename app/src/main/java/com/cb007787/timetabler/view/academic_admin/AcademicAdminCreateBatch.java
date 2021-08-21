package com.cb007787.timetabler.view.academic_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.BatchCreate;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.BatchService;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademicAdminCreateBatch extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout batchCodeLayout;
    private TextInputEditText batchCode;
    private TextInputLayout batchNameLayout;
    private TextInputEditText batchName;
    private LinearLayout modulesToBeAssigned;
    private LinearProgressIndicator progressIndicator;
    private MaterialTextView moduleLabel;
    private Button createButton;

    private ModuleService moduleService;
    private BatchService batchService;
    private String token;
    private List<String> selectedModulesForBatch = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_admin_create_batch);

        getReferences();

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //add a back button
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        createButton.setOnClickListener(v -> {
            //create a batch in DB
            createBatchDB();
        });
    }

    private void getReferences() {
        toolbar = findViewById(R.id.toolbar);
        moduleService = APIConfigurer.getApiConfigurer().getModuleService();
        batchService = APIConfigurer.getApiConfigurer().getBatchService();
        batchCodeLayout = findViewById(R.id.batch_code_layout);
        batchCode = findViewById(R.id.batch_code);
        batchNameLayout = findViewById(R.id.batch_name_layout);
        batchName = findViewById(R.id.batch_name);
        modulesToBeAssigned = findViewById(R.id.modules_to_be_assigned_layout);
        progressIndicator = findViewById(R.id.progress_bar);
        moduleLabel = findViewById(R.id.modules_to_be_assigned_label);
        createButton = findViewById(R.id.create_batch_btn);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getModulesThatCanBeAssignedToBatch();
    }

    private void getModulesThatCanBeAssignedToBatch() {
        progressIndicator.setVisibility(View.VISIBLE);
        Call<List<Module>> call = moduleService.getModulesThatCanBeAssignedToBatch(token);
        call.enqueue(new Callback<List<Module>>() {
            @Override
            public void onResponse(@NonNull Call<List<Module>> call, @NonNull Response<List<Module>> response) {
                progressIndicator.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    //create check boxes and bind to linear layout
                    bindModulesToView(response.body());
                } else {
                    //hide the module section
                    modulesToBeAssigned.setVisibility(View.GONE);
                    moduleLabel.setVisibility(View.GONE);
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while fetching modules that can be assigned to batch", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Module>> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                constructError("We ran into an error while fetching modules that can be assigned to batch", false);
                modulesToBeAssigned.setVisibility(View.GONE);
                moduleLabel.setVisibility(View.GONE);
            }
        });
    }

    private void bindModulesToView(List<Module> moduleList) {
        for (Module eachModule : moduleList) {
            MaterialCheckBox eachBox = new MaterialCheckBox(this);
            eachBox.setText(eachModule.getModuleName());
            eachBox.setChecked(false);

            modulesToBeAssigned.addView(eachBox);

            eachBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    //checked
                    this.selectedModulesForBatch.add(String.valueOf(eachModule.getModuleId()));
                } else {
                    //unchecked
                    this.selectedModulesForBatch.remove(String.valueOf(eachModule.getModuleId()));
                }
            });
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


    private void createBatchDB() {
        progressIndicator.setVisibility(View.VISIBLE);

        String enteredBatchName = batchName.getText().toString().trim();
        String enteredBatchCode = batchCode.getText().toString().trim();
        String[] selectedModules = selectedModulesForBatch.toArray(new String[selectedModulesForBatch.size()]);

        BatchCreate theCreateObj = new BatchCreate();
        theCreateObj.setBatchName(enteredBatchName);
        theCreateObj.setBatchCode(enteredBatchCode);
        theCreateObj.setModuleId(selectedModules);

        if (isValid(theCreateObj)) {
            Call<SuccessResponseAPI> createCall = batchService.createBatch(theCreateObj, token);
            createCall.enqueue(new Callback<SuccessResponseAPI>() {
                @Override
                public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                    progressIndicator.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        startActivity(new Intent(getApplicationContext(), AcademicAdministratorBatchManagement.class));
                        finish(); //do not load this activity on back.
                        Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                            constructError(theErrorReturned.getErrorMessage(), false);
                        } catch (IOException e) {
                            constructError("We ran into an error while creating the batch", false);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                    //creation failed
                    progressIndicator.setVisibility(View.GONE);
                    constructError("We ran into an error while creating the batch", false);
                }
            });
        } else {
            constructError("Please provide valid inputs before proceeding", false);
            progressIndicator.setVisibility(View.GONE);
        }

    }

    private boolean isValid(BatchCreate theCreateObj) {
        //optional - module selection
        //input length max defined by input box already
        //only check empty.

        boolean isBatchCodeValid = true;
        boolean isBatchNameValid = true;

        if (theCreateObj.getBatchCode().length() == 0) {
            batchCodeLayout.setError("Please provide a batch code");
            isBatchCodeValid = false;
        } else {
            batchCodeLayout.setError(null);
        }

        if (theCreateObj.getBatchName().length() == 0) {
            batchNameLayout.setError("Please provide a batch name");
            isBatchNameValid = false;
        } else {
            batchNameLayout.setError(null);
        }


        return isBatchCodeValid && isBatchNameValid;
    }
}