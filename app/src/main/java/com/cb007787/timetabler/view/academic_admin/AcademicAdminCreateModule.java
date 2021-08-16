package com.cb007787.timetabler.view.academic_admin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.os.Bundle;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.BatchService;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;

public class AcademicAdminCreateModule extends AppCompatActivity {

    private Toolbar toolbar;

    private ModuleService moduleService;
    private BatchService batchService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_admin_create_module);
        getReferences();

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        token = SharedPreferenceService.getToken(this, PreferenceInformation.PREFERENCE_NAME);

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //add back button
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(ActivityCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24));
        }
    }

    private void getReferences() {
        toolbar = findViewById(R.id.toolbar);
        moduleService = APIConfigurer.getApiConfigurer().getModuleService();
        batchService = APIConfigurer.getApiConfigurer().getBatchService();
    }
}