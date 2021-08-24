package com.cb007787.timetabler.view.student;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.provider.TaskContentProvider;
import com.cb007787.timetabler.provider.TaskDbHelper;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentTaskCreateUpdate extends AppCompatActivity {

    private Toolbar toolbar;
    private boolean isUpdate = false;
    private TextInputLayout taskNameLayout;
    private TextInputEditText taskName;
    private TextInputLayout taskDescriptionLayout;
    private TextInputEditText taskDescription;
    private TextInputLayout startDateLayout;
    private TextInputEditText startDate;
    private TextInputLayout endDateLayout;
    private TextInputEditText endDate;
    private Button manageButton;
    private ImageButton endDateBtn;
    private ImageButton startDateBtn;
    private LinearProgressIndicator linearProgressIndicator;

    private long selectedStartDate;
    private long selectedEndDate;
    private SimpleDateFormat simpleDateFormat;
    private AuthReturn loggedInUser;
    private ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_task_create_update);

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);

        getReferences();
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null) {
            isUpdate = intent.getBooleanExtra("isUpdate", false);
        }

        startDate.setEnabled(false);
        startDate.setTextColor(ActivityCompat.getColor(this, R.color.black));
        endDate.setEnabled(false);
        endDate.setTextColor(ActivityCompat.getColor(this, R.color.black));

        startDateBtn.setOnClickListener(v -> {
            //show start date picker
            showStartDatePicker();
        });

        endDateBtn.setOnClickListener(v -> {
            //show end date picker
            showEndDatePicker();
        });

        manageButton.setOnClickListener(v -> {
            handleTask();
        });
    }


    private void getReferences() {
        toolbar = findViewById(R.id.toolbar);
        taskName = findViewById(R.id.task_name_field);
        taskNameLayout = findViewById(R.id.task_name_layout);
        taskDescription = findViewById(R.id.task_description_field);
        taskDescriptionLayout = findViewById(R.id.task_description_layout);
        startDate = findViewById(R.id.start_date_field);
        startDateLayout = findViewById(R.id.start_date_layout);
        startDateBtn = findViewById(R.id.start_date_btn);
        endDate = findViewById(R.id.end_date_field);
        endDateLayout = findViewById(R.id.end_date_layout);
        endDateBtn = findViewById(R.id.end_date_btn);
        manageButton = findViewById(R.id.task_manage_btn);
        contentResolver = getContentResolver();
        linearProgressIndicator = findViewById(R.id.progress_bar);
        try {
            loggedInUser = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isUpdate) {
            toolbar.setTitle("Update Task");
            manageButton.setText("Update Task");
        } else {
            toolbar.setTitle("Create Task");
            manageButton.setText("Create Task");
        }
    }

    private void showStartDatePicker() {
        MaterialDatePicker<Long> startDatePicker = MaterialDatePicker.Builder.datePicker()
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .setTitleText("Select Start Date")
                .setSelection(new Date().getTime())
                .build();

        startDatePicker.addOnPositiveButtonClickListener(selection -> {
            //no need validation as user can schedule a task for any date.
            java.sql.Date selectedDate = new java.sql.Date(selection);
            startDate.setText(simpleDateFormat.format(selectedDate));
            selectedStartDate = selection;
        });

        startDatePicker.addOnNegativeButtonClickListener(v -> {
            startDatePicker.dismiss();
        });

        startDatePicker.show(getSupportFragmentManager().beginTransaction(), "START_DATE_PICKER");
    }

    private void showEndDatePicker() {
        MaterialDatePicker<Long> endDatePicker = MaterialDatePicker.Builder.datePicker()
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .setTitleText("Select End Date")
                .setSelection(new Date().getTime())
                .build();

        endDatePicker.addOnPositiveButtonClickListener(selection -> {
            //no need validation as user can schedule a task for any date.
            java.sql.Date selectedDate = new java.sql.Date(selection);
            endDate.setText(simpleDateFormat.format(selectedDate));
            selectedEndDate = selection;
        });

        endDatePicker.addOnNegativeButtonClickListener(v -> {
            endDatePicker.dismiss();
        });

        endDatePicker.show(getSupportFragmentManager().beginTransaction(), "END_DATE_PICKER");
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

    private void handleTask() {
        linearProgressIndicator.setVisibility(View.VISIBLE);
        String enteredTaskName = taskName.getText().toString();
        String enteredTaskDescription = taskName.getText().toString();
        long enteredStart = selectedStartDate;
        long enteredEnd = selectedEndDate;
        String username = loggedInUser.getUsername();

        if (isUpdate) {
            //update task
        } else {
            //create new task.
            ContentValues contentValues = new ContentValues();
            contentValues.put(TaskDbHelper.TASK_NAME, enteredTaskName);
            contentValues.put(TaskDbHelper.TASK_DESCRIPTION, enteredTaskDescription);
            contentValues.put(TaskDbHelper.START_DATE, enteredStart);
            contentValues.put(TaskDbHelper.DUE_DATE, enteredEnd);
            contentValues.put(TaskDbHelper.USERNAME, username);

            Uri insertUri = contentResolver.insert(TaskContentProvider.PERFORM_INSERT, contentValues);
            if (insertUri.equals(TaskContentProvider.SUCCESS_URI)) {
                Toast.makeText(getApplicationContext(), "Your Task Has Been Created Successfully", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, StudentTaskManagement.class));
                finish();
                linearProgressIndicator.setVisibility(View.GONE);
            } else {
                linearProgressIndicator.setVisibility(View.GONE);
                constructError("We ran into an error while creating your task", false);
            }
        }
    }
}