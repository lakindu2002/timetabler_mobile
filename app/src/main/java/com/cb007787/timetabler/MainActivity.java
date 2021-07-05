package com.cb007787.timetabler;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.cb007787.timetabler.view.shared.SharedContainer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(() -> {
            //new runnable instance
            Intent theIntent = new Intent(MainActivity.this, SharedContainer.class);
            startActivity(theIntent);
            finish();
        }, 100000);
    }
}