package com.cb007787.timetabler.view.lecturer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.Classroom;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.LectureService;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private LectureService lectureService;
    private Toolbar theToolbar;
    private LinearProgressIndicator loadingBar;
    private SwipeRefreshLayout swiper;

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
    private Button scheduleLectureButton;

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

        swiper.setOnRefreshListener(this::getModuleForLectureSchedule);

        lectureDateSelector.setOnClickListener(v -> constructDatePickerForLectureDateSelection());

        commencingTimeSelector.setOnClickListener(v -> {
            //when user click on time input,
            //open time picker
            constructTimePickerForLectureTimeSelection("lectureStartTime");
        });

        finishingTimeSelector.setOnClickListener(v -> {
            constructTimePickerForLectureTimeSelection("lectureFinishingTime");
        });

        scheduleLectureButton.setOnClickListener(v -> {
            //user click schedule button
            handleScheduleClick();
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

            formatHelper.set(Calendar.HOUR, selectedHour);
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
            Date userSelectedDateInDateFormat = new Date(selection);

            Calendar todayDate = Calendar.getInstance();
            todayDate.set(Calendar.HOUR, 0);
            todayDate.set(Calendar.MINUTE, 0);
            todayDate.set(Calendar.SECOND, 0);

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
        this.swiper = findViewById(R.id.swiper);
        this.moduleName = findViewById(R.id.module_information);
        this.moduleTaughtBy = findViewById(R.id.lecturer_name_schedule);
        this.lectureDate = findViewById(R.id.lecture_date);
        this.lectureCommencingTime = findViewById(R.id.start_time);
        this.lectureFinishingTime = findViewById(R.id.end_time);
        this.scheduleLectureButton = findViewById(R.id.schedule_lecture_button);
        this.commencingTimeSelector = findViewById(R.id.start_time_select_button);
        this.lectureDateSelector = findViewById(R.id.lecture_date_select_button);
        this.finishingTimeSelector = findViewById(R.id.finish_time_select_button);
        this.classroomList = findViewById(R.id.classroom_list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //load module and enrolled batch information and the classroom list to schedule lecture
        getModuleForLectureSchedule();
    }

    private void getModuleForLectureSchedule() {
        loadingBar.setVisibility(View.VISIBLE);

        Call<HashMap<String, Object>> classroomAndModuleCall = lectureService.getModuleInformationAndClassroomForSchedule(token, moduleIdPassedFromIntent);
        classroomAndModuleCall.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<HashMap<String, Object>> call, @NonNull Response<HashMap<String, Object>> response) {
                loadingBar.setVisibility(View.GONE);
                if (swiper.isRefreshing()) {
                    swiper.setRefreshing(false);
                }
                try {
                    handleOnResponse(response);
                } catch (IOException e) {
                    Log.e("onResponse", e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<HashMap<String, Object>> call, @NonNull Throwable t) {
                loadingBar.setVisibility(View.GONE);
                if (swiper.isRefreshing()) {
                    swiper.setRefreshing(false);
                }
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
                    APIConfigurer.getTheErrorReturned(response.errorBody()).getErrorMessage(), true
            );
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
                    "%s \nCapacity - %d \nAC - %s\nSmart Board - %s"
                    , eachClassroomInDb.getClassroomName(), eachClassroomInDb.getMaxCapacity(), eachClassroomInDb.isAcPresent(), eachClassroomInDb.isSmartBoardPresent()));
        }
        //set the adapter so that for the classroom dropdown the classroom list from the database will be available.
        classroomList.setAdapter(classroomNameList);
    }

    private void constructError(String errorMessage, boolean isInfo) {
        Snackbar theSnackBar = Snackbar.make(theToolbar, errorMessage, Snackbar.LENGTH_LONG);
        if (!isInfo) {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        } else {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        }
        theSnackBar.show();
    }

    /**
     * Method executed once user clicks schedule lecture.
     */
    private void handleScheduleClick() {
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