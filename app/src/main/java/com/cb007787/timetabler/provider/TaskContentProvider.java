package com.cb007787.timetabler.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Class will act as the interface that external apps use to communicate with the content provided - SQLite Task Database.
 */
public class TaskContentProvider extends ContentProvider {
    private TaskDAO taskDAO; //the DAO used to perform crud with task database.
    //do not create a match for the root URI
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public boolean onCreate() {
        try {
            //retrieve the task dao.
            taskDAO = TimeTablerDatabase.getTimeTablerDatabase(getContext()).getTaskDAO();
            return true;//provider successfully loaded
        } catch (Exception ex) {
            ex.printStackTrace();
            return false; //provider failed to load.
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
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
        return null;
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
