package com.cb007787.timetabler.view.academic_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.LectureShow;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.recyclers.LectureRecycler;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.LectureService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * View lectures per batch.
 */
public class AcademicAdminViewLecturesPerBatch extends AppCompatActivity {

    private String batchCode;
    private LectureService lectureService;
    private String token;
    private AuthReturn loggedInUser;
    private List<LectureShow> lecturesPerBatch = new ArrayList<>();


    private Toolbar toolbar;
    private LinearProgressIndicator progressIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LectureRecycler adapter;
    private ImageButton datePickerBtn;
    private TextInputEditText selectedDate;

    private Date startDate;
    private Date endDate;
    private SimpleDateFormat simpleDateFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_admin_view_lectures_per_batch);
        getReferences();

        //format selected date.
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);
        try {
            loggedInUser = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
        } catch (JsonProcessingException e) {
            Log.e("JsonProcessingException", e.getLocalizedMessage());
        }

        Intent intent = getIntent();
        if (intent != null) {
            batchCode = intent.getStringExtra("batchCode");
        }

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        }

        adapter = new LectureRecycler(this, lecturesPerBatch);
        LectureRecycler.setUserRole(loggedInUser.getRole());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter); //bind adapter to recycler view to load lectures.

        datePickerBtn.setOnClickListener(v -> {
            loadDatePicker();
        });

        swipeRefreshLayout.setOnRefreshListener(this::loadLecturesPerBatch);

        selectedDate.setEnabled(false);

        adapter.setLectureCancelAdminListener(this::cancelLectureOnDb);
    }

    @Override
    protected void onStart() {
        super.onStart();
        toolbar.setTitle("Timetable - " + batchCode);
        loadLecturesPerBatch(); //get all lectures for batch from the api.
    }

    private void getReferences() {
        toolbar = findViewById(R.id.tool_bar);
        recyclerView = findViewById(R.id.recycler);
        swipeRefreshLayout = findViewById(R.id.swiper);
        progressIndicator = findViewById(R.id.progress_bar);
        datePickerBtn = findViewById(R.id.date_picker_filter);
        selectedDate = findViewById(R.id.selected_dates);
        lectureService = APIConfigurer.getApiConfigurer().getLectureService();
    }

    private void loadDatePicker() {
        MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder
                .dateRangePicker()
                .setTitleText("Select Start and End Dates")
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .build();

        datePicker.addOnCancelListener(DialogInterface::cancel);
        datePicker.addOnPositiveButtonClickListener(selection -> {
            //selection is a "Pair<Long,Long>" that has start and end time in ms.
            Long startTime = selection.first;
            Long endTime = selection.second;

            Calendar startCalendar = Calendar.getInstance();
            Calendar endCalendar = Calendar.getInstance();

            startCalendar.setTimeInMillis(startTime);
            endCalendar.setTimeInMillis(endTime);

            startDate = startCalendar.getTime();
            endDate = endCalendar.getTime();

            //format the selected date and show on view and then filter.
            selectedDate.setText(String.format(
                    "From - %s to - %s", simpleDateFormat.format(startDate), simpleDateFormat.format(endDate)
            ));

            filterTheLecturesForStartDateAndTime();
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void filterTheLecturesForStartDateAndTime() {
        List<LectureShow> filteredList =
                this.lecturesPerBatch.stream().filter(
                        //filter lecture date >= start date || lecture date <= end date
                        //between start and end date all lectures
                        (eachLecture) -> (eachLecture.getLectureDate().after(startDate) || eachLecture.getLectureDate().equals(startDate))
                                && (eachLecture.getLectureDate().before(endDate) || eachLecture.getLectureDate().equals(endDate))
                ).collect(Collectors.toList());

        adapter.setTheLectureList(filteredList); //notify the data set change and update the adapter.

        if (filteredList.size() == 0) {
            constructError("There are no lectures available for the given date", true);
        }
    }

    public void loadLecturesPerBatch() {
        //clear inputs on refresh
        startDate = null;
        endDate = null;
        selectedDate.setText(null);
        progressIndicator.setVisibility(View.VISIBLE);
        lectureService.loadLecturesPerBatch(batchCode, token).enqueue(new Callback<List<LectureShow>>() {
            @Override
            public void onResponse(@NonNull Call<List<LectureShow>> call, @NonNull Response<List<LectureShow>> response) {
                progressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful()) {
                    lecturesPerBatch = response.body();
                    toolbar.setTitle("Timetable - " + batchCode);
                    adapter.setTheLectureList(lecturesPerBatch);
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        constructError("We ran into an error while fetching lectures for this batch", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LectureShow>> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                t.printStackTrace();
                constructError("We ran into an error while fetching lectures for this batch", false);
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

    private void cancelLectureOnDb(LectureShow theLecture) {
        progressIndicator.setVisibility(View.VISIBLE);
        lectureService.cancelLectureForAdmin(token, theLecture.getLectureId(), batchCode).enqueue(new Callback<SuccessResponseAPI>() {
            @Override
            public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                progressIndicator.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Snackbar theSnackBar = Snackbar.make(toolbar, response.body().getMessage(), Snackbar.LENGTH_LONG);
                    theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_success, null));
                    View view = theSnackBar.getView();
                    //retrieve the underling text view on the snack bar and increase the lines on it to display full message
                    TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
                    snackBarText.setMaxLines(5);
                    theSnackBar.show();
                    loadLecturesPerBatch(); //load all lecs for the batch.
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while cancelling the lecture", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                constructError("We ran into an error while cancelling the lecture", false);
            }
        });
    }
}