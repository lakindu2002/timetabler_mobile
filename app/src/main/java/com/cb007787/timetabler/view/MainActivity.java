package com.cb007787.timetabler.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.view.common.CommonContainer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, CommonContainer.class));
            finish();
        }, 1000);
    }
}