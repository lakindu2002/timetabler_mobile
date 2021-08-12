package com.cb007787.timetabler.view.common.shared;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.model.User;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.service.UserService;
import com.cb007787.timetabler.view.academic_admin.AcademicAdminHome;
import com.cb007787.timetabler.view.lecturer.LecturerHome;
import com.cb007787.timetabler.view.student.StudentHome;
import com.cb007787.timetabler.view.system_admin.SystemAdminHome;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SharedUserProfile extends AppCompatActivity {

    private Toolbar toolbar;
    private AuthReturn loggedInUser;
    private User apiUser;
    private String jwtToken;
    private UserService theUserService;
    private ProgressBar loadingBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private MaterialTextView username;
    private MaterialTextView firstName;
    private MaterialTextView lastName;
    private MaterialTextView age;
    private MaterialTextView email;
    private EditText contact;
    private EditText firstPassword;
    private EditText secondPassword;
    private MaterialTextView memberSince;

    private Button updateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            loggedInUser = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
            jwtToken = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);
        } catch (JsonProcessingException e) {
            Log.i(SharedUserProfile.class.getName(), "Failed Parsing JSON");
        }

        if (loggedInUser.getRole().toLowerCase().equals("student") || loggedInUser.getRole().toLowerCase().equals("lecturer")) {
            setContentView(R.layout.activity_shared_profile);
        }

        //if token expired, will navigate to login
        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);

        getReferences();
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoggedInUserInformation();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserAccount(v);
            }
        });
    }

    /**
     * Callback executed when the update button is clicked
     *
     * @param v The button clicked
     */
    private void updateUserAccount(View v) {
        //updating only contact number and password, check if they are valid
        if (areUpdateInputsValid()) {
            loadingBar.setVisibility(View.VISIBLE);
            //inputs are valid, set the inputs into user object and update the user.
            apiUser.setContactNumber(contact.getText().toString());
            //set the password and re-entered password as well.
            apiUser.setPassword(firstPassword.getText().toString());
            apiUser.setReEnterPassword(secondPassword.getText().toString());

            //call update endpoint
            Call<SuccessResponseAPI> updateAPICall = theUserService.updateUserAccount(jwtToken, apiUser);
            updateAPICall.enqueue(new Callback<SuccessResponseAPI>() {
                @Override
                public void onResponse(Call<SuccessResponseAPI> call, Response<SuccessResponseAPI> response) {
                    try {
                        handleOnUserUpdate(response);
                    } catch (IOException e) {
                        Log.i(SharedUserProfile.class.getName(), "Failed Parsing JSON");
                        loadingBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<SuccessResponseAPI> call, Throwable t) {
                    handleOnFailure(t);
                }
            });
        }
    }

    /**
     * Callback executed when the API returned a response back to the client
     *
     * @param response The response sent from the API
     */
    private void handleOnUserUpdate(Response<SuccessResponseAPI> response) throws IOException {
        Snackbar theSnackBar = null;
        if (response.isSuccessful()) {
            //update occurred successfully
            loadingBar.setVisibility(View.GONE);
            SuccessResponseAPI theSuccess = response.body();
            theSnackBar = Snackbar.make(loadingBar, theSuccess.getMessage(), Snackbar.LENGTH_LONG);
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_success, null));
            //clear inputs after success password update
            firstPassword.setText("");
            secondPassword.setText("");

            getLoggedInUserInformation(); //get the logged in user information
        } else {
            //error occurred
            loadingBar.setVisibility(View.GONE);
            ErrorResponseAPI theError = APIConfigurer.getTheErrorReturned(response.errorBody());
            theSnackBar = Snackbar.make(loadingBar, theError.getErrorMessage(), Snackbar.LENGTH_LONG);
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        }
        theSnackBar.show();
    }

    /**
     * Method will check if the contact number, first password and second password is valid.
     *
     * @return Boolean to indicate if validate or not
     */
    private boolean areUpdateInputsValid() {
        boolean contactValid = true;
        boolean passwordValid = true;

        if (TextUtils.isEmpty(contact.getText().toString())) {
            contact.setError("Please provide a contact number");
            contactValid = false;
        } else if (!contact.getText().toString().matches("^[0-9]+$")) {
            contact.setError("Please provide only digits in the contact number");
            contactValid = false;
        } else if (!(contact.getText().toString().length() == 10)) {
            contact.setError("Please provide 10 digits for the contact number");
            contactValid = false;
        }

        //validate password
        if (!TextUtils.isEmpty(firstPassword.getText().toString()) || !TextUtils.isEmpty(secondPassword.getText().toString())) {
            //user provided a password since either field is not empty, validate it

            //password match
            if (!firstPassword.getText().toString().equals(secondPassword.getText().toString())) {
                firstPassword.setError("Passwords do not match");
                secondPassword.setError("Passwords do not match");
                passwordValid = false;
                //password is not valid as they are not the same
            }

            //either password between 8 or 30 characters
            if ((firstPassword.getText().toString().length() < 8) || firstPassword.getText().toString().length() > 30) {
                firstPassword.setError("Passwords must be between 8 and 30 characters");
                passwordValid = false;
            }

            if ((secondPassword.getText().toString().length() < 8) || secondPassword.getText().toString().length() > 30) {
                secondPassword.setError("Passwords must be between 8 and 30 characters");
                passwordValid = false;
            }
        }

        return contactValid && passwordValid;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLoggedInUserInformation();
    }

    private void getLoggedInUserInformation() {
        loadingBar.setVisibility(View.VISIBLE);

        Call<User> userInformation = theUserService.getUserInformation(jwtToken, loggedInUser.getUsername());
        userInformation.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                //the response given by rest api
                try {
                    handleOnResponse(response);
                    stopRefreshing();
                } catch (IOException e) {
                    Log.e(SharedUserProfile.class.getName(), "FAILED PARSING JSON ON API CALL BACK");
                    stopRefreshing();
                    loadingBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                handleOnFailure(t);
                stopRefreshing();
                //in case of network error or response processing error
            }
        }); //execute the operation asynchronously because http methods needs to occur on secondary thread
    }

    private void handleOnFailure(Throwable t) {
        constructError("An unexpected error occurred while processing your request.");
        Log.e(SharedUserProfile.class.getName(), t.getLocalizedMessage());
        loadingBar.setVisibility(View.GONE);
    }

    private void handleOnResponse(Response<User> response) throws IOException {
        if (response.isSuccessful()) {
            //returned response code is 200 to 300
            apiUser = response.body(); //the returned success body. API only returns success with HTTP OK (200)
            showInView();
        } else {
            if (response.code() == 404) {
                //endpoint not found
                constructError("The resource you are trying to access does not exist");
            } else {
                //returned an error
                ErrorResponseAPI errorResponseAPI = APIConfigurer.getTheErrorReturned(response.errorBody());
                constructError(errorResponseAPI.getErrorMessage());
            }
        }
        loadingBar.setVisibility(View.GONE);
    }

    /**
     * Method will be executed to update the UI elements after the success response has been obtained from the API.
     */
    private void showInView() {
        username.setText(apiUser.getUsername());
        firstName.setText(apiUser.getFirstName());
        lastName.setText(apiUser.getLastName());
        age.setText(String.format(Locale.ENGLISH, "%s (%d)", new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(apiUser.getDateOfBirth()), apiUser.getAge()));
        email.setText(apiUser.getEmailAddress());
        contact.setText(apiUser.getContactNumber());
        memberSince.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(apiUser.getMemberSince()));
    }

    private void constructError(String errorMessage) {
        Snackbar theSnackBar = Snackbar.make(toolbar, errorMessage, Snackbar.LENGTH_LONG);
        theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        theSnackBar.show();
    }

    private void stopRefreshing() {
        if (swipeRefreshLayout.isRefreshing()) {
            //the swipe refresh is active, stop it
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void getReferences() {
        toolbar = findViewById(R.id.profile_toolbar_profile);
        //retrieve an instance of the HTTP compiled user service using retrofit
        theUserService = APIConfigurer.getApiConfigurer().getUserService();
        loadingBar = findViewById(R.id.progress_bar);
        swipeRefreshLayout = findViewById(R.id.swiper);

        //retrieve references to UI text fields.
        username = findViewById(R.id.user_name_profile);
        firstName = findViewById(R.id.first_name_profile);
        lastName = findViewById(R.id.last_name_profile_label);
        age = findViewById(R.id.age_profile_label);
        email = findViewById(R.id.email_profile_label);
        contact = findViewById(R.id.contact_profile_label);
        memberSince = findViewById(R.id.membersince_profile_label);
        firstPassword = findViewById(R.id.password_01_label);
        secondPassword = findViewById(R.id.password_02_label);

        updateBtn = findViewById(R.id.update_account);

    }

    /**
     * Method listen to click events on the toolbar
     *
     * @param item The item clicked
     * @return The select status
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //clicked the home button for android - display home as up
            //navigate to respective dashboards for each role
            Intent homeIntent = null;
            switch (loggedInUser.getRole().toLowerCase().trim()) {
                case "student": {
                    homeIntent = new Intent(this, StudentHome.class);
                    startActivity(homeIntent);
                    break;
                }
                case "system administrator": {
                    homeIntent = new Intent(this, SystemAdminHome.class);
                    startActivity(homeIntent);
                    break;
                }
                case "lecturer": {
                    homeIntent = new Intent(this, LecturerHome.class);
                    startActivity(homeIntent);
                    break;
                }
                case "academic admin": {
                    homeIntent = new Intent(this, AcademicAdminHome.class);
                    startActivity(homeIntent);
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //event raised when the user clicks back using phone back button
        Intent homeIntent = null;
        switch (loggedInUser.getRole().toLowerCase().trim()) {
            case "student": {
                homeIntent = new Intent(this, StudentHome.class);
                startActivity(homeIntent);
                break;
            }
            case "system administrator": {
                homeIntent = new Intent(this, SystemAdminHome.class);
                startActivity(homeIntent);
                break;
            }
            case "lecturer": {
                homeIntent = new Intent(this, LecturerHome.class);
                startActivity(homeIntent);
                break;
            }
            case "academic admin": {
                homeIntent = new Intent(this, AcademicAdminHome.class);
                startActivity(homeIntent);
                break;
            }
            default: {
                super.onBackPressed();
            }
        }
    }

}