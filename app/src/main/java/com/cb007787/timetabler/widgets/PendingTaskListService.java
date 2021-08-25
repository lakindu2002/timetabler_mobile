package com.cb007787.timetabler.widgets;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.Task;
import com.cb007787.timetabler.provider.TaskContentProvider;
import com.cb007787.timetabler.provider.TaskDbHelper;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PendingTaskListService extends RemoteViewsService {
    //the service class that is used to fetch data for a dynamic list
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        //remote views factory - adapter in recycler view
        return new PendingTaskListViewFactory(getApplicationContext(), new ArrayList<>());
    }

    public static class PendingTaskListViewFactory implements RemoteViewsFactory {
        //define the adapter for the StackView.
        private final Context context;
        private final List<Task> taskList;
        private AuthReturn authReturn;
        private final SimpleDateFormat dateFormat;
        private final SimpleDateFormat createFormat;
        private Cursor retrievedQuery;

        public PendingTaskListViewFactory(Context context, List<Task> taskList) {
            this.context = context;
            this.taskList = taskList;
            dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            createFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            Log.i("onDataSetChanged - PendingTaskListService", "Data Set Update Triggered");
            //clear identity of remote process, so permission is checked against app and not against external caller.
            //it uses the external party context for permission checking, but this self call so can clear.
            long contentProviderToken = Binder.clearCallingIdentity();
            try {
                authReturn = SharedPreferenceService.getLoggedInUser(context, PreferenceInformation.PREFERENCE_NAME);
                if (authReturn != null) {
                    //if logged in user, then fetch data.
                    String[] columnsRequired = {
                            TaskDbHelper.TASK_NAME,
                            TaskDbHelper.START_DATE,
                            TaskDbHelper.DUE_DATE,
                            TaskDbHelper.TASK_CREATED_AT,
                            TaskDbHelper.TASK_STATUS
                    };
                    String[] whereArgs = {"Pending", authReturn.getUsername()};
                    retrievedQuery = context.getContentResolver().query(TaskContentProvider.PERFORM_ALL_PENDING_URI, columnsRequired, null, whereArgs, "ASC");


                    if (retrievedQuery != null && !retrievedQuery.isClosed()) {
                        //if query is prsent and it is not closed.
                        if (retrievedQuery.moveToFirst()) {
                            //row exists
                            taskList.clear(); //clear data before adding again.
                            while (!retrievedQuery.isAfterLast()) {
                                //iterate over the rows
                                String taskName = retrievedQuery.getString(retrievedQuery.getColumnIndex(TaskDbHelper.TASK_NAME));
                                String taskStatus = retrievedQuery.getString(retrievedQuery.getColumnIndex(TaskDbHelper.TASK_STATUS));
                                long startDate = retrievedQuery.getLong(retrievedQuery.getColumnIndex(TaskDbHelper.START_DATE));
                                long dueDate = retrievedQuery.getLong(retrievedQuery.getColumnIndex(TaskDbHelper.DUE_DATE));
                                long createDate = retrievedQuery.getLong(retrievedQuery.getColumnIndex(TaskDbHelper.TASK_CREATED_AT));

                                Task eachTask = new Task();
                                eachTask.setTaskName(taskName);
                                eachTask.setStartDateInMs(startDate);
                                eachTask.setDueDateInMs(dueDate);
                                eachTask.setTaskCreatedAtInMs(createDate);
                                eachTask.setTaskStatus(taskStatus);

                                taskList.add(eachTask);

                                retrievedQuery.moveToNext(); //go to next row.
                            }
                        }
                        retrievedQuery.close(); //close query.
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //give the token back to the caller.
                Binder.restoreCallingIdentity(contentProviderToken);
            }
        }


        @Override
        public void onDestroy() {
            if (retrievedQuery != null) {
                retrievedQuery.close(); //close connection.
            }
        }

        @Override
        public int getCount() {
            return taskList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (taskList.size() != 0) {
                //if there are objects present.
                Task taskAtPosition = taskList.get(position);
                RemoteViews viewForEachCard = new RemoteViews(context.getPackageName(), R.layout.widget_pending_each_card);
                viewForEachCard.setTextViewText(R.id.widget_task_name, taskAtPosition.getTaskName());
                viewForEachCard.setTextViewText(R.id.widget_created, "Created: " + createFormat.format(new Date(taskAtPosition.getTaskCreatedAtInMs())));
                viewForEachCard.setTextViewText(R.id.widget_duration, String.format(
                        "From %s to %s", dateFormat.format(new Date(taskAtPosition.getStartDateInMs())),
                        dateFormat.format(new Date(taskAtPosition.getDueDateInMs()))
                ));
                viewForEachCard.setTextViewText(R.id.widget_status, String.format("Status: %s", taskAtPosition.getTaskStatus()));

                return viewForEachCard;
            } else {
                return null;
            }
        }

        @Override
        public RemoteViews getLoadingView() {
            //use default loading view when data is being loaded via the remote views factory.
            return null;
        }


        @Override
        public int getViewTypeCount() {
            //return different types of views to use in collection
            return 1; //using only one layout.
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
