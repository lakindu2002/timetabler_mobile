package com.cb007787.timetabler.view.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturnDTO;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.AuthService;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.logging.Logger;

import okhttp3.ResponseBody;
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

    private Logger LOGGER;

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
        this.loginButton = inflatedView.findViewById(R.id.login_button);

        //replace lambda with method expression
        this.loginButton.setOnClickListener(this::onLoginClicked);
    }

    public void onLoginClicked(View theClickedButton) {
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
                    handleOnResponse(response);
                }

                @Override
                public void onFailure(@NonNull Call<AuthReturnDTO> call, @NonNull Throwable t) {
                    //when the api runs into error with processing response or in network error
                    handleFailCommunication(t);
                }
            });
        } else {
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
    private void handleOnResponse(Response<AuthReturnDTO> authResponse) {
        if (authResponse.isSuccessful()) {
            //user has been successfully logged in
            LOGGER.info("LOGGED IN SUCCESSFULLY");
        } else {
            LOGGER.info("FAILED TO LOGIN");
            //error occurred during logging in
        }
    }

    /**
     * Callback executed when an exception occurs during login
     *
     * @param exception Exception send back by the server
     */
    private void handleFailCommunication(Throwable exception) {
        //requireView - return view that fragment is inflated on
        LOGGER.warning("EXCEPTION - " + exception.getMessage());
        Snackbar theSnackbar = Snackbar.make(requireView(), "An unexpected error occurred while logging in.", Snackbar.LENGTH_LONG);
        theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        theSnackbar.show();
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