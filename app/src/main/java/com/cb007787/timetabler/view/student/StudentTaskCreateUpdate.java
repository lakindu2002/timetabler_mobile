package com.cb007787.timetabler.view.student;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.Task;
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
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.Locale;

public class StudentTaskCreateUpdate extends AppCompatActivity {

    private boolean isUpdate = false;
    private int taskId = 0;

    private Toolbar toolbar;
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
    private MaterialTextView lastUpdatedAt; //used only when updating a task.
    private MaterialTextView taskStatus; // used for updating only.

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
            if (isUpdate) {
                //if task is being updated, load task being updated.
                taskId = intent.getIntExtra("taskId", 0);
            }
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
        lastUpdatedAt = findViewById(R.id.last_updated_at);
        taskStatus = findViewById(R.id.task_status);
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
            lastUpdatedAt.setVisibility(View.VISIBLE);
            taskStatus.setVisibility(View.VISIBLE);
            loadTaskInformation();
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
        String enteredTaskName = taskName.getText().toString().trim();
        String enteredTaskDescription = taskDescription.getText().toString().trim();
        long enteredStart = selectedStartDate;
        long enteredEnd = selectedEndDate;
        String username = loggedInUser.getUsername();

        boolean isValid = validateInputs(enteredTaskName, enteredTaskDescription, enteredStart, enteredEnd, username);

        if (isValid) {
            if (isUpdate) {
                //update task
                ContentValues contentValues = new ContentValues();
                contentValues.put(TaskDbHelper.TASK_NAME, enteredTaskName);
                contentValues.put(TaskDbHelper.TASK_DESCRIPTION, enteredTaskDescription);
                contentValues.put(TaskDbHelper.START_DATE, enteredStart);
                contentValues.put(TaskDbHelper.DUE_DATE, enteredEnd);

                String whereClause = TaskDbHelper.TASK_ID + "=?";
                String[] whereArgs = {String.valueOf(taskId)};

                int affectedRows = contentResolver.update(TaskContentProvider.PERFORM_UPDATE, contentValues, whereClause, whereArgs);

                if (affectedRows == 0) {
                    //no rows affected, error.
                    constructError("We ran into an error while updating the task", false);
                    linearProgressIndicator.setVisibility(View.GONE);
                } else if (affectedRows == 1) {
                    //success, only one ID so one should get updated.
                    Toast.makeText(this, "The task has been updated successfully", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, StudentTaskManagement.class));
                    finish();//do not load this activity on back
                    linearProgressIndicator.setVisibility(View.GONE);
                }

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
        } else {
            linearProgressIndicator.setVisibility(View.GONE);
            constructError("Provide Valid Inputs Before Proceeding", false);
        }
    }

    private boolean validateInputs(String enteredTaskName, String enteredTaskDescription, long enteredStart, long enteredEnd, String username) {
        boolean isTaskNameValid = true;
        boolean isTaskDescriptionValid = true;
        boolean isEnteredStartValid = true;
        boolean isEnteredEndValid = true;
        boolean isUsernameValid = true;

        if (TextUtils.isEmpty(enteredTaskName)) {
            isTaskNameValid = false;
            taskNameLayout.setError("Provide a Task Name");
        } else {
            taskNameLayout.setError(null);
        }

        if (TextUtils.isEmpty(enteredTaskDescription)) {
            isTaskDescriptionValid = false;
            taskDescriptionLayout.setError("Provide a Task Description");
        } else {
            taskDescriptionLayout.setError(null);
        }

        if (enteredStart > enteredEnd) {
            isEnteredStartValid = false;
            Toast.makeText(this, "Start Date Cannot Be After End Date", Toast.LENGTH_LONG).show();
        }

        if (enteredStart == 0) {
            isEnteredStartValid = false;
            startDateLayout.setError("Provide a Start Date");
        } else {
            startDateLayout.setError(null);
        }

        if (enteredEnd == 0) {
            isEnteredEndValid = false;
            endDateLayout.setError("Provide a Start Date");
        } else {
            endDateLayout.setError(null);
        }

        if (username == null || TextUtils.isEmpty(username)) {
            isUsernameValid = false;
            Toast.makeText(this, "Logged In User Not Present", Toast.LENGTH_LONG).show();
        }

        return isEnteredEndValid && isUsernameValid && isEnteredStartValid && isTaskDescriptionValid && isTaskNameValid;
    }

    private void loadTaskInformation() {
        SimpleDateFormat updateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.ENGLISH);
        //use the content provider to fetch from the database the task for the given user.
        linearProgressIndicator.setVisibility(View.VISIBLE);
        String[] requiredColumns = {
                TaskDbHelper.TASK_ID,
                TaskDbHelper.TASK_NAME,
                TaskDbHelper.TASK_DESCRIPTION,
                TaskDbHelper.TASK_STATUS,
                TaskDbHelper.START_DATE,
                TaskDbHelper.DUE_DATE,
                TaskDbHelper.LAST_UPDATED_AT
        }; //the columns to get from the query.

        //where username is equal to given username and task id is equal to given task it
        String whereClause = TaskDbHelper.USERNAME + "=? AND " + TaskDbHelper.TASK_ID + "=?";
        String[] whereData = {loggedInUser.getUsername(), String.valueOf(taskId)};
        Cursor executedQuery = contentResolver.query(TaskContentProvider.PERFORM_FIND_ONE_URI, requiredColumns, whereClause, whereData, null);
        //executed query will have only one.
        boolean firstExist = executedQuery.moveToFirst();//get the first row.

        //retrieve data on first row.
        if (firstExist) {
            String savedTaskName = executedQuery.getString(executedQuery.getColumnIndex(TaskDbHelper.TASK_NAME));
            int savedTaskId = executedQuery.getInt(executedQuery.getColumnIndex(TaskDbHelper.TASK_ID));
            String savedTaskDescription = executedQuery.getString(executedQuery.getColumnIndex(TaskDbHelper.TASK_DESCRIPTION));
            long savedStartDate = executedQuery.getLong(executedQuery.getColumnIndex(TaskDbHelper.START_DATE));
            long savedDueDate = executedQuery.getLong(executedQuery.getColumnIndex(TaskDbHelper.DUE_DATE));
            String savedStatus = executedQuery.getString(executedQuery.getColumnIndex(TaskDbHelper.TASK_STATUS));
            long savedLastUpdatedAt = executedQuery.getLong(executedQuery.getColumnIndex(TaskDbHelper.LAST_UPDATED_AT));


            //show in view
            taskId = savedTaskId;
            selectedStartDate = savedStartDate;
            selectedEndDate = savedDueDate;

            taskName.setText(savedTaskName);
            taskDescription.setText(savedTaskDescription);
            startDate.setText(simpleDateFormat.format(new java.sql.Date(savedStartDate)));
            endDate.setText(simpleDateFormat.format(new java.sql.Date(savedDueDate)));
            taskStatus.setText(String.format("Task Status: %s", savedStatus));
            lastUpdatedAt.setText(String.format("Last Updated: %s", updateFormat.format(new java.sql.Date(savedLastUpdatedAt))));


            if (savedStatus.equalsIgnoreCase("completed")) {
                //if task is completed, cannot update date.
                startDateBtn.setEnabled(false);
                endDateBtn.setEnabled(false);
            }

        } else {
            constructError("The Task Could Not Be Retrieved", false);
        }
        executedQuery.close();
        linearProgressIndicator.setVisibility(View.GONE);
    }
}