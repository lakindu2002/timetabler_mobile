package com.cb007787.timetabler.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.sql.Date;
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

    //used to identify the type of function type, conjoined during the Content URI creation.
    private static final String ALL_TASKS = "all";
    private static final String NEW = "create";
    private static final String COMPLETED_TASKS = "completed";
    private static final String PENDING = "pending";
    private static final String ONE = "one";
    private static final String DELETE = "delete";
    private static final String COMPLETE = "complete";
    private static final String UPDATE = "update";

    //uris to be called from the client.
    public static final Uri PERFORM_ALL_URI = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, ALL_TASKS));
    public static final Uri PERFORM_ALL_PENDING_URI = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, PENDING));
    public static final Uri PERFORM_ALL_COMPLETED_URI = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, COMPLETED_TASKS));
    public static final Uri PERFORM_FIND_ONE_URI = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, ONE));
    public static final Uri PERFORM_INSERT = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, NEW));
    public static final Uri PERFORM_DELETE = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, DELETE));
    public static final Uri PERFORM_MARK_COMPLETE = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, COMPLETE));
    public static final Uri PERFORM_UPDATE = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, UPDATE));

    public static final Uri SUCCESS_URI = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, "SUCCESS"));
    public static final Uri FAIL_URI = Uri.parse(String.format("content://%s/%s/%s", AUTHORITY, TASK_TABLE, "FAIL"));


    //execute when class is loaded into JVM
    static {
        //output for - 1: content://com.cb007787.timetabler.provider/task/all
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + ALL_TASKS, 1);//content uri for the get all tasks query
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + COMPLETED_TASKS, 2);//content uri for the get all completed tasks
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + PENDING, 3);//content uri for the get all pending tasks
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + ONE, 4);//content uri for the get task by id
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + NEW, 5);//content uri for the create new task.
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + DELETE, 6);//content uri for the delete task.
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + COMPLETE, 7);//content uri for the complete task.
        uriMatcher.addURI(AUTHORITY, TASK_TABLE + "/" + UPDATE, 8);//content uri for the update task.
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        TaskDbHelper taskDbHelper = new TaskDbHelper(getContext());
        database = taskDbHelper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    //projection - columns to get from query, selection - where clause, sort order - ASC or DESC
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //match is tested against the "code" added in static block for each uri.
        switch (uriMatcher.match(uri)) {
            case 1: {
                //client requires all tasks
                return database.query(TaskDbHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, TaskDbHelper.DUE_DATE + " " + sortOrder);
            }
            case 2://client requires all completed tasks

            case 3: { //client requires all pending tasks
                return database.query(TaskDbHelper.TABLE_NAME, projection, TaskDbHelper.TASK_STATUS + "=? AND " + TaskDbHelper.USERNAME + "=?", selectionArgs, null, null, TaskDbHelper.DUE_DATE + " " + sortOrder);
            }
            case 4: {
                //client requires task by id
                return database.query(TaskDbHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            }
        }
        //in case nothing matches, return all tasks for the user.
        return database.query(TaskDbHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, TaskDbHelper.DUE_DATE + " " + sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        if (uri.equals(PERFORM_ALL_PENDING_URI) || uri.equals(PERFORM_ALL_URI) || uri.equals(PERFORM_ALL_COMPLETED_URI)) {
            return "vnd.android.cursor.dir/tasks"; //multiple
        } else {
            return "vnd.android.cursor.item/tasks"; //single
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        //if the caller wishes to create a task
        if (values != null && uriMatcher.match(uri) == 5) {
            //execute only if passed uri is meant for inserting data.
            long currentTime = Calendar.getInstance().getTimeInMillis();
            values.put(TaskDbHelper.TASK_CREATED_AT, currentTime);
            values.put(TaskDbHelper.LAST_UPDATED_AT, currentTime);
            values.put(TaskDbHelper.TASK_STATUS, "Pending");

            //insert into database.
            database.insert(TaskDbHelper.TABLE_NAME, null, values);
            return SUCCESS_URI;
        } else {
            return FAIL_URI;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int deletedRows = 0;
        if (selection != null && selectionArgs != null) {
            if (uriMatcher.match(uri) == 6) {
                //sql delete statement to delete from db.
                deletedRows = database.delete(TaskDbHelper.TABLE_NAME, selection, selectionArgs);
            }
        }
        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int updatedRows = 0;
        if (selection != null && selectionArgs != null) {
            if (uriMatcher.match(uri) == 7) {
                //mark as complete
                ContentValues completeValue = new ContentValues();
                completeValue.put(TaskDbHelper.TASK_STATUS, "Completed");
                completeValue.put(TaskDbHelper.LAST_UPDATED_AT, Calendar.getInstance().getTimeInMillis());
                //table name, update value, where clause, where criteria
                updatedRows = database.update(TaskDbHelper.TABLE_NAME, completeValue, selection, selectionArgs);
            } else if (uriMatcher.match(uri) == 8) {
                //update the row.
                values.put(TaskDbHelper.LAST_UPDATED_AT, Calendar.getInstance().getTimeInMillis());
                updatedRows = database.update(TaskDbHelper.TABLE_NAME, values, selection, selectionArgs);
            }
        }
        return updatedRows;
    }
}
