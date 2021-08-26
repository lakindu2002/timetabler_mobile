package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.interfaces.UpdateCallbacks;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.model.Task;
import com.cb007787.timetabler.provider.TaskContentProvider;
import com.cb007787.timetabler.provider.TaskDbHelper;
import com.cb007787.timetabler.view.student.StudentTaskCreateUpdate;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskRecycler extends RecyclerView.Adapter<TaskRecycler.ViewHolder> {
    private Context context;
    private List<Task> theTasks;
    private SimpleDateFormat simpleDateFormat;
    private UpdateCallbacks updateCallbacks;
    private DeleteCallbacks deleteCallbacks;

    public TaskRecycler(Context context) {
        this.context = context;
        simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
    }

    public void setTheTasks(List<Task> theTasks) {
        this.theTasks = theTasks;
        notifyDataSetChanged();
    }

    public void setUpdateCallbacks(UpdateCallbacks updateCallbacks) {
        this.updateCallbacks = updateCallbacks;
    }

    public void setDeleteCallbacks(DeleteCallbacks deleteCallbacks) {
        this.deleteCallbacks = deleteCallbacks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(context)
                        .inflate(R.layout.task_card_layout, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task theTask = theTasks.get(position);
        Date startDate = new Date(theTask.getStartDateInMs());
        Date endDate = new Date(theTask.getDueDateInMs());
        Date createdDate = new Date(theTask.getTaskCreatedAtInMs());

        holder.taskName.setText(theTask.getTaskName());
        holder.taskDuration.setText(
                String.format("%s to %s", simpleDateFormat.format(startDate), simpleDateFormat.format(endDate))
        );
        holder.taskStatus.setText(
                String.format("Status: %s", theTask.getTaskStatus())
        );
        holder.createdDate.setText(
                String.format("Created: %s", simpleDateFormat.format(createdDate))
        );
        holder.more.setOnClickListener(v -> {
            //when user clicks more, show popup
            PopupMenu popupMenu = new PopupMenu(context, holder.more);
            popupMenu.inflate(R.menu.student_task_popup);

            Menu menu = popupMenu.getMenu();
            if (theTask.getTaskStatus().equalsIgnoreCase("completed")) {
                //if task is already completed, do not show complete button
                menu.removeItem(R.id.task_complete);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.task_more) {
                    Intent navigationIntent = new Intent(context, StudentTaskCreateUpdate.class);
                    navigationIntent.putExtra("isUpdate", true);
                    navigationIntent.putExtra("taskId", theTask.get_ID());
                    context.startActivity(navigationIntent);
                    return true;
                } else if (item.getItemId() == R.id.task_delete) {
                    //user click delete task
                    launchDeleteModal(theTask.get_ID());
                    return true;
                } else if (item.getItemId() == R.id.task_complete) {
                    //user click complete task.
                    launchMarkCompleteModal(theTask.get_ID());
                    return true;
                } else if (item.getItemId() == R.id.task_add_to_calendar) {
                    //save the intent to the calendar of the mobile
                    Intent theCalendarIntent = new Intent(Intent.ACTION_INSERT); //allow calendar to handle insertion task.
                    theCalendarIntent.setData(CalendarContract.Events.CONTENT_URI); //uri for interacting with calendar events
                    //provide start and end date for the calendar event
                    theCalendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, theTask.getStartDateInMs());
                    theCalendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, theTask.getDueDateInMs());

                    //task title
                    theCalendarIntent.putExtra(CalendarContract.Events.TITLE, theTask.getTaskName());

                    //task description
                    theCalendarIntent.putExtra(CalendarContract.Events.DESCRIPTION, theTask.getTaskDescription());

                    context.startActivity(theCalendarIntent); //launch calendar create
                    return true;
                }
                return false;
            });

            popupMenu.show(); //show menu to user
        });
    }

    private void launchDeleteModal(int id) {
        //show confirmation
        new MaterialAlertDialogBuilder(context)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    //delete in content provider
                    deleteCallbacks.onDeleteCalled();
                    String[] taskDelete = {String.valueOf(id)};
                    int deletedRows = context.getContentResolver().delete(TaskContentProvider.PERFORM_DELETE, TaskDbHelper.TASK_ID + "=?", taskDelete);
                    if (deletedRows == 0) {
                        deleteCallbacks.onDeleteFailure("The task was not deleted");
                    } else {
                        deleteCallbacks.onDeleteSuccessResponse(new SuccessResponseAPI());
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    //cancel
                    dialog.cancel();
                })
                .show(); //show the dialog to user
    }

    private void launchMarkCompleteModal(int id) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Complete Task")
                .setMessage("Are you sure you mark this task as completed?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    //mark as complete in the content provider.
                    updateCallbacks.onUpdate();

                    String[] updateValue = {String.valueOf(id)};
                    //get the content resolver and hit the Update - Mark Complete URI.
                    //pass the WHERE clause and the parameters for TaskId=? where updatedValue = ?.
                    int updatedRows = context.getContentResolver().update(
                            TaskContentProvider.PERFORM_MARK_COMPLETE, null,
                            TaskDbHelper.TASK_ID + "=?",
                            updateValue
                    );
                    if (updatedRows == 0) {
                        //failed to update as no rows were affected
                        updateCallbacks.onUpdateFailed("We could not mark this task as complete");
                    } else {
                        updateCallbacks.onUpdateCompleted(new SuccessResponseAPI());
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    //cancel
                    dialog.cancel();
                })
                .show(); //show the dialog to user
    }

    @Override
    public int getItemCount() {
        return theTasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected MaterialTextView taskName;
        protected MaterialTextView taskDuration;
        protected MaterialTextView createdDate;
        protected MaterialTextView taskStatus;
        protected ImageView more;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskDuration = itemView.findViewById(R.id.task_duration);
            taskName = itemView.findViewById(R.id.task_name);
            createdDate = itemView.findViewById(R.id.created_at);
            taskStatus = itemView.findViewById(R.id.task_status);
            more = itemView.findViewById(R.id.more_options_task);
        }
    }
}
