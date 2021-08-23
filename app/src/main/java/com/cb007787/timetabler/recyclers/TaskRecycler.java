package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.provider.Task;
import com.google.android.material.textview.MaterialTextView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskRecycler extends RecyclerView.Adapter<TaskRecycler.ViewHolder> {
    private Context context;
    private List<Task> theTasks;
    private SimpleDateFormat simpleDateFormat;

    public TaskRecycler(Context context) {
        this.context = context;
        simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskDuration = itemView.findViewById(R.id.task_duration);
            taskName = itemView.findViewById(R.id.task_name);
            createdDate = itemView.findViewById(R.id.created_at);
            taskStatus = itemView.findViewById(R.id.task_status);
        }
    }
}
