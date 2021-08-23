package com.cb007787.timetabler.provider;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface TaskDAO {
    @Insert
    void createTask(Task newTask);
}
