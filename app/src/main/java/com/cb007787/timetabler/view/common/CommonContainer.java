package com.cb007787.timetabler.view.common;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.cb007787.timetabler.R;

/**
 * UI Activity designed to hold a fragment container that will hold the reset password and login UIs.
 *
 * @author Lakindu Hewawasam
 */
public class CommonContainer extends AppCompatActivity {


    private FragmentContainerView fragmentContainerView;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);

        this.fragmentContainerView = findViewById(R.id.common_holder);
        this.fragmentManager = getSupportFragmentManager();

        loadFragments();
    }

    private void loadFragments() {
        Intent intent = getIntent();
        if (intent != null) {
            //activity was loaded from an intent
            switch (intent.getStringExtra("loadingPage")) {
                case "LOGIN": {
                    loadLogin();
                    break;
                }
                case "RESET": {
                    loadReset();
                    break;
                }
                default: {
                    loadLogin();
                }
            }
        } else {
            //directly loaded, default to login
            loadLogin();
        }
    }

    private void loadReset() {
        this.fragmentManager.beginTransaction().replace(fragmentContainerView.getId(), new ResetPasswordFragment()).commit();
    }

    private void loadLogin() {
        this.fragmentManager.beginTransaction().replace(fragmentContainerView.getId(), new LoginFragment()).commit();
    }
}