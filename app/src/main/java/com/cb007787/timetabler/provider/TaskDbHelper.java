package com.cb007787.timetabler.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "timetabler-note-keeper.db";
    public static String TABLE_NAME = "tasks";
    private static final int DATABASE_VERSION = 1;

    public static String TASK_NAME = "task_name";
    public static String TASK_DESCRIPTION = "task_description";
    public static String START_DATE = "start_date";
    public static String DUE_DATE = "due_date";
    public static String TASK_STATUS = "task_status";
    public static String TASK_CREATED_AT = "task_created_at";
    public static String LAST_UPDATED_AT = "last_updated_at";
    public static String USERNAME = "username";

    private static TaskDbHelper DATABASE_SINGLETON;

    private static final String SQL_CREATE_TABLE = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
                    "id INTEGER AUTO_INCREMENT PRIMARY KEY," +
                    "%s VARCHAR(255) NOT NULL," +
                    "%s VARCHAR(255) NOT NULL," +
                    "%s LONG NOT NULL," +
                    "%s LONG NOT NULL," +
                    "%s VARCHAR(255) NOT NULL," +
                    "%s LONG NOT NULL," +
                    "%s LONG NOT NULL," +
                    "%s VARCHAR(255) NOT NULL); ", TABLE_NAME, TASK_NAME, TASK_DESCRIPTION, START_DATE, DUE_DATE, TASK_STATUS, TASK_CREATED_AT, LAST_UPDATED_AT, USERNAME
    );

    protected TaskDbHelper(Context theContext) {
        super(theContext, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static TaskDbHelper getDatabaseSingleton(Context theContext) {
        if (DATABASE_SINGLETON == null) {
            DATABASE_SINGLETON = new TaskDbHelper(theContext);
        }
        return DATABASE_SINGLETON;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create the database table.
        db.execSQL(SQL_CREATE_TABLE);
        onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //upgrade database with new version
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public synchronized void close() {
        super.close();
    }
}
