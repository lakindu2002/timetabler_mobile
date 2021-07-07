package com.cb007787.timetabler.view.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.cb007787.timetabler.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.logging.Logger;


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

    public LoginFragment() {
        LOGGER = Logger.getLogger(LoginFragment.class.getName());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        } else {
            Snackbar validationError = Snackbar.make(theClickedButton, "Please Provide Valid Inputs", Snackbar.LENGTH_LONG);
            validationError.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
            validationError.show();
        }
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