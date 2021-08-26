package com.cb007787.timetabler.view.student;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.interfaces.UpdateCallbacks;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.model.Task;
import com.cb007787.timetabler.provider.TaskContentProvider;
import com.cb007787.timetabler.provider.TaskDbHelper;
import com.cb007787.timetabler.recyclers.TaskRecycler;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

        adapter.setDeleteCallbacks(new DeleteCallbacks() {
            @Override
            public void onDeleteSuccessResponse(SuccessResponseAPI theSuccessObject) {
                linearProgressIndicator.setVisibility(View.GONE);
                Snackbar theSnackBar = Snackbar.make(toolbar, "The task was removed successfully", Snackbar.LENGTH_LONG);
                theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_success, null));
                View view = theSnackBar.getView();
                //retrieve the underling text view on the snack bar and increase the lines on it to display full message
                TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
                snackBarText.setMaxLines(5);
                theSnackBar.show();
                loadTasks();
            }

            @Override
            public void onDeleteFailure(String message) {
                linearProgressIndicator.setVisibility(View.GONE);
                constructError(message, false);
            }

            @Override
            public void onDeleteCalled() {
                linearProgressIndicator.setVisibility(View.VISIBLE);
            }
        });

        adapter.setUpdateCallbacks(new UpdateCallbacks() {
            @Override
            public void onUpdate() {
                linearProgressIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onUpdateCompleted(SuccessResponseAPI theResponse) {
                linearProgressIndicator.setVisibility(View.GONE);
                Snackbar theSnackBar = Snackbar.make(toolbar, "The task was marked as complete successfully", Snackbar.LENGTH_LONG);
                theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_success, null));
                View view = theSnackBar.getView();
                //retrieve the underling text view on the snack bar and increase the lines on it to display full message
                TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
                snackBarText.setMaxLines(5);
                theSnackBar.show();
                loadTasks();
            }

            @Override
            public void onUpdateFailed(String errorMessage) {
                linearProgressIndicator.setVisibility(View.GONE);
                constructError(errorMessage, false);
            }
        });

        taskRecycler.setLayoutManager(new LinearLayoutManager(this));
        taskRecycler.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(this::loadTasks);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search, menu); //inflate search menu
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint("Provide Task Name");
        searchView.setBackgroundColor(ActivityCompat.getColor(this, R.color.white));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTask(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTask(newText);
                return true;
            }
        });
        return true;
    }

    private void searchTask(String key) {
        key = key.trim().toLowerCase();
        if (key.length() == 0) {
            //no search value
            adapter.setTheTasks(taskList);
        } else {
            //filter
            String finalKey = key;
            List<Task> filteredTask =
                    taskList.stream()
                            .filter((eachTask) -> eachTask.getTaskName().trim().toLowerCase().contains(finalKey))
                            .collect(Collectors.toList());

            if (filteredTask.size() == 0) {
                constructError("There are no tasks for the provided name", true);
            }

            adapter.setTheTasks(filteredTask);
        }
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
                    TaskDbHelper.TASK_CREATED_AT,
                    TaskDbHelper.TASK_DESCRIPTION //used for calendar event
            }; //required columns from the query.

            Cursor query = contentResolver.query(TaskContentProvider.PERFORM_ALL_URI, requiredColumns, TaskDbHelper.USERNAME + "=" + "'" + loggedInUser.getUsername() + "'", null, "ASC");
            boolean movedToFirst = query.moveToFirst(); //bring the cursor from the top to the first row.
            if (movedToFirst) { //if first row exists.
                while (!query.isAfterLast()) {
                    //loop till at the last row.
                    int taskId = query.getInt(query.getColumnIndex(TaskDbHelper.TASK_ID));
                    String taskName = query.getString(query.getColumnIndex(TaskDbHelper.TASK_NAME));
                    long startDate = query.getLong(query.getColumnIndex(TaskDbHelper.START_DATE));
                    long dueDate = query.getLong(query.getColumnIndex(TaskDbHelper.DUE_DATE));
                    String taskStatus = query.getString(query.getColumnIndex(TaskDbHelper.TASK_STATUS));
                    long createdAy = query.getLong(query.getColumnIndex(TaskDbHelper.TASK_CREATED_AT));
                    String description = query.getString(query.getColumnIndex(TaskDbHelper.TASK_DESCRIPTION));

                    Task eachTask = new Task();
                    eachTask.set_ID(taskId);
                    eachTask.setDueDateInMs(dueDate);
                    eachTask.setTaskName(taskName);
                    eachTask.setStartDateInMs(startDate);
                    eachTask.setTaskStatus(taskStatus);
                    eachTask.setTaskCreatedAtInMs(createdAy);
                    eachTask.setTaskDescription(description);

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
        if (taskList.size() == 0) {
            constructError("You have no tasks. Create some tasks for them to be visible", true);
        }
    }

    private void constructError(String errorMessage, boolean isInfo) {
        Snackbar theSnackBar = Snackbar.make(toolbar, errorMessage, Snackbar.LENGTH_LONG);
        if (isInfo) {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        } else {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        }
        View view = theSnackBar.getView();
        //retrieve the underling text view on the snack bar and increase the lines on it to display full message
        TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
        snackBarText.setMaxLines(5);
        theSnackBar.show();
    }
}