package com.cb007787.timetabler.view.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturnDTO;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.PasswordReset;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.AuthService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.academic_admin.AcademicAdminHome;
import com.cb007787.timetabler.view.lecturer.LecturerHome;
import com.cb007787.timetabler.view.student.StudentHome;
import com.cb007787.timetabler.view.system_admin.SystemAdminHome;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Fragment used to handle the Login UI and Logic.
 *
 * @author Lakindu Hewawasam
 */
public class LoginFragment extends Fragment {

    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText usernameField;
    private TextInputEditText passwordField;
    private Button loginButton;
    private ProgressBar loadingSpinner;

    private final Logger LOGGER;

    private AuthService authService;

    public LoginFragment() {
        LOGGER = Logger.getLogger(LoginFragment.class.getName());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.authService = APIConfigurer.getApiConfigurer().getAuthService();
        LOGGER.info("INITIALIZED AUTH API");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_login, container, false);
        getReferences(inflatedView);
        return inflatedView;
    }

    private void getReferences(View inflatedView) {
        this.usernameLayout = inflatedView.findViewById(R.id.username_field_layout);
        this.passwordLayout = inflatedView.findViewById(R.id.password_field_layout);
        this.usernameField = inflatedView.findViewById(R.id.login_username_field);
        this.passwordField = inflatedView.findViewById(R.id.login_password_field);
        this.loadingSpinner = inflatedView.findViewById(R.id.login_progress);
        this.loginButton = inflatedView.findViewById(R.id.login_button);

        //replace lambda with method expression
        this.loginButton.setOnClickListener(this::onLoginClicked);
    }

    public void onLoginClicked(View theClickedButton) {
        this.loadingSpinner.setVisibility(View.VISIBLE);

        if (areInputsValid()) {
            //proceed with login
            HashMap<String, String> theLoginRequest = new HashMap<String, String>();
            theLoginRequest.put("username", usernameField.getText().toString());
            theLoginRequest.put("password", passwordField.getText().toString());
            //create an instance of the login request

            this.authService.login(theLoginRequest).enqueue(new Callback<AuthReturnDTO>() {
                @Override
                public void onResponse(@NonNull Call<AuthReturnDTO> call, @NonNull Response<AuthReturnDTO> response) {
                    //when the api sends a response - pass or fail
                    try {
                        handleOnResponse(response);
                    } catch (IOException e) {
                        LOGGER.warning("FAILURE DURING ERROR PARSING ON LOGIN");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AuthReturnDTO> call, @NonNull Throwable t) {
                    //when the api runs into error with processing response or in network error
                    handleFailCommunication(t);
                }
            });
        } else {
            this.loadingSpinner.setVisibility(View.GONE);
            Snackbar validationError = Snackbar.make(theClickedButton, "Please Provide Valid Inputs", Snackbar.LENGTH_LONG);
            validationError.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
            validationError.show();
        }
    }

    /**
     * Method executed when the endpoint returns a response - success or fail
     *
     * @param authResponse The return from the api
     */
    private void handleOnResponse(Response<AuthReturnDTO> authResponse) throws IOException {
        if (authResponse.isSuccessful()) {
            //user has been successfully logged in
            AuthReturnDTO authReturnDTO = authResponse.body();

            //add the success auth information to the shared preferences.
            SharedPreferenceService.setLoginSuccessVariables(
                    requireContext(), authReturnDTO, authResponse.headers().get("Authorization"), PreferenceInformation.PREFERENCE_NAME
            );

            //navigate based on role
            navigateBasedOnRole(authReturnDTO);
        } else {
            //error occurred during logging in
            if (authResponse.code() == 423) {
                //account is locked, requires default password reset.
                //navigate to reset password page.
                PasswordReset passwordResetObject = APIConfigurer.getPasswordResetObject(authResponse.errorBody());
                //attach the reset information onto the shared preferences so that it can be accessed from the fragment.
                //attach the reset password JWT Token to the shared preferences

                SharedPreferenceService.setResetUserInformation(
                        requireContext(), passwordResetObject, authResponse.headers().get("Authorization"), PreferenceInformation.PREFERENCE_NAME
                );

                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.common_holder, new ResetPasswordFragment()).commit();
                Snackbar theSnackbar = Snackbar.make(requireView(), "You need to reset your default password before logging in.", Snackbar.LENGTH_LONG);
                theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
                theSnackbar.show();
            } else {
                //other error.
                ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(authResponse.errorBody());
                Snackbar theSnackbar = Snackbar.make(requireView(), theErrorReturned.getErrorMessage(), Snackbar.LENGTH_LONG);
                theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
                theSnackbar.show();
            }
        }
        this.loadingSpinner.setVisibility(View.GONE);
    }

    private void navigateBasedOnRole(AuthReturnDTO authReturnDTO) {
        //access the user role and navigate to the required directory based on the role.
        Intent theRoleIntent = null;

        switch (authReturnDTO.getRole().toLowerCase().trim()) {
            case "academic administrator": {
                theRoleIntent = new Intent(requireContext(), AcademicAdminHome.class);
                break;
            }
            case "system administrator": {
                theRoleIntent = new Intent(requireContext(), SystemAdminHome.class);
                break;
            }
            case "lecturer": {
                theRoleIntent = new Intent(requireContext(), LecturerHome.class);
                break;
            }
            case "student": {
                theRoleIntent = new Intent(requireContext(), StudentHome.class);
                break;
            }
        }
        startActivity(theRoleIntent);
        requireActivity().finish(); //remove the splash screen from activity back trace.
    }

    /**
     * Callback executed when an exception occurs during login
     *
     * @param exception Exception send back by the server
     */
    private void handleFailCommunication(Throwable exception) {
        //requireView - return view that fragment is inflated on
        Snackbar theSnackbar = Snackbar.make(requireView(), "An unexpected error occurred while logging in.", Snackbar.LENGTH_LONG);
        theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        theSnackbar.show();
        this.loadingSpinner.setVisibility(View.GONE);
    }

    private boolean areInputsValid() {
        boolean isPasswordValid = false;
        boolean isUsernameValid = false;

        if (passwordField.getText().toString().length() == 0) {
            this.passwordLayout.setError("Password Cannot Be Empty");
        } else {
            this.passwordLayout.setError(null);
            isPasswordValid = true;
        }

        if (usernameField.getText().toString().length() == 0) {
            this.usernameLayout.setError("Username Cannot Be Empty");
        } else {
            this.usernameLayout.setError(null);
            isUsernameValid = true;
        }
        return isUsernameValid && isPasswordValid;
    }
}