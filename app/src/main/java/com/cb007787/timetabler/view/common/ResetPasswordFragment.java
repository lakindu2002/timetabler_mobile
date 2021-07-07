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

    private Button resetPasswordButton;

    public ResetPasswordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        //hook executed after fragment lifecycle has started
        super.onStart();
        constructInitialSnackbar();
    }

    private void constructInitialSnackbar() {
        //requireView - Get the root view for the fragment's layout (the one returned by onCreateView).
        Snackbar theSnackbar = Snackbar.make(requireView(), getString(R.string.reset_info_text), Snackbar.LENGTH_INDEFINITE);
        theSnackbar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        //add a dismiss button
        theSnackbar.setActionTextColor(getResources().getColor(R.color.snackbar_action, null));
        theSnackbar.setAction("Dismiss", v -> theSnackbar.dismiss());
        theSnackbar.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View theInflatedView = inflater.inflate(R.layout.fragment_reset_password, container, false);
        getReferences(theInflatedView);

        return theInflatedView;
    }

    private void getReferences(View theInflatedView) {
        this.firstPasswordLayout = theInflatedView.findViewById(R.id.first_pw_reset);
        this.firstPassword = theInflatedView.findViewById(R.id.first_pw);
        this.secondPasswordLayout = theInflatedView.findViewById(R.id.second_pw_reset);
        this.secondPassword = theInflatedView.findViewById(R.id.second_pw);
        this.resetPasswordButton = theInflatedView.findViewById(R.id.reset_pw_btn);

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
    }
}