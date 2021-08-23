package com.cb007787.timetabler.provider;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task")
public class Task {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int _ID; //name required to ensure auto-retrieval from provider for the ID.

    @ColumnInfo(name = "name")
    private String taskName;

    @ColumnInfo(name = "description")
    private String taskDescription;

    @ColumnInfo(name = "start_date")
    private long startDateInMs;

    @ColumnInfo(name = "due_date")
    private long dueDateInMs;

    @ColumnInfo(name = "status", defaultValue = "Pending")
    private String taskStatus;

    @ColumnInfo(name = "created_at")
    private long taskCreatedAtInMs;

    @ColumnInfo(name = "updated_at")
    private long lastUpdatedAtInMs;

    @ColumnInfo(name = "student")
    private String studentUsername; //denote the user creating the task, only view logged in users tasks.

    public Task() {
    }

    public int get_ID() {
        return _ID;
    }

    public void set_ID(int _ID) {
        this._ID = _ID;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public long getStartDateInMs() {
        return startDateInMs;
    }

    public void setStartDateInMs(long startDateInMs) {
        this.startDateInMs = startDateInMs;
    }

    public long getDueDateInMs() {
        return dueDateInMs;
    }

    public void setDueDateInMs(long dueDateInMs) {
        this.dueDateInMs = dueDateInMs;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public long getTaskCreatedAtInMs() {
        return taskCreatedAtInMs;
    }

    public void setTaskCreatedAtInMs(long taskCreatedAtInMs) {
        this.taskCreatedAtInMs = taskCreatedAtInMs;
    }

    public long getLastUpdatedAtInMs() {
        return lastUpdatedAtInMs;
    }

    public void setLastUpdatedAtInMs(long lastUpdatedAtInMs) {
        this.lastUpdatedAtInMs = lastUpdatedAtInMs;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }
}
