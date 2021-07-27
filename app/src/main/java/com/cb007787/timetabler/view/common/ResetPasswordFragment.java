package com.cb007787.timetabler.view.common;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.PasswordReset;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.AuthService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Class used to handle the reset password layout and business logic
 *
 * @author Lakindu Hewawasam
 */
public class ResetPasswordFragment extends Fragment {

    private TextInputLayout firstPasswordLayout;
    private TextInputEditText firstPassword;
    private TextInputLayout secondPasswordLayout;
    private TextInputEditText secondPassword;
    private ProgressBar resetSpinner;
    private Button resetPasswordButton;

    private AuthService authService;
    private PasswordReset passwordResetInformation;

    public ResetPasswordFragment() {
        this.authService = APIConfigurer.getApiConfigurer().getAuthService();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        //hook executed after fragment lifecycle has started
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View theInflatedView = inflater.inflate(R.layout.fragment_reset_password, container, false);
        getReferences(theInflatedView);

        //retrieve the saved reset username and reset token from the shared preference

        //de-serialize the JSON string back to the password reset class using object mapper.
        try {
            this.passwordResetInformation = SharedPreferenceService.getResetInformation(
                    requireContext(), PreferenceInformation.PREFERENCE_NAME
            );
        } catch (JsonProcessingException e) {
            System.out.println("ERROR PARSING JSON");
        }

        return theInflatedView;
    }

    private void getReferences(View theInflatedView) {
        this.firstPasswordLayout = theInflatedView.findViewById(R.id.first_pw_reset);
        this.firstPassword = theInflatedView.findViewById(R.id.first_pw);
        this.secondPasswordLayout = theInflatedView.findViewById(R.id.second_pw_reset);
        this.secondPassword = theInflatedView.findViewById(R.id.second_pw);
        this.resetPasswordButton = theInflatedView.findViewById(R.id.reset_pw_btn);
        this.resetSpinner = theInflatedView.findViewById(R.id.reset_spinner);

        //by the use of :: it invokes the handleResetClicked method.
        //JVM assumes it is an implementation of the View.OnClickListener.
        //as the View object is passed into it.
        this.resetPasswordButton.setOnClickListener(this::handleResetClicked);
    }

    /**
     * Handles the onclick of the Reset Password Button
     *
     * @param theClickedButton The button clicked.
     */
    public void handleResetClicked(View theClickedButton) {
        /*
        Generally the View passed into the snackbar can be the view that was interacted with to trigger
        the snackbar, such as a button that was clicked, or a card that was swiped.
         */
        this.resetSpinner.setVisibility(View.VISIBLE);
        if (isPasswordValid()) {
            //entered passwords are valid
            HashMap<String, String> authRequest = new HashMap<>();
            authRequest.put("username", passwordResetInformation.getUsernameNeedingReset());
            authRequest.put("password", firstPassword.getText().toString());

            this.authService.resetDefaultPassword(authRequest, SharedPreferenceService.getToken(requireContext(), PreferenceInformation.PREFERENCE_NAME)).enqueue(new Callback<SuccessResponseAPI>() {
                @Override
                public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                    //handle response from api
                    try {
                        handleOnResponse(response);
                    } catch (IOException e) {
                        System.out.println("ERROR PARSING ERROR JSON BODY");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                    //handle failure to send request or process response
                    handleFailCommunication(t);
                }
            });
        } else {
            this.resetSpinner.setVisibility(View.GONE);
        }
    }

    private void handleOnResponse(Response<SuccessResponseAPI> resetResponse) throws IOException {
        if (resetResponse.isSuccessful()) {
            Snackbar theSnackbar = Snackbar.make(requireView(), resetResponse.body().getMessage(), Snackbar.LENGTH_LONG);
            theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_success, null));
            theSnackbar.show();

            //since password has been resetted successfully, clear the shared preferences again so that user can re-login
            SharedPreferenceService.clearSharedPreferences(requireContext(), PreferenceInformation.PREFERENCE_NAME);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.common_holder, new LoginFragment()).commit();

        } else {
            ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(resetResponse.errorBody());
            Snackbar theSnackbar = Snackbar.make(requireView(), theErrorReturned.getErrorMessage(), Snackbar.LENGTH_LONG);
            theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
            theSnackbar.show();
        }
        this.resetSpinner.setVisibility(View.GONE);
    }

    private void handleFailCommunication(Throwable exception) {
        //requireView - return view that fragment is inflated on
        Snackbar theSnackbar = Snackbar.make(requireView(), "An unexpected error occurred while resetting your password", Snackbar.LENGTH_LONG);
        theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        theSnackbar.show();
        this.resetSpinner.setVisibility(View.GONE);
    }

    private boolean isPasswordValid() {
        boolean isPasswordValid = false;

        if (firstPassword.getText().toString().length() == 0) {
            firstPasswordLayout.setError("Password Cannot Be Empty");
        } else {
            isPasswordValid = true;
            firstPasswordLayout.setError(null);
        }

        if (secondPassword.getText().toString().length() == 0) {
            isPasswordValid = false;
            secondPasswordLayout.setError("Password Cannot Be Empty");
        } else {
            isPasswordValid = true;
            secondPasswordLayout.setError(null);
        }

        if (firstPassword.getText().toString().length() >= 8 && secondPassword.getText().toString().length() >= 8) {
            if (secondPassword.getText().toString().equals(firstPassword.getText().toString())) {
                isPasswordValid = true;
                secondPasswordLayout.setError(null);
                firstPasswordLayout.setError(null);
            } else {
                isPasswordValid = false;
                firstPasswordLayout.setError("Passwords Do Not Match");
                secondPasswordLayout.setError("Passwords Do Not Match");
            }
        } else {
            Snackbar validationError = Snackbar.make(requireView(), "Passwords Should Be Greater Than 8 Characters", Snackbar.LENGTH_LONG);
            validationError.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
            validationError.show();
            isPasswordValid = false;
        }
        return isPasswordValid;
    }
}