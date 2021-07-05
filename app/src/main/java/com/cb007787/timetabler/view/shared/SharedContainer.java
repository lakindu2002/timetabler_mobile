package com.cb007787.timetabler.view.shared;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.cb007787.timetabler.R;

public class SharedContainer extends AppCompatActivity {

    private FragmentContainerView fragmentHolder;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_container);

        getReferences();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showLoginFragment();
    }

    private void showLoginFragment() {
        this.fragmentManager.beginTransaction().replace(fragmentHolder.getId(), new Login()).commit();
    }

    private void getReferences() {
        this.fragmentHolder = findViewById(R.id.fragment_holder);
        this.fragmentManager = getSupportFragmentManager();
    }
}