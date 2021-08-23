package com.cb007787.timetabler.view.student;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
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

    private long selectedStartDate;
    private long selectedEndDate;
    private SimpleDateFormat simpleDateFormat;

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
        if (isUpdate) {
            //update task
        } else {
            //create new task.
        }
    }
}