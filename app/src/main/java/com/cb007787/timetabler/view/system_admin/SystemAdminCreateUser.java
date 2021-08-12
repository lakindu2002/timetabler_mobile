package com.cb007787.timetabler.view.system_admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SystemAdminCreateUser extends AppCompatActivity {

    private Toolbar theToolbar;
    private LinearProgressIndicator loadingBar;
    private ImageButton datePicker;

    private Calendar selectedDate = null;

    private TextInputLayout usernameLayout;
    private TextInputEditText usernameField;
    private TextInputLayout firstNameLayout;
    private TextInputEditText firstNameField;
    private TextInputLayout lastNameLayout;
    private TextInputEditText lastNameField;
    private TextInputLayout contactNumberLayout;
    private TextInputEditText contactNumberField;
    private TextInputLayout emailAddressLayout;
    private TextInputEditText emailAddressField;
    private TextInputLayout dateOfBirthLayout;
    private TextInputEditText dateOfBirthField;
    private TextInputLayout userRoleLayout;
    private AutoCompleteTextView userRoleField;
    private Button createUserBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_admin_create_user);
        getReferences();
        dateOfBirthField.setEnabled(false); //disable the date of birth to prevent type

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);

        setSupportActionBar(theToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //enable back button.
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        datePicker.setOnClickListener(v -> {
            //open date picker
            openDatePicker();
        });
    }

    private void getReferences() {
        this.theToolbar = findViewById(R.id.the_toolbar);
        this.loadingBar = findViewById(R.id.progress_bar);
        this.datePicker = findViewById(R.id.imageButton);
        this.usernameLayout = findViewById(R.id.username_layout);
        this.usernameField = findViewById(R.id.username);
        this.firstNameLayout = findViewById(R.id.first_name_layout);
        this.firstNameField = findViewById(R.id.first_name);
        this.lastNameLayout = findViewById(R.id.last_name_layout);
        this.lastNameField = findViewById(R.id.last_name);
        this.contactNumberLayout = findViewById(R.id.contact_number_layout);
        this.contactNumberField = findViewById(R.id.contact_number);
        this.emailAddressLayout = findViewById(R.id.email_address_layout);
        this.emailAddressField = findViewById(R.id.email_address);
        this.dateOfBirthLayout = findViewById(R.id.date_of_birth_layout);
        this.dateOfBirthField = findViewById(R.id.date_of_birth);
        this.userRoleLayout = findViewById(R.id.user_type_layout);
        this.userRoleField = findViewById(R.id.user_role);
        this.createUserBtn = findViewById(R.id.create_user);
    }

    private void openDatePicker() {
        //method executes when user clicks on date button
        MaterialDatePicker<Long> dateOfBirthPicker = MaterialDatePicker.Builder
                .datePicker() //create a date picker
                .setTitleText("Select Date of Birth")
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR) //use calendar input
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) //set calendar todays date
                .build();

        dateOfBirthPicker.addOnCancelListener(DialogInterface::cancel); //when user clicks cancel
        dateOfBirthPicker.addOnPositiveButtonClickListener(selectedTimeInMs -> {
            //when user confirms the date selected
            Calendar selectedTime = Calendar.getInstance(); //get a calendar instance
            selectedTime.setTimeInMillis(selectedTimeInMs); //set the selected millisecond time as calendar time
            selectedDate = selectedTime; //set the selected time to global time

            //format the time into Month/Date/Year and display on input box
            dateOfBirthField.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).format(selectedTime.getTime()));
        });

        dateOfBirthPicker.show(getSupportFragmentManager(), "Date_Of_Birth_Picker");
    }
}
