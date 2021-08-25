package com.cb007787.timetabler.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TaskDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "timetabler-note-keeper";
    public static String TABLE_NAME = "tasks";
    private static final int DATABASE_VERSION = 1;

    public static final String TASK_ID = "id";
    public static String TASK_NAME = "task_name";
    public static String TASK_DESCRIPTION = "task_description";
    public static String START_DATE = "start_date";
    public static String DUE_DATE = "due_date";
    public static String TASK_STATUS = "task_status";
    public static String TASK_CREATED_AT = "task_created_at";
    public static String LAST_UPDATED_AT = "last_updated_at";
    public static String USERNAME = "username";

    private static final String SQL_CREATE_TABLE = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "%s VARCHAR(255) NOT NULL," +
                    "%s VARCHAR(255) NOT NULL," +
                    "%s LONG NOT NULL," +
                    "%s LONG NOT NULL," +
                    "%s VARCHAR(255) NOT NULL," +
                    "%s LONG NOT NULL," +
                    "%s LONG NOT NULL," +
                    "%s VARCHAR(255) NOT NULL); ", TABLE_NAME, TASK_ID, TASK_NAME, TASK_DESCRIPTION, START_DATE, DUE_DATE, TASK_STATUS, TASK_CREATED_AT, LAST_UPDATED_AT, USERNAME
    );

    public TaskDbHelper(Context theContext) {
        super(theContext, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create the database table.
        db.execSQL(SQL_CREATE_TABLE);
        Log.i("onCreate SQLite", "Table Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //upgrade database with new version
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onUpgrade(db, oldVersion, newVersion);
        Log.i("onCreate SQLite", "Table Dropped");
    }

    @Override
    public synchronized void close() {
        super.close();
    }
}
