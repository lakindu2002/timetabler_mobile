package com.cb007787.timetabler.view.student;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.Task;
import com.cb007787.timetabler.provider.TaskContentProvider;
import com.cb007787.timetabler.provider.TaskDbHelper;
import com.cb007787.timetabler.recyclers.TaskRecycler;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StudentTaskManagement extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView taskRecycler;
    private AuthReturn loggedInUser;
    private FloatingActionButton floatingActionButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearProgressIndicator linearProgressIndicator;
    private ContentResolver contentResolver;
    private List<Task> taskList = new ArrayList<>();
    private TaskRecycler adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_task_management);

        SharedPreferenceService.validateToken(this, PreferenceInformation.PREFERENCE_NAME);

        getReferences();

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //create back button
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_home_24);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        floatingActionButton.setOnClickListener(v -> {
            Intent navigation = new Intent(this, StudentTaskCreateUpdate.class);
            navigation.putExtra("isUpdate", false);
            startActivity(navigation);
        });

        adapter = new TaskRecycler(this);
        adapter.setTheTasks(taskList);

        taskRecycler.setLayoutManager(new LinearLayoutManager(this));
        taskRecycler.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(this::loadTasks);
    }

    private void getReferences() {
        toolbar = findViewById(R.id.tasks_toolbar);
        contentResolver = getContentResolver();
        try {
            loggedInUser = SharedPreferenceService.getLoggedInUser(this, PreferenceInformation.PREFERENCE_NAME);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        floatingActionButton = findViewById(R.id.floating_action_button);
        swipeRefreshLayout = findViewById(R.id.swiper);
        linearProgressIndicator = findViewById(R.id.progress_bar);
        taskRecycler = findViewById(R.id.recycler);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadTasks();
    }

    private void loadTasks() {
        if (loggedInUser != null) {
            taskList.clear();
            String[] requiredColumns = {
                    TaskDbHelper.TASK_ID,
                    TaskDbHelper.TASK_NAME,
                    TaskDbHelper.START_DATE,
                    TaskDbHelper.DUE_DATE,
                    TaskDbHelper.TASK_STATUS,
                    TaskDbHelper.TASK_CREATED_AT
            }; //required columns from the query.

            Cursor query = contentResolver.query(TaskContentProvider.PERFORM_ALL_URI, requiredColumns, TaskDbHelper.USERNAME + "=" + "'" + loggedInUser.getUsername() + "'", null, "ASC");
            boolean movedToFirst = query.moveToFirst();
            if (movedToFirst) {
                while (!query.isAfterLast()) {
                    //loop till at the last row.
                    int taskId = query.getInt(query.getColumnIndex(TaskDbHelper.TASK_ID));
                    String taskName = query.getString(query.getColumnIndex(TaskDbHelper.TASK_NAME));
                    long startDate = query.getLong(query.getColumnIndex(TaskDbHelper.START_DATE));
                    long dueDate = query.getLong(query.getColumnIndex(TaskDbHelper.DUE_DATE));
                    String taskStatus = query.getString(query.getColumnIndex(TaskDbHelper.TASK_STATUS));
                    long createdAy = query.getLong(query.getColumnIndex(TaskDbHelper.TASK_CREATED_AT));

                    Task eachTask = new Task();
                    eachTask.set_ID(taskId);
                    eachTask.setDueDateInMs(dueDate);
                    eachTask.setTaskName(taskName);
                    eachTask.setStartDateInMs(startDate);
                    eachTask.setTaskStatus(taskStatus);
                    eachTask.setTaskCreatedAtInMs(createdAy);

                    taskList.add(eachTask);
                    query.moveToNext(); //move to the next row.
                }
            }
            query.close();
            adapter.setTheTasks(taskList);
        }
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}