package com.cb007787.timetabler.provider;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

//@Database() - defines class as a room database
//entities - the tables in the database : classes marked with @Entity
//abstract methods required for DAO construction - Classes marked with @Dao
@Database(version = 1, entities = {Task.class}, exportSchema = false)
// task is an entity of this DB.
public abstract class TimeTablerDatabase extends RoomDatabase {
    private static TimeTablerDatabase timeTablerDatabase;

    protected TimeTablerDatabase() {
        //protected construct is needed as super constructor is required to be called
        //this however, still prevents outside clients from calling "new"
    }

    //synchronized used in multi-threading scenarios.
    public static synchronized TimeTablerDatabase getTimeTablerDatabase(Context theContext) {
        if (timeTablerDatabase == null) {
            createDbInstance(theContext);
        }
        return timeTablerDatabase;
    }

    private static void createDbInstance(Context theContext) {
        //construct a database of name = "timetabler-note-keeper"
        timeTablerDatabase = Room
                .databaseBuilder(theContext, TimeTablerDatabase.class, "timetabler-note-keeper")
                .build();
    }

    //has @Query that has SQL queries defined in it. Room defined the underlying sql implementations at compile time
    public abstract TaskDAO getTaskDAO();
}
