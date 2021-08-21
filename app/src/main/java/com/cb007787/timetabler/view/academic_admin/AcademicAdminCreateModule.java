package com.cb007787.timetabler.view.academic_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.BatchService;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademicAdminCreateModule extends AppCompatActivity {

    private Toolbar toolbar;
    private LinearProgressIndicator progressIndicator;
    private TextInputLayout moduleNameLayout;
    private TextInputEditText moduleName;
    private TextInputLayout creditCountLayout;
    private MaterialAutoCompleteTextView creditCount;
    private TextInputLayout independentLearningHoursLayout;
    private TextInputEditText independentLearningHours;
    private TextInputLayout contactHoursLayout;
    private TextInputEditText contactHours;
    private Button manageBtn;


    private int editingModuleId;
    private Module moduleBeingEdited;
    private boolean editMode = false;
    private ModuleService moduleService;
    private String token;
    private List<String> allowedCreditCount = Arrays.asList("10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_admin_create_module);
        getReferences();

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //add back button
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(ActivityCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24));
        }

        //populate the credit count with the array adapter
        ArrayAdapter<String> creditCountList = new ArrayAdapter<>(this, R.layout.role_array_adapter); //re-use role adapter for the text view on autocomplete view
        for (String eachCredit : allowedCreditCount) {
            creditCountList.add(eachCredit);
        }

        //assign adapter for the input
        creditCount.setAdapter(creditCountList);

        manageBtn.setOnClickListener(v -> {
            //update/save module.
            if (editMode) {
                //update
                updateModule();
            } else {
                //create new
                saveModule();
            }
        });

        Intent intent = getIntent(); //if the action is an edit, retrieve edit object
        if (intent.getSerializableExtra("theModule") != null) {
            //editing a module.
            editMode = true;
            editingModuleId = intent.getIntExtra("theModule", 0);
            //do not allow editing module name and credit count
            moduleName.setEnabled(false);
            creditCount.setEnabled(false);
            moduleName.setTextColor(getResources().getColor(R.color.black, null));
            creditCount.setTextColor(getResources().getColor(R.color.black, null));
            manageBtn.setText("Update Module");

            fetchEditModule();
        }
    }


    private void getReferences() {
        toolbar = findViewById(R.id.toolbar);
        progressIndicator = findViewById(R.id.progress_bar);
        moduleNameLayout = findViewById(R.id.module_name_layout);
        moduleName = findViewById(R.id.module_name);
        creditCountLayout = findViewById(R.id.credit_count_layout);
        creditCount = findViewById(R.id.credit_count);
        independentLearningHoursLayout = findViewById(R.id.independent_learning_hours_layout);
        independentLearningHours = findViewById(R.id.independent_learning_hours);
        contactHours = findViewById(R.id.contact_hours);
        contactHoursLayout = findViewById(R.id.contact_hours_layout);
        moduleService = APIConfigurer.getApiConfigurer().getModuleService();
        manageBtn = findViewById(R.id.module_btn);
    }

    public void saveModule() {
        progressIndicator.setVisibility(View.VISIBLE);
        String enteredModuleName = moduleName.getText().toString();
        String selectedCreditCount = creditCount.getText().toString();
        String enteredContactHours = contactHours.getText().toString();
        String selectedIndependentHours = independentLearningHours.getText().toString();

        Module theModule = new Module();
        theModule.setModuleName(enteredModuleName);
        theModule.setCreditCount(selectedCreditCount);
        theModule.setContactHours(enteredContactHours);
        theModule.setIndependentHours(selectedIndependentHours);

        if (areInputsValid(theModule)) {
            Call<SuccessResponseAPI> createApiCall = moduleService.createModule(theModule, token);
            createApiCall.enqueue(new Callback<SuccessResponseAPI>() {
                @Override
                public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                    progressIndicator.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        //module created successfully
                        Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), AcademicAdminModuleManagement.class));
                        finish();
                    } else {
                        //module not created
                        try {
                            ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                            if (theErrorReturned.getValidationErrors().size() > 0) {
                                //have validation errors input
                                for (String eachError : theErrorReturned.getValidationErrors()) {
                                    constructError(eachError, false);
                                }
                            } else {
                                //no validation error, just errors
                                constructError(theErrorReturned.getErrorMessage(), false);
                            }
                        } catch (IOException e) {
                            constructError("We ran into an error while creating the module", false);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                    //call not sent or error parsing response.
                    progressIndicator.setVisibility(View.GONE);
                    constructError("We ran into an error while creating the module", false);
                }
            });
        } else {
            progressIndicator.setVisibility(View.GONE);
            constructError("Please provide valid inputs before proceeding", false);
        }
    }

    private boolean areInputsValid(Module theModule) {
        boolean isModuleNameValid = true;
        boolean isCreditCountValid = true;
        boolean isContactHoursValid = true;
        boolean isIndependentHoursValid = true;

        if (TextUtils.isEmpty(theModule.getModuleName())) {
            //module name not provided
            moduleNameLayout.setError("Please provide a module name");
            isModuleNameValid = false;
        } else {
            moduleNameLayout.setError(null);
        }

        if (TextUtils.isEmpty(theModule.getCreditCount())) {
            creditCountLayout.setError("Please provide a valid credit count");
            isCreditCountValid = false;
        } else {
            creditCountLayout.setError(null);
        }

        if (TextUtils.isEmpty(theModule.getContactHours())) {
            contactHoursLayout.setError("Please provide a valid contact hours");
            isContactHoursValid = false;
        } else {
            contactHoursLayout.setError(null);
        }

        if (TextUtils.isEmpty(theModule.getIndependentHours())) {
            independentLearningHoursLayout.setError("Please provide a valid independent learning hours");
            isIndependentHoursValid = false;
        } else {
            independentLearningHoursLayout.setError(null);
        }

        return isModuleNameValid && isContactHoursValid && isCreditCountValid && isIndependentHoursValid;
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

    private void fetchEditModule() {
        progressIndicator.setVisibility(View.VISIBLE);
        Call<Module> getModuleCall = moduleService.getModuleById(token, editingModuleId);
        getModuleCall.enqueue(new Callback<Module>() {
            @Override
            public void onResponse(@NonNull Call<Module> call, @NonNull Response<Module> response) {
                progressIndicator.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    //module retrieved
                    moduleBeingEdited = response.body();
                    moduleName.setText(moduleBeingEdited.getModuleName());
                    creditCount.setText(moduleBeingEdited.getCreditCount());
                    independentLearningHours.setText(moduleBeingEdited.getIndependentHours());
                    contactHours.setText(moduleBeingEdited.getContactHours());
                } else {
                    manageBtn.setEnabled(false);
                    try {
                        constructError(APIConfigurer.getTheErrorReturned(response.errorBody()).getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We rain into an error while retrieving module information for update", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Module> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                manageBtn.setEnabled(false);
                constructError("We rain into an error while retrieving module information for update", false);
            }
        });
    }

    private void updateModule() {
        progressIndicator.setVisibility(View.VISIBLE);
        String enteredIndependentHours = independentLearningHours.getText().toString();
        String enteredContactHours = contactHours.getText().toString();

        moduleBeingEdited.setIndependentHours(enteredIndependentHours);
        moduleBeingEdited.setContactHours(enteredContactHours);

        if (areInputsValid(moduleBeingEdited)) {
            Call<SuccessResponseAPI> updateCall = moduleService.updateModule(moduleBeingEdited, token, moduleBeingEdited.getModuleId());
            updateCall.enqueue(new Callback<SuccessResponseAPI>() {
                @Override
                public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                    progressIndicator.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        //updated successfully
                        Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                        startActivity(
                                new Intent(getApplicationContext(), AcademicAdminModuleManagement.class)
                        );
                        finish(); //do not allow back button to navigate back to this activity after success update
                    } else {
                        //module not created
                        try {
                            ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                            if (theErrorReturned.getValidationErrors().size() > 0) {
                                //have validation errors input
                                for (String eachError : theErrorReturned.getValidationErrors()) {
                                    constructError(eachError, false);
                                }
                            } else {
                                //no validation error, just errors
                                constructError(theErrorReturned.getErrorMessage(), false);
                            }
                        } catch (IOException e) {
                            constructError("We ran into an error while updating the module", false);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                    progressIndicator.setVisibility(View.GONE);
                    constructError("We rain into an error while updating the module information", false);
                }
            });
        } else {
            constructError("Please provide valid inputs before proceeding", false);
            progressIndicator.setVisibility(View.GONE);
        }
    }
}