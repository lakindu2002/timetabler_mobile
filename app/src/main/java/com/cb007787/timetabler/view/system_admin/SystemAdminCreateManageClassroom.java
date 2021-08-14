package com.cb007787.timetabler.view.system_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.Classroom;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.ClassroomService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SystemAdminCreateManageClassroom extends AppCompatActivity {

    private TextInputLayout classroomNameLayout;
    private TextInputLayout capacityLayout;
    private TextInputEditText classroomName;
    private TextInputEditText capacity;
    private RadioGroup acRadioGroup;
    private RadioGroup smartBoardRadioGroup;
    private Button manageClassroomButton;
    private Toolbar theToolbar;
    private LinearProgressIndicator progressIndicator;

    private boolean isEdit = false;
    private ClassroomService classroomService;
    private int classroomIdToEdit;
    private Classroom editingClassroomFromServer; //object returned from server when editing.
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_admin_create_manage_classroom);
        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);

        getReferences();

        Intent callingActivity = getIntent();
        //check if create classroom or edit classroom mode
        if (callingActivity != null) {
            //default to create.
            isEdit = callingActivity.getBooleanExtra("editMode", false);
            if (isEdit) {
                //edit the classroom
                //retrieve editing classroom
                classroomIdToEdit = callingActivity.getIntExtra("classroomId", 0);

                theToolbar.setTitle("Edit Classroom Information");
                manageClassroomButton.setText("Update Classroom");
                //do not allow editing classroom name and capacity.
                classroomName.setEnabled(false);
                capacity.setEnabled(false);

                classroomName.setTextColor(getResources().getColor(R.color.black, null));
                capacity.setTextColor(getResources().getColor(R.color.black, null));
                classroomNameLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                capacityLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
            } else {
                //create new classroom
                theToolbar.setTitle("Create New Classroom");
                manageClassroomButton.setText("Create Classroom");
            }
        }

        setSupportActionBar(theToolbar);
        ActionBar theActionBar = getSupportActionBar();

        //create a back button
        if (theActionBar != null) {
            theActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
            theActionBar.setDisplayHomeAsUpEnabled(true); //enable navigate back to parent activity.
        }

        manageClassroomButton.setOnClickListener(v -> {
            //user click manage button
            if (isEdit) {
                //update classroom information
                handleUpdateClassroomButtonClick();
            } else {
                //create new classroom
                handleCreateClassroomClick();
            }
        });
    }

    private void getReferences() {
        classroomName = findViewById(R.id.classroom_name);
        capacity = findViewById(R.id.max_capacity);
        acRadioGroup = findViewById(R.id.air_conditioner_radio_group);
        smartBoardRadioGroup = findViewById(R.id.smart_board_radio_group);
        manageClassroomButton = findViewById(R.id.manage_classroom_button);
        theToolbar = findViewById(R.id.toolbar);
        classroomService = APIConfigurer.getApiConfigurer().getTheClassroomService();
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);
        progressIndicator = findViewById(R.id.progress_bar);
        classroomNameLayout = findViewById(R.id.classroom_name_layout);
        capacityLayout = findViewById(R.id.max_capacity_layout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isEdit) {
            //load classroom information for edit
            loadClassroomInformationFromDB();
        }
    }


    private void handleUpdateClassroomButtonClick() {
        progressIndicator.setVisibility(View.VISIBLE);
        //update the classroom information
        //only ac and smart board availability can be updated.

        int checkedSmartBoardId = smartBoardRadioGroup.getCheckedRadioButtonId();
        if (R.id.smart_board_yes == checkedSmartBoardId) {
            //smart board available
            editingClassroomFromServer.setSmartBoardPresent(true);
        } else if (R.id.smart_board_no == checkedSmartBoardId) {
            //no smart board available
            editingClassroomFromServer.setSmartBoardPresent(false);
        }

        int checkedAcId = acRadioGroup.getCheckedRadioButtonId();
        if (R.id.ac_yes == checkedAcId) {
            //ac available
            editingClassroomFromServer.setAcPresent(true);
        } else if (R.id.ac_no == checkedAcId) {
            //no ac available
            editingClassroomFromServer.setAcPresent(false);
        }

        Call<SuccessResponseAPI> updateCall = classroomService.updateClassroom(editingClassroomFromServer, token);
        updateCall.enqueue(new Callback<SuccessResponseAPI>() {
            @Override
            public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                progressIndicator.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    //update successfully
                    Intent goToClassroomIntent = new Intent(getApplicationContext(), SystemAdminClassroomManagement.class);
                    Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    startActivity(goToClassroomIntent);
                } else {
                    //classroom information failed to update.
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), true);
                    } catch (IOException e) {
                        constructError("We ran into an error while updating the classroom information", true);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                constructError("We ran into an error while updating the classroom information", true);
            }
        });
    }

    private void handleCreateClassroomClick() {
        //validate only classroom name and max capacity as radio boxes have defaults assigned.
        progressIndicator.setVisibility(View.VISIBLE);
        int checkedSmartBoardRadioButton = smartBoardRadioGroup.getCheckedRadioButtonId();
        int checkedAcRadioButton = acRadioGroup.getCheckedRadioButtonId();
        String enteredClassroomName = classroomName.getText().toString().trim();
        String enteredCapacity = capacity.getText().toString().trim();

        if (!enteredClassroomName.matches("^([A-Za-z][1-9]-[A-Za-z]{2}[1-9]{1,2})|([A-Za-z]{1,50})$")) {
            classroomNameLayout.setError("Please Provide a Valid Classroom Name (eg: L4-CR5/L4-CR99/Auditorium)");
        } else {
            classroomNameLayout.setError(null);
        }

        if (enteredCapacity.length() == 0) {
            capacityLayout.setError("Please provide at least a three digit number for the capacity");
        } else {
            capacityLayout.setError(null);
        }

        if (capacityLayout.getError() == null && classroomNameLayout.getError() == null) {
            //inputs valid
            Classroom createClassroom = new Classroom();

            if (checkedAcRadioButton == R.id.ac_no) {
                //selected no ac
                createClassroom.setAcPresent(false);
            } else if (checkedAcRadioButton == R.id.ac_yes) {
                createClassroom.setAcPresent(true);
            }

            if (checkedSmartBoardRadioButton == R.id.smart_board_yes) {
                //selected smart board available
                createClassroom.setSmartBoardPresent(true);
            } else if (checkedSmartBoardRadioButton == R.id.smart_board_no) {
                //no smart board
                createClassroom.setSmartBoardPresent(false);
            }

            createClassroom.setClassroomName(classroomName.getText().toString());
            createClassroom.setMaxCapacity(Integer.parseInt(capacity.getText().toString()));

            Call<SuccessResponseAPI> createCall = classroomService.createClassroom(createClassroom, token);
            createCall.enqueue(new Callback<SuccessResponseAPI>() {
                @Override
                public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                    progressIndicator.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        //classroom created successfully
                        Intent navigateBackToAllClassrooms = new Intent(getApplicationContext(), SystemAdminClassroomManagement.class);
                        startActivity(navigateBackToAllClassrooms);
                        Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        //ran into an error
                        try {
                            ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                            constructError(theErrorReturned.getErrorMessage(), false);
                        } catch (IOException e) {
                            constructError("We ran into an unexpected error while creating the classroom", false);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                    progressIndicator.setVisibility(View.GONE);
                    constructError("We ran into an unexpected error while creating the classroom", false);
                }
            });
        } else {
            progressIndicator.setVisibility(View.GONE);
        }
    }

    private void loadClassroomInformationFromDB() {
        //load for edit.
        progressIndicator.setVisibility(View.VISIBLE);
        Call<Classroom> theClassroom = classroomService.getClassroomInformation(classroomIdToEdit, token);
        theClassroom.enqueue(new Callback<Classroom>() {
            @Override
            public void onResponse(@NonNull Call<Classroom> call, @NonNull Response<Classroom> response) {
                progressIndicator.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    //classroom information loaded from server successfully
                    editingClassroomFromServer = response.body();
                    classroomName.setText(editingClassroomFromServer.getClassroomName());
                    capacity.setText(String.valueOf(editingClassroomFromServer.getMaxCapacity()));

                    if (editingClassroomFromServer.isSmartBoardPresent()) {
                        //check the yes on smart board radio group
                        smartBoardRadioGroup.check(R.id.smart_board_yes);
                    } else {
                        //no smart board
                        smartBoardRadioGroup.check(R.id.smart_board_no);
                    }

                    if (editingClassroomFromServer.isAcPresent()) {
                        //check the yes on ac yes
                        acRadioGroup.check(R.id.ac_yes);
                    } else {
                        //no ac
                        acRadioGroup.check(R.id.ac_no);
                    }

                } else {
                    //error fetching response
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while loading the classroom information", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Classroom> call, @NonNull Throwable t) {
                //error parsing response or sending request
                progressIndicator.setVisibility(View.GONE);
                constructError("We ran into an error while loading the classroom information", false);
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