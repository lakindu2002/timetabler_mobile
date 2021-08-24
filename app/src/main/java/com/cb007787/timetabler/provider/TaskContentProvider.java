package com.cb007787.timetabler.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;

/**
 * Class will act as the interface that external apps use to communicate with the content provided - SQLite Task Database.
 */
public class TaskContentProvider extends ContentProvider {
    //do not create a match for the root URI
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //content uri identifies the data in a provider.
    private static final String AUTHORITY = "com.cb007787.timetabler.provider"; //name of the provider (single authority).
    private static final String TASK_TABLE = "task";

    private static final String ALL_TASKS = "all";
    private static final String NEW = "create";
    private static final String COMPLETED_TASKS = "completed";
    private static final String PENDING = "pending";
    private static final String ONE = "one";

    //uris to be called from the client.
    public static final Uri PERFORM_ALL_URI = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, ALL_TASKS));
    public static final Uri PERFORM_ALL_PENDING_URI = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, PENDING));
    public static final Uri PERFORM_ALL_COMPLETED_URI = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, COMPLETED_TASKS));
    public static final Uri PERFORM_FIND_ONE_URI = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, ONE));
    public static final Uri PERFORM_INSERT = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, NEW));

    public static final Uri SUCCESS_URI = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, "SUCCESS"));
    public static final Uri FAIL_URI = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, "FAIL"));


    //execute when class is loaded into JVM
    static {
        //output for - 1: content://com.cb007787.timetabler.provider/task/all
        //output for - 4: content://com.cb007787.timetabler.provider/task/one/#
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + ALL_TASKS, 1);//content uri for the get all tasks query
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + COMPLETED_TASKS, 2);//content uri for the get all completed tasks
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + PENDING, 3);//content uri for the get all pending tasks
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + ONE + "/#", 4);//content uri for the get task by id
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + NEW, 5);//content uri for the create new task.
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //match is tested against the "code" added in static block for each uri.
        switch (uriMatcher.match(uri)) {
            case 1: {
                //client requires all tasks
                break;
            }
            case 2: {
                //client requires all completed tasks
                break;
            }
            case 3: {
                //client requires all pending tasks
                break;
            }
            case 4: {
                //client requires task by id
                break;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (uriMatcher.match(uri) == 5) {
            if (values != null) {
                try {
                    String taskName = values.getAsString("taskName").trim();
                    String taskDescription = values.getAsString("taskDescription").trim();
                    long startDate = values.getAsLong("startDate");
                    long endDate = values.getAsLong("endDate");
                    String usernameOfStudent = values.getAsString("username");

                    Task theTask = new Task();
                    theTask.setTaskName(taskName);
                    theTask.setTaskDescription(taskDescription);
                    theTask.setStartDateInMs(startDate);
                    theTask.setDueDateInMs(endDate);
                    theTask.setStudentUsername(usernameOfStudent);
                    theTask.setTaskStatus("Pending");
                    theTask.setTaskCreatedAtInMs(Calendar.getInstance().getTimeInMillis());
                    theTask.setLastUpdatedAtInMs(Calendar.getInstance().getTimeInMillis());

//                    TaskDAO taskDAO = TimeTablerDatabase.getTimeTablerDatabase(getContext()).getTaskDAO();
//                    taskDAO.createTask(theTask);
                    return SUCCESS_URI;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return FAIL_URI;
                }
            } else {
                return FAIL_URI;
            }
        } else {
            return FAIL_URI;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
