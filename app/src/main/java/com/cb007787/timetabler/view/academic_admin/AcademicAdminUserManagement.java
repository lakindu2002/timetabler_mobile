package com.cb007787.timetabler.view.academic_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.common.shared.UserLoadingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class AcademicAdminUserManagement extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private UserLoadingFragment loader = null;
    private BottomNavigationView navigationView;
    private String loadingPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_admin_user_management);
        getReferences();

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //assign back button
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null) {
            //check if user is coming from a card
            if (intent.getStringExtra("loading") != null) {
                //coming from check, load the required card user type.
                loadingPage = intent.getStringExtra("loading");
            }
        }

        navigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void getReferences() {
        toolbar = findViewById(R.id.the_toolbar);
        navigationView = findViewById(R.id.bottom_navigation);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //default to loading lecturer
        navigationView.setSelectedItemId(R.id.user_lecturer_admin);
        //utilize common UserLoadingFragment

        if (loadingPage != null) {
            if (loadingPage.equalsIgnoreCase("student")) {
                navigationView.setSelectedItemId(R.id.user_student_admin);
            } else if (loadingPage.equalsIgnoreCase("lecturer")) {
                navigationView.setSelectedItemId(R.id.user_lecturer_admin);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint("Provide Username");
        searchView.setBackgroundColor(getResources().getColor(R.color.white, null));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loader.search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loader.search(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.user_lecturer_admin) {
            //view lecturer
            loader = UserLoadingFragment.newInstance("lecturer");
        } else if (item.getItemId() == R.id.user_student_admin) {
            //view  student
            loader = UserLoadingFragment.newInstance("student");
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, loader).commit();
        return true;
    }
}