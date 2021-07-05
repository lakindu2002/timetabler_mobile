package com.cb007787.timetabler.common;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.cb007787.timetabler.R;

public class CommonContainer extends AppCompatActivity {


    private FragmentContainerView fragmentContainerView;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);

        this.fragmentContainerView = findViewById(R.id.common_holder);
        this.fragmentManager = getSupportFragmentManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadLogin();
    }

    private void loadLogin() {
        this.fragmentManager.beginTransaction().replace(fragmentContainerView.getId(), new Login()).commit();
    }
}