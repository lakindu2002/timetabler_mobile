package com.cb007787.timetabler.view.system_admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.Role;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.model.User;
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
import java.util.Date;
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
    private List<Role> roleListFromDB;

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

        createUserBtn.setOnClickListener(v -> {
            //triggered when system admin clicks "Create User".
            handleCreateBtnClick();
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

            //check if date of birth greater than today.
            Calendar currentTime = Calendar.getInstance();
            //uses by default GMT 17:30 time zone
            currentTime.set(Calendar.HOUR_OF_DAY, 17);
            currentTime.set(Calendar.MINUTE, 30);
            currentTime.set(Calendar.MILLISECOND, 0);

            if (selectedTime.after(currentTime)) {
                constructError("Date of Birth Cannot Be After Today");
            } else {
                //format the time into Month/Date/Year and display on input box
                dateOfBirthField.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).format(selectedTime.getTime()));
                selectedDate = selectedTime; //set the selected time to global time
            }
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
                    roleListFromDB = roleList;
                    assignRolesToAdapter(roleListFromDB);
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage());
                    } catch (IOException e) {
                        constructError("We ran into an unexpected error while fetching user roles.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Role>> call, @NonNull Throwable t) {
                loadingBar.setVisibility(View.GONE);
                constructError("We ran into an unexpected error while fetching user roles.");
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

    private void handleCreateBtnClick() {
        loadingBar.setVisibility(View.VISIBLE);
        //retrieve the user inputs
        String username = usernameField.getText().toString();
        String firstName = firstNameField.getText().toString().trim();
        String lastName = lastNameField.getText().toString().trim();
        String emailAddress = emailAddressField.getText().toString().trim();
        String contactNumber = contactNumberField.getText().toString().trim();
        Date selectedDateInPicker = selectedDate == null ? null : selectedDate.getTime();
        Role userType = getUserRole(userRoleField.getText().toString());

        //construct a POJO for validation.
        User theNewUser = new User();
        theNewUser.setUsername(username);
        theNewUser.setFirstName(firstName);
        theNewUser.setLastName(lastName);
        theNewUser.setEmailAddress(emailAddress);
        theNewUser.setContactNumber(contactNumber);
        theNewUser.setDateOfBirth(selectedDateInPicker);
        theNewUser.setUserRole(userType);

        boolean isValid = validateUserCreate(theNewUser); //validate the input fields

        if (isValid) {
            //hit the endpoint and create the user
            createNewUser(theNewUser);
        } else {
            constructError("There are validation errors");
            loadingBar.setVisibility(View.GONE);
        }

    }

    private boolean validateUserCreate(User theNewUser) {
        boolean isUsernameValid = true;
        boolean isFirstNameValid = true;
        boolean isLastNameValid = true;
        boolean isEmailAddressValid = true;
        boolean isContactNumberValid = true;
        boolean isDateOfBirthValid = true;
        boolean isUserRoleValid = true;


        if (TextUtils.isEmpty(theNewUser.getUsername())) {
            usernameLayout.setError("Provide a Valid Username");
            isUsernameValid = false;
        } else if (theNewUser.getUsername().length() < 8) {
            usernameLayout.setError("Must be minimum 8 characters");
            isUsernameValid = false;
        } else {
            usernameLayout.setError(null);
        }

        if (TextUtils.isEmpty(theNewUser.getFirstName())) {
            firstNameLayout.setError("Provide a First Name");
            isFirstNameValid = false;
        } else {
            firstNameLayout.setError(null);
        }

        if (TextUtils.isEmpty(theNewUser.getLastName())) {
            lastNameLayout.setError("Provide a Last Name");
            isLastNameValid = false;
        } else {
            lastNameLayout.setError(null);
        }

        if (!theNewUser.getFirstName().matches("^[A-Za-z ]+")) {
            isFirstNameValid = false;
            firstNameLayout.setError("First Name Can Only Contain Text");
        } else {
            firstNameLayout.setError(null);
        }

        if (!theNewUser.getLastName().matches("^[A-Za-z ]+")) {
            isLastNameValid = false;
            lastNameLayout.setError("Last Name Can Only Contain Text");
        } else {
            lastNameLayout.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(theNewUser.getEmailAddress()).matches()) {
            emailAddressLayout.setError("Provide a Valid Email Address");
            isEmailAddressValid = false;
        } else {
            emailAddressLayout.setError(null);
        }

        if (TextUtils.isEmpty(theNewUser.getContactNumber())) {
            contactNumberLayout.setError("Provide a Valid Contact Number");
            isContactNumberValid = false;
        } else if (!TextUtils.isDigitsOnly(theNewUser.getContactNumber())) {
            contactNumberLayout.setError("Contact Number Must Be Digits Only");
            isContactNumberValid = false;
        } else if (theNewUser.getContactNumber().length() < 10) {
            contactNumberLayout.setError("Contact Number Must Be 10 Digits");
            isContactNumberValid = false;
        } else {
            contactNumberLayout.setError(null);
        }

        if (theNewUser.getDateOfBirth() == null) {
            dateOfBirthLayout.setError("Provide a Valid Date of Birth");
            isDateOfBirthValid = false;
        } else {
            dateOfBirthLayout.setError(null);
        }

        if (theNewUser.getUserRole() == null) {
            userRoleLayout.setError("Provide a Valid User Role");
            isUserRoleValid = false;
        } else {
            userRoleLayout.setError(null);
        }

        return isUsernameValid && isFirstNameValid && isLastNameValid && isEmailAddressValid && isContactNumberValid && isDateOfBirthValid && isUserRoleValid;
    }

    private void createNewUser(User theNewUser) {
        Call<SuccessResponseAPI> createUserCall = userService.createNewUser(theNewUser, token);

        createUserCall.enqueue(new Callback<SuccessResponseAPI>() {
            @Override
            public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                loadingBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), SystemAdminUserManagement.class));
                    finish();
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        constructError("We ran into an error while creating the user");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                loadingBar.setVisibility(View.GONE);
                constructError("We ran into an error while creating the user");
            }
        });
    }

    private Role getUserRole(String selectedRole) {
        //method will load the role for the selected role during create form
        for (Role role : roleListFromDB) {
            if (selectedRole.toLowerCase().trim().equalsIgnoreCase(role.getRoleName())) {
                return role;
            }
        }
        return null;
    }


    private void constructError(String errorMessage) {
        Snackbar theSnackBar = Snackbar.make(theToolbar, errorMessage, Snackbar.LENGTH_LONG);
        theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        View view = theSnackBar.getView();
        //retrieve the underling text view on the snack bar and increase the lines on it to display full message
        TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
        snackBarText.setMaxLines(5);
        theSnackBar.show();
    }
}
