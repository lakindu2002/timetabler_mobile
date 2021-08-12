package com.cb007787.timetabler.view.lecturer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateLecture extends DialogFragment {

    private int lectureIdToEdit;
    private LectureService lectureService;
    private String token;
    private String selectedDateInUi = null; //formatted date in "yyyy-mm-dd"

    private ImageView backButton;
    private LinearProgressIndicator progressIndicator;
    private MaterialTextView moduleName;
    private MaterialTextView moduleTaughtBy;
    private MaterialTextView classroomCurrent;
    private MaterialTextView batches;
    private TextInputLayout classroomLayout;
    private TextInputLayout lectureDateLayout;
    private TextInputLayout startTimeLayout;
    private TextInputLayout endTimeLayout;
    private AutoCompleteTextView selectedClassroom;
    private TextInputEditText lectureDate;
    private TextInputEditText startTime;
    private TextInputEditText endTime;
    private ImageButton finishTimeButton;
    private ImageButton startTimeButton;
    private ImageButton calendarButton;
    private Button rescheduleButton;

    private LectureCreate createdLecture;
    private List<Classroom> possibleClassrooms;
    private List<BatchShow> batchesHavingThisLecture;
    private Module theModuleForLecture;
    private Classroom currentClassroom;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UpdateListener actionListener; //listener triggered after successfully updating the data

    public interface UpdateListener {
        void updateSuccess();
    }

    public void setActionListener(UpdateListener actionListener) {
        this.actionListener = actionListener;
    }

    /**
     * Used to create new fragment instance
     *
     * @param lectureIdToEdit The lecture id to be passed to the fragment via the bunder
     * @return The Update Lecture dialog fragment.
     */
    public static UpdateLecture newInstance(int lectureIdToEdit) {
        Bundle args = new Bundle();

        UpdateLecture fragment = new UpdateLecture();
        args.putInt("lectureId", lectureIdToEdit);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadEditInformation();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyles);
        //validate jwt to ensure user has valid token.
        SharedPreferenceService.validateToken(requireContext(), PreferenceInformation.PREFERENCE_NAME);
        if (getArguments() != null) {
            //Return the arguments supplied when the fragment was instantiated
            //there is a saved instance, retrieve the lecture ID.
            this.lectureIdToEdit = getArguments().getInt("lectureId", 0);
        }
    }

    /**
     * The system calls this to get the DialogFragment's layout, regardless
     * of whether it's being displayed as a dialog or an embedded fragment.
     **/
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LayoutInflater theInflater = LayoutInflater.from(getContext());
        View inflatedDialog = theInflater.inflate(R.layout.dialog_update_lecture, container, false);

        getReferences(inflatedDialog);

        return inflatedDialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeToolBar();
        lectureDate.setEnabled(false);
        startTime.setEnabled(false);
        endTime.setEnabled(false);

        calendarButton.setOnClickListener(v -> constructDatePickerForLectureDateSelection());

        startTimeButton.setOnClickListener(v -> {
            //when user click on time input,
            //open time picker
            constructTimePickerForLectureTimeSelection("lectureStartTime");
        });

        finishTimeButton.setOnClickListener(v -> {
            constructTimePickerForLectureTimeSelection("lectureFinishingTime");
        });

        rescheduleButton.setOnClickListener(v -> {
            //user click confirm button
            updateLectureClick();
        });
    }

    private void getReferences(View inflatedDialog) {
        this.backButton = inflatedDialog.findViewById(R.id.back_button);
        this.lectureService = APIConfigurer.getApiConfigurer().getLectureService();
        this.token = SharedPreferenceService.getToken(requireContext(), PreferenceInformation.PREFERENCE_NAME);
        this.progressIndicator = inflatedDialog.findViewById(R.id.progress_bar);
        this.moduleName = inflatedDialog.findViewById(R.id.module_information);
        this.moduleTaughtBy = inflatedDialog.findViewById(R.id.lecturer_name_schedule);
        this.classroomCurrent = inflatedDialog.findViewById(R.id.classroom_name_current);
        this.batches = inflatedDialog.findViewById(R.id.batches_scheduled);
        this.classroomLayout = inflatedDialog.findViewById(R.id.classroom_list_layout);
        this.lectureDateLayout = inflatedDialog.findViewById(R.id.lecture_date_input_layout);
        this.startTimeLayout = inflatedDialog.findViewById(R.id.start_time_input_layout);
        this.endTimeLayout = inflatedDialog.findViewById(R.id.end_time_layout);
        this.selectedClassroom = inflatedDialog.findViewById(R.id.classroom_list);
        this.lectureDate = inflatedDialog.findViewById(R.id.lecture_date);
        this.startTime = inflatedDialog.findViewById(R.id.start_time);
        this.endTime = inflatedDialog.findViewById(R.id.end_time);
        this.finishTimeButton = inflatedDialog.findViewById(R.id.finish_time_select_button);
        this.startTimeButton = inflatedDialog.findViewById(R.id.start_time_select_button);
        this.calendarButton = inflatedDialog.findViewById(R.id.lecture_date_select_button);
        this.rescheduleButton = inflatedDialog.findViewById(R.id.confirm_lecture_button);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        dialog.dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        dialog.cancel();
    }

    private void initializeToolBar() {
        backButton.setOnClickListener(v -> {
            this.dismiss();
        });
    }

    private void loadEditInformation() {
        this.progressIndicator.setVisibility(View.VISIBLE);
        Call<HashMap<String, Object>> theUpdateCall = lectureService.getTheUpdateInformationForLecture(token, lectureIdToEdit);
        theUpdateCall.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<HashMap<String, Object>> call, @NonNull Response<HashMap<String, Object>> response) {
                progressIndicator.setVisibility(View.GONE);
                handleFetchInformationOnResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<HashMap<String, Object>> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                constructMessage("We ran into an unexpected error while processing your request", false);
            }
        });
    }

    private void handleFetchInformationOnResponse(Response<HashMap<String, Object>> response) {
        if (response.isSuccessful()) {
            HashMap<String, Object> theReturnedBody = response.body();
            //keys provided by api during return
            //use object mapper to map returned data to the required type
            //type references used to infer objects of generic type.
            this.possibleClassrooms = objectMapper.convertValue(theReturnedBody.get("possibleClassrooms"), new TypeReference<List<Classroom>>() {
            });
            this.batchesHavingThisLecture = objectMapper.convertValue(theReturnedBody.get("batchesHavingTheLecture"), new TypeReference<List<BatchShow>>() {
            });
            this.createdLecture = objectMapper.convertValue(theReturnedBody.get("lectureInformation"), LectureCreate.class);
            this.theModuleForLecture = objectMapper.convertValue(theReturnedBody.get("theModuleForLecture"), Module.class);
            this.currentClassroom = objectMapper.convertValue(theReturnedBody.get("currentClassroom"), Classroom.class);
            showDataInView(); //update UI elements.
        } else {
            try {
                ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                constructMessage(theErrorReturned.getErrorMessage(), false);
            } catch (IOException e) {
                e.printStackTrace();
                constructMessage("We ran into an unexpected error while processing your request", false);
            }
        }
    }

    private void showDataInView() {
        moduleName.setText(theModuleForLecture.getModuleName());
        moduleTaughtBy.setText(String.format("Taught By - %s %s", theModuleForLecture.getTheLecturer().getFirstName(), theModuleForLecture.getTheLecturer().getLastName()));
        classroomCurrent.setText(String.format("At - %s", currentClassroom.getClassroomName()));

        StringBuilder batchNames = new StringBuilder();
        for (BatchShow eachBatch : batchesHavingThisLecture) {
            batchNames.append(eachBatch.getBatchCode()).append(", ");
        }
        batches.setText(String.format("Conducted For - %s", batchNames.toString()));

        ArrayAdapter<String> possibleClassroomList = new ArrayAdapter<String>(requireContext(), R.layout.classroom_layout);
        for (Classroom eachClassroomInDb : possibleClassrooms) {
            //add the classroom information to the array adapter
            possibleClassroomList.add(String.format(Locale.ENGLISH,
                    "%s\nCapacity - %d \nAC - %s\nSmart Board - %s"
                    , eachClassroomInDb.getClassroomName(), eachClassroomInDb.getMaxCapacity(), eachClassroomInDb.isAcPresent(), eachClassroomInDb.isSmartBoardPresent()));
        }
        selectedClassroom.setAdapter(possibleClassroomList);
        lectureDate.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).format(createdLecture.getLectureDate()));
        startTime.setText(createdLecture.getStartTime());
        endTime.setText(createdLecture.getEndTime());
    }

    private void updateLectureClick() {
        progressIndicator.setVisibility(View.VISIBLE);
        String classroomSelected = selectedClassroom.getText().toString().trim();
        String selectedDate = lectureDate.getText().toString().trim();
        String selectedStartTime = startTime.getText().toString();
        String selectedEndTime = endTime.getText().toString();

        boolean isValid = validateLectureInputs(classroomSelected, selectedDate, selectedStartTime, selectedEndTime);
        if (isValid) {
            try {
                createdLecture.setStartTime(selectedStartTime);
                createdLecture.setEndTime(selectedEndTime);
                createdLecture.setClassroomID(getClassroomId(classroomSelected));
                if (selectedDateInUi == null) {
                    //user didnt select a lecture date, take the current one
                    createdLecture.setLectureDate(createdLecture.getLectureDate());
                } else {
                    createdLecture.setLectureDate(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(selectedDateInUi));
                }

                updateLectureInformationInDb();
            } catch (ParseException e) {
                constructMessage("Failed to process the selected date", false);
                progressIndicator.setVisibility(View.GONE);
            }
        } else {
            progressIndicator.setVisibility(View.GONE);
        }
    }

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
                startTime.setText(formattedTime);
            } else {
                //set time on end time
                endTime.setText(formattedTime);
            }
        });


        theTimePicker.show(getParentFragmentManager(), "LECTURE_TIME_PICKER");
    }

    private void constructDatePickerForLectureDateSelection() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Lecture Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .build();

        datePicker.show(getParentFragmentManager(), "LECTURE_DATE_PICKER");

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
                constructMessage("Please select a date that is today or after today", true);
            } else {
                String selectedDate = theFormat.format(userSelectedDateInDateFormat);
                lectureDate.setText(selectedDate);
            }
        });
    }


    /**
     * Validate inputs before proceeding with API Call
     *
     * @param selectedClassroomName The selected classroom
     * @param selectedDate          The entered date
     * @param startTime             The entered start time
     * @param endTime               The entered end time
     * @return The boolean to indicate if validation pass or fail
     */
    private boolean validateLectureInputs(String selectedClassroomName, String selectedDate, String startTime, String endTime) {
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

    private int getClassroomId(String classroomSelected) {
        //split by new line as i create the list view on autocomplete with new line to separate each word.
        String[] classroomSplit = classroomSelected.split("\n");
        String classroomName = classroomSplit[0];
        for (Classroom eachClassroom : possibleClassrooms) {
            if (eachClassroom.getClassroomName().trim().equalsIgnoreCase(classroomName)) {
                return eachClassroom.getClassroomId();
            }
        }
        return 0;
    }


    private void updateLectureInformationInDb() {
        Call<SuccessResponseAPI> updateCall = lectureService.rescheduleLecture(token, createdLecture, createdLecture.getLectureId());
        updateCall.enqueue(new Callback<SuccessResponseAPI>() {
            @Override
            public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                progressIndicator.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    actionListener.updateSuccess(); //call the custom listener to trigger a refresh in the lecture view for user.
                    dismiss(); //hide modal
                } else {
                    try {
                        constructMessage(
                                APIConfigurer.getTheErrorReturned(response.errorBody()).getErrorMessage(), false
                        );
                    } catch (IOException e) {
                        constructMessage("We ran into an unexpected error. Please try again", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                constructMessage("We ran into an unexpected error. Please try again", false);
            }
        });
    }

    private void constructMessage(String errorMessage, boolean isInfo) {
        Snackbar theSnackBar = Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG);
        if (isInfo) {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        } else {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        }
        theSnackBar.show();
    }

}
