package com.cb007787.timetabler.view.system_admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.Role;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.service.UserRoleService;
import com.cb007787.timetabler.service.UserService;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private String token;
    private UserRoleService userRoleService;
    private UserService userService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_admin_create_user);
        getReferences();
        dateOfBirthField.setEnabled(false); //disable the date of birth to prevent type

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        this.token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);

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
        this.userRoleService = APIConfigurer.getApiConfigurer().getUserRoleService();
        this.userService = APIConfigurer.getApiConfigurer().getUserService();
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

    @Override
    protected void onStart() {
        super.onStart();
        getSystemRolesFromDatabase();
    }

    private void getSystemRolesFromDatabase() {
        //retrieve roles except system admin for account creation
        loadingBar.setVisibility(View.VISIBLE);
        Call<List<Role>> allCall = userRoleService.getAllRolesWithoutSystemAdmin(token);
        allCall.enqueue(new Callback<List<Role>>() {
            @Override
            public void onResponse(@NonNull Call<List<Role>> call, @NonNull Response<List<Role>> response) {
                loadingBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    List<Role> roleList = response.body();
                    assignRolesToAdapter(roleList);
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage());
                    } catch (IOException e) {
                        constructError("We ran into an unexpected error while processing your request.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Role>> call, @NonNull Throwable t) {
                loadingBar.setVisibility(View.GONE);
                constructError("We ran into an unexpected error while processing your request.");
            }
        });
    }

    private void assignRolesToAdapter(List<Role> roleList) {
        //create a array adapter that will drop down to select from
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, R.layout.role_array_adapter);
        for (Role eachRole : roleList) {
            roleAdapter.add(eachRole.getRoleName());
        }
        userRoleField.setAdapter(roleAdapter); //ensure that the role list will create the drop down on autocomplete view.
    }

    private void constructError(String errorMessage) {
        Snackbar theSnackBar = Snackbar.make(theToolbar, errorMessage, Snackbar.LENGTH_LONG);
        theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        theSnackBar.show();
    }
}
