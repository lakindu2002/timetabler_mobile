package com.cb007787.timetabler.view.lecturer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.BatchShow;
import com.cb007787.timetabler.model.Classroom;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.LectureCreate;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.LectureService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.lecturer.LecturerModules;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleLecture extends AppCompatActivity {

    private int moduleIdPassedFromIntent;
    private String token;
    private List<Classroom> loadedClassrooms;
    private Module loadedModule;
    private String selectedDateInUi;//to be formatted in "yyyy-mm-dd"
    private final ArrayList<String> selectedBatches = new ArrayList<>(); //used to hold selected batches in checkbox.

    private LectureService lectureService;
    private Toolbar theToolbar;
    private LinearProgressIndicator loadingBar;

    //schedule inputs
    private MaterialTextView moduleName;
    private MaterialTextView moduleTaughtBy;
    private AutoCompleteTextView classroomList;
    private TextInputEditText lectureDate;
    private TextInputEditText lectureCommencingTime;
    private TextInputEditText lectureFinishingTime;
    private ImageButton lectureDateSelector;
    private ImageButton commencingTimeSelector;
    private ImageButton finishingTimeSelector;
    private MaterialCheckBox eachBatchCheckbox;
    private LinearLayout checkboxLayout; //used to hold list of checkboxes.
    private Button confirmLectureButton;

    //layouts to handle errors
    private TextInputLayout endTimeLayout;
    private TextInputLayout startTimeLayout;
    private TextInputLayout lectureDateLayout;
    private TextInputLayout classroomLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_lecture);
        getReferences();

        lectureDate.setEnabled(false);
        lectureCommencingTime.setEnabled(false);
        lectureFinishingTime.setEnabled(false);


        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);

        Intent intent = getIntent();
        if (intent != null) {
            this.moduleIdPassedFromIntent = intent.getIntExtra("theModuleId", 0);
        } else {
            this.moduleIdPassedFromIntent = 0;
        }

        setSupportActionBar(theToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24, null));
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        lectureDateSelector.setOnClickListener(v -> constructDatePickerForLectureDateSelection());

        commencingTimeSelector.setOnClickListener(v -> {
            //when user click on time input,
            //open time picker
            constructTimePickerForLectureTimeSelection("lectureStartTime");
        });

        finishingTimeSelector.setOnClickListener(v -> {
            constructTimePickerForLectureTimeSelection("lectureFinishingTime");
        });

        confirmLectureButton.setOnClickListener(v -> {
            //user click confirm button
            handleLectureUiClick();
        });
    }

    /**
     * key - lectureStartTime or lectureEndTime
     * maximum schedule time - 18:00
     * minimum schedule time - 8:00
     *
     * @param key The mode to determine if start or end time is selected.
     */
    private void constructTimePickerForLectureTimeSelection(String key) {
        String titleText = key.equals("lectureStartTime") ? "Lecture Commencing Time" : "Lecture Finishing Time";
        Calendar currentTime = Calendar.getInstance();
        //create a timepicker to initially show current time from system
        MaterialTimePicker theTimePicker = new MaterialTimePicker.Builder()
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK) //show clock face inputs
                .setHour(currentTime.get(Calendar.HOUR))
                .setMinute(currentTime.get(Calendar.MINUTE))
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText(titleText)
                .build();

        theTimePicker.addOnCancelListener(DialogInterface::cancel);
        theTimePicker.addOnDismissListener(DialogInterface::dismiss);
        theTimePicker.addOnPositiveButtonClickListener(v -> {
            //user submit the time
            //retrieve the hour and minute the user selected.

            //used to format the time
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            Calendar formatHelper = Calendar.getInstance();

            int selectedHour = theTimePicker.getHour();
            int selectedMinute = theTimePicker.getMinute();

            formatHelper.set(Calendar.HOUR_OF_DAY, selectedHour);
            formatHelper.set(Calendar.MINUTE, selectedMinute);

            String formattedTime = timeFormatter.format(formatHelper.getTime());

            if (key.equalsIgnoreCase("lectureStartTime")) {
                //set time on start time
                lectureCommencingTime.setText(formattedTime);
            } else {
                //set time on end time
                lectureFinishingTime.setText(formattedTime);
            }
        });


        theTimePicker.show(getSupportFragmentManager(), "LECTURE_TIME_PICKER");
    }

    private void constructDatePickerForLectureDateSelection() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Lecture Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .build();

        datePicker.show(getSupportFragmentManager(), "LECTURE_DATE_PICKER");

        datePicker.addOnCancelListener(DialogInterface::dismiss); //when user clicks cancel, dismiss dialog
        datePicker.addOnPositiveButtonClickListener(selection -> {
            //user clicks okay
            //method input - millisecond of time date
            //show the selected lecture date.
            SimpleDateFormat theFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
            SimpleDateFormat theServerFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date userSelectedDateInDateFormat = new Date(selection);
            selectedDateInUi = theServerFormat.format(userSelectedDateInDateFormat);

            Calendar todayDate = Calendar.getInstance();
            //user picked time gets set to the locale of gmt +5:30
            todayDate.set(Calendar.HOUR_OF_DAY, 5);
            todayDate.set(Calendar.MINUTE, 30);
            todayDate.set(Calendar.SECOND, 0);
            todayDate.set(Calendar.MILLISECOND, 0);

            if (userSelectedDateInDateFormat.getTime() < todayDate.getTimeInMillis()) {
                //user selected previous date, do not allow
                constructError("Please select a date that is today or after today", true);
            } else {
                String selectedDate = theFormat.format(userSelectedDateInDateFormat);
                lectureDate.setText(selectedDate);
            }
        });
    }

    private void getReferences() {
        this.lectureService = APIConfigurer.getApiConfigurer().getLectureService();
        this.theToolbar = findViewById(R.id.toolbar);
        this.loadingBar = findViewById(R.id.progress_bar);
        this.moduleName = findViewById(R.id.module_information);
        this.moduleTaughtBy = findViewById(R.id.lecturer_name_schedule);
        this.lectureDate = findViewById(R.id.lecture_date);
        this.lectureCommencingTime = findViewById(R.id.start_time);
        this.lectureFinishingTime = findViewById(R.id.end_time);
        this.confirmLectureButton = findViewById(R.id.confirm_lecture_button);
        this.commencingTimeSelector = findViewById(R.id.start_time_select_button);
        this.lectureDateSelector = findViewById(R.id.lecture_date_select_button);
        this.finishingTimeSelector = findViewById(R.id.finish_time_select_button);
        this.classroomList = findViewById(R.id.classroom_list);
        this.checkboxLayout = findViewById(R.id.checkbox_layout);
        this.endTimeLayout = findViewById(R.id.end_time_layout);
        this.startTimeLayout = findViewById(R.id.start_time_input_layout);
        this.lectureDateLayout = findViewById(R.id.lecture_date_input_layout);
        this.classroomLayout = findViewById(R.id.classroom_list_layout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //load module and enrolled batch information and the classroom list to schedule lecture
        getModuleForLectureSchedule();
    }

    private void getModuleForLectureSchedule() {
        confirmLectureButton.setEnabled(true);
        loadingBar.setVisibility(View.VISIBLE);

        Call<HashMap<String, Object>> classroomAndModuleCall = lectureService.getModuleInformationAndClassroomForSchedule(token, moduleIdPassedFromIntent);
        classroomAndModuleCall.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<HashMap<String, Object>> call, @NonNull Response<HashMap<String, Object>> response) {
                loadingBar.setVisibility(View.GONE);
                try {
                    handleOnResponse(response);
                } catch (IOException e) {
                    confirmLectureButton.setEnabled(false);
                    Log.e("onResponse", e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<HashMap<String, Object>> call, @NonNull Throwable t) {
                loadingBar.setVisibility(View.GONE);
                confirmLectureButton.setEnabled(false);
                constructError(t.getLocalizedMessage(), false);
            }
        });
    }

    private void handleOnResponse(Response<HashMap<String, Object>> response) throws IOException {
        if (response.isSuccessful()) {
            //key present in the HashMap sent from API - /findModuleAndClassroomsForLectureSchedule/{moduleId}
            //Lecture API
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, Object> responseBody = response.body();
            //type reference used to obtain the type of the generic class.
            loadedClassrooms = objectMapper.convertValue(responseBody.get("theClassroomList"), new TypeReference<List<Classroom>>() {
            });
            loadedModule = objectMapper.convertValue(responseBody.get("theModuleInformation"), Module.class);

            showInView();
        } else {
            constructError(
                    APIConfigurer.getTheErrorReturned(response.errorBody()).getErrorMessage(), false
            );
            confirmLectureButton.setEnabled(false);
        }
    }

    private void showInView() {
        //show module and classrooms in view and ask to schedule lecture.
        moduleName.setText(loadedModule.getModuleName());
        moduleTaughtBy.setText(String.format("Taught By - %s %s", loadedModule.getTheLecturer().getFirstName(), loadedModule.getTheLecturer().getLastName()));

        //create an array adapter that can be used in the classroom layout containing one single text view
        ArrayAdapter<String> classroomNameList = new ArrayAdapter<String>(this, R.layout.classroom_layout);
        for (Classroom eachClassroomInDb : loadedClassrooms) {
            //add the classroom information to the array adapter
            classroomNameList.add(String.format(Locale.ENGLISH,
                    "%s\nCapacity - %d \nAC - %s\nSmart Board - %s"
                    , eachClassroomInDb.getClassroomName(), eachClassroomInDb.getMaxCapacity(), eachClassroomInDb.isAcPresent(), eachClassroomInDb.isSmartBoardPresent()));
        }
        //set the adapter so that for the classroom dropdown the classroom list from the database will be available.
        classroomList.setAdapter(classroomNameList);

        //create dynamic checkboxes to select the batch
        for (BatchShow eachBatch : loadedModule.getTheBatchList()) {
            eachBatchCheckbox = new MaterialCheckBox(this);
            eachBatchCheckbox.setText(eachBatch.getBatchCode());

            eachBatchCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //method executed when the checkbox gets checked or unchecked.
                if (isChecked) {
                    //insert the batch name to the selected batch list
                    this.selectedBatches.add(buttonView.getText().toString());
                } else {
                    //user de-selected the batch, therefore, remove the batch from the list
                    for (String eachBatchInSelectedList : selectedBatches) {
                        if (eachBatchInSelectedList.equalsIgnoreCase(buttonView.getText().toString())) {
                            //remove the string
                            selectedBatches.remove(eachBatchInSelectedList);
                            break;
                        }
                    }
                }
            });
            checkboxLayout.addView(eachBatchCheckbox);
        }
    }

    private void constructError(String errorMessage, boolean isInfo) {
        Snackbar theSnackBar = Snackbar.make(theToolbar, errorMessage, Snackbar.LENGTH_LONG);
        if (!isInfo) {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        } else {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        }
        View view = theSnackBar.getView();
        //retrieve the underling text view on the snack bar and increase the lines on it to display full message
        TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
        snackBarText.setMaxLines(5);
        theSnackBar.show();
    }

    /**
     * Method executed once user clicks confirm lecture button
     */
    private void handleLectureUiClick() {
        loadingBar.setVisibility(View.VISIBLE);
        //retrieve inputs
        String selectedClassroomName = classroomList.getText().toString();
        String selectedDate = lectureDate.getText().toString();
        String startTime = lectureCommencingTime.getText().toString();
        String endTime = lectureFinishingTime.getText().toString();
        String[] selectedBatchCodes = selectedBatches.toArray(new String[selectedBatches.size()]);

        //perform validations on the front end before sending to server.
        boolean isValid = validateLectureInputs(selectedClassroomName, selectedDate, startTime, endTime, selectedBatchCodes);

        //create date formatters
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        if (isValid) {
            //format parsed date from initially set in date picker
            try {
                java.util.Date passingDateToServer = dateFormat.parse(selectedDateInUi);
                int classroomId = getClassroomId(selectedClassroomName);

                //create a DTO POJO used to transmit data to server
                LectureCreate lectureCreate = new LectureCreate();
                lectureCreate.setBatchList(selectedBatchCodes);
                lectureCreate.setEndTime(endTime);
                lectureCreate.setStartTime(startTime);
                lectureCreate.setLectureDate(passingDateToServer);
                lectureCreate.setClassroomID(classroomId);
                lectureCreate.setModuleId(loadedModule.getModuleId());

                manageLectureInDB(lectureCreate);
            } catch (ParseException e) {
                Log.e("scheduleLecture", e.getLocalizedMessage());
            }
        } else {
            loadingBar.setVisibility(View.GONE);
        }

    }

    private void manageLectureInDB(LectureCreate lectureToBeManaged) {
        //if lecture is being created by user execute the create lecture api call
        Call<SuccessResponseAPI> createLectureApiCall = lectureService.createLecture(lectureToBeManaged, token);
        createLectureApiCall.enqueue(new Callback<SuccessResponseAPI>() {
            @Override
            public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                loadingBar.setVisibility(View.GONE);
                handleLectureManagedResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                loadingBar.setVisibility(View.GONE);
                constructError("We ran into an unknown error while scheduling the lecture", false);
            }
        });
    }

    private void handleLectureManagedResponse(Response<SuccessResponseAPI> response) {
        if (response.isSuccessful()) {
            //lecture created successfully
            Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
            //navigate to lecturer modules
            startActivity(new Intent(this, LecturerModules.class));
            finish(); //remove activity from back trace so that when user clicks back, it doesn't navigate to this
        } else {
            try {
                ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                constructError(theErrorReturned.getErrorMessage(), false);
            } catch (IOException e) {
                e.printStackTrace();
                constructError("We ran into an unknown error while scheduling the lecture", false);
            }
        }
    }

    private int getClassroomId(String selectedClassroomName) {
        String[] classroomSplit = selectedClassroomName.split("\n");
        String classroomName = classroomSplit[0];
        for (Classroom eachClassroom : loadedClassrooms) {
            if (eachClassroom.getClassroomName().trim().equalsIgnoreCase(classroomName)) {
                return eachClassroom.getClassroomId();
            }
        }
        return 0;
    }

    /**
     * Validate inputs before proceeding with API Call
     *
     * @param selectedClassroomName The selected classroom
     * @param selectedDate          The entered date
     * @param startTime             The entered start time
     * @param endTime               The entered end time
     * @param selectedBatchCodes    The selected batch codes
     * @return The boolean to indicate if validation pass or fail
     */
    private boolean validateLectureInputs(String selectedClassroomName, String selectedDate, String startTime, String endTime, String[] selectedBatchCodes) {
        boolean isValid = false;
        boolean areTimesPresent = false;
        boolean isStartValid = false; //check if start time is valid.
        boolean isEndValid = false; //check if end time is valid.

        if (TextUtils.isEmpty(selectedClassroomName)) {
            classroomLayout.setError("Please select a classroom to proceed");
            return false;
        } else {
            isValid = true;
            classroomLayout.setError(null);
        }

        if (selectedDate.trim().equalsIgnoreCase("mm/dd/yyyy")) {
            areTimesPresent = false;
            lectureDateLayout.setError("Please provide a date to proceed");
            return false;
        } else {
            areTimesPresent = true;
            isValid = true;
            lectureDateLayout.setError(null);
        }

        if (startTime.trim().equalsIgnoreCase("--:--")) {
            areTimesPresent = false;
            startTimeLayout.setError("Please provide a commencing time to proceed");
            return false;
        } else {
            isValid = true;
            areTimesPresent = true;
            startTimeLayout.setError(null);
        }

        if (endTime.trim().equalsIgnoreCase("--:--")) {
            areTimesPresent = false;
            endTimeLayout.setError("Please provide a finishing time to proceed");
            return false;
        } else {
            endTimeLayout.setError(null);
        }

        if (selectedBatchCodes.length == 0) {
            constructError("Please select a batch to proceed", false);
            return false;
        }

        //validate the times.
        //check if start time == end time
        //check if end time before start time
        //check if start time before min and after max time
        //check if end time before min and after max time
        SimpleDateFormat theDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
        try {
            //format the start and end time to the defined format in the simple date format
            java.util.Date enteredStartTime = theDateFormat.parse(String.format("%s %s", selectedDate, startTime));
            java.util.Date enteredEndTime = theDateFormat.parse(String.format("%s %s", selectedDate, endTime));

            if (enteredEndTime.equals(enteredStartTime)) {
                //if lecture ends at start
                endTimeLayout.setError("The finishing time cannot be the start time");
                return false;
            } else {
                if (enteredEndTime.before(enteredStartTime)) {
                    //if lecture ends before start time.
                    endTimeLayout.setError("Please ensure finishing time is after start time");
                    return false;
                } else {
                    java.util.Date minTime = theDateFormat.parse(String.format("%s %s", selectedDate, "08:00"));
                    java.util.Date maxTime = theDateFormat.parse(String.format("%s %s", selectedDate, "18:00"));

                    if (enteredStartTime.before(minTime) || enteredStartTime.after(maxTime)) {
                        //start time falls before 8 or after 18
                        startTimeLayout.setError("Commencing time must be between 08:00 and 18:00");
                        isStartValid = false; //start time invalid.
                    } else {
                        startTimeLayout.setError(null);
                        isStartValid = true; //start time valid.
                    }

                    if (enteredEndTime.before(minTime) || enteredEndTime.after(maxTime)) {
                        //end time falls before 8 or after 18
                        endTimeLayout.setError("Finishing time must be between 08:00 and 18:00");
                        isEndValid = false; //end time not valid
                    } else {
                        endTimeLayout.setError(null);
                        isEndValid = true; //end time not valid
                    }
                }
            }
        } catch (ParseException e) {
            isValid = false;
            Log.e("Validating Inputs", e.getLocalizedMessage());
        }
        //return the absolute boolean end of validation
        return isValid && isStartValid && isEndValid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //after press back go back to previous page.
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        //after press back go back to previous page.
        super.onBackPressed();
    }
}