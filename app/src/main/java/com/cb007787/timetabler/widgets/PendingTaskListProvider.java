package com.cb007787.timetabler.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.cb007787.timetabler.R;

public class PendingTaskListProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //app widget ids - all instances of widgets on home screen as more than one widget can be available at a time
        for (int widgetInstanceId : appWidgetIds) {
            //iterate over each instance of the app widget inflated on the home screen.

            //define the service required to load data for the StackView
            Intent pendingTaskService = new Intent(context, PendingTaskListService.class);
            //remote view because view is accessed in another process
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_pending_task_list);
            remoteViews.setRemoteAdapter(R.id.task_list, pendingTaskService); //set the adapter
            remoteViews.setEmptyView(R.id.task_list, R.id.empty_list); //when no data, show message

            appWidgetManager.notifyAppWidgetViewDataChanged(widgetInstanceId, R.id.task_list); //refresh data.
            appWidgetManager.updateAppWidget(widgetInstanceId, remoteViews); //update the UI on the widget.
        }
    }
}
