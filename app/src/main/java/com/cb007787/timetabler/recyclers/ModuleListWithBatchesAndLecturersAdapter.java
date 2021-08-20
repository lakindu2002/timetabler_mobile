package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.BatchShow;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.model.User;
import com.cb007787.timetabler.view.common.shared.ScheduleLecture;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.Locale;

public class ModuleListWithBatchesAndLecturersAdapter extends RecyclerView.Adapter<ModuleListWithBatchesAndLecturersAdapter.ViewHolder> {
    private Context theContext;
    private List<Module> moduleList;

    public ModuleListWithBatchesAndLecturersAdapter(Context theContext) {
        this.theContext = theContext;
    }

    public void setModuleList(List<Module> moduleList) {
        this.moduleList = moduleList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater
                        .from(theContext)
                        .inflate(R.layout.modules_with_batches_and_lecturers_card, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Module theModule = moduleList.get(position);
        User theLecturer = theModule.getTheLecturer();
        StringBuilder batchList = new StringBuilder();

        for (BatchShow eachBatch : theModule.getTheBatchList()) {
            batchList.append(String.format("%s,", eachBatch.getBatchCode()));
        }

        holder.moduleName.setText(theModule.getModuleName());
        holder.lecturerName.setText(String.format(Locale.ENGLISH, "Lecturer: %s %s", theLecturer.getFirstName(), theLecturer.getLastName()));
        holder.batchList.setText(String.format("Batches Enrolled: %s", batchList.toString()));
        holder.scheduleButton.setOnClickListener(v -> {
            //call shared class between lecturer and academic admin for lecture scheduling
            Intent scheduleIntent = new Intent(theContext, ScheduleLecture.class);
            scheduleIntent.putExtra("theModuleId", theModule.getModuleId());
            theContext.startActivity(scheduleIntent); //navigate to schedule page.
        });


    }

    @Override
    public int getItemCount() {
        return moduleList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected MaterialTextView moduleName;
        protected MaterialTextView lecturerName;
        protected MaterialTextView batchList;
        protected Button scheduleButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleName = itemView.findViewById(R.id.module_name);
            lecturerName = itemView.findViewById(R.id.lecturer_name);
            batchList = itemView.findViewById(R.id.batches_enrolled);
            scheduleButton = itemView.findViewById(R.id.schedule_btn);
        }
    }
}
