package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.model.User;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.ModuleService;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModuleRecyclerAcademicAdmin extends RecyclerView.Adapter<ModuleRecyclerAcademicAdmin.ViewHolder> {
    private final Context theContext;
    private final ModuleService moduleService;
    private List<Module> modulesList;

    public ModuleRecyclerAcademicAdmin(Context theContext) {
        this.theContext = theContext;
        moduleService = APIConfigurer.getApiConfigurer().getModuleService();
        modulesList = new ArrayList<>();
    }

    public void setModulesList(List<Module> modulesList) {
        this.modulesList = modulesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate academic admin card
        LayoutInflater inflater = LayoutInflater.from(theContext);
        View inflatedView = inflater.inflate(R.layout.module_academic_admin_view, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Module module = modulesList.get(position); //get module at each iteration for recycler.
        //assign data to the view.
        User theLecturer = module.getTheLecturer();
        int batchCountInModule = module.getTheBatchList().size();

        String lecturerName = theLecturer != null ? String.format("%s %s", theLecturer.getFirstName(), theLecturer.getLastName()) : "No Lecturer Assigned";
        String batchCount = batchCountInModule == 0 ? "No Batches Enrolled" : String.format(Locale.ENGLISH, "%d", batchCountInModule);

        if (theLecturer == null) {
            //set the text to red
            holder.getLecturerName().setTextColor(theContext.getResources().getColor(R.color.btn_danger, null));
        }

        if (batchCountInModule == 0) {
            //set the text to red
            holder.getBatchCount().setTextColor(theContext.getResources().getColor(R.color.btn_danger, null));
        }

        holder.getModuleName().setText(module.getModuleName());
        holder.getLecturerName().setText(lecturerName);
        holder.getCreditCount().setText(module.getCreditCount());
        holder.getIndependentHours().setText(module.getIndependentHours());
        holder.getContactHours().setText(module.getContactHours());
        holder.getBatchCount().setText(batchCount);
    }

    @Override
    public int getItemCount() {
        return modulesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView moduleName;
        private final ImageView moreButton;
        private final MaterialTextView lecturerName;
        private final MaterialTextView creditCount;
        private final MaterialTextView independentHours;
        private final MaterialTextView contactHours;
        private final MaterialTextView batchCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleName = itemView.findViewById(R.id.module_title);
            moreButton = itemView.findViewById(R.id.more_admin_module);
            lecturerName = itemView.findViewById(R.id.taught_by);
            creditCount = itemView.findViewById(R.id.credit_count);
            independentHours = itemView.findViewById(R.id.independent);
            contactHours = itemView.findViewById(R.id.contact_learning);
            batchCount = itemView.findViewById(R.id.batch_count);
        }

        public MaterialTextView getModuleName() {
            return moduleName;
        }

        public ImageView getMoreButton() {
            return moreButton;
        }

        public MaterialTextView getLecturerName() {
            return lecturerName;
        }

        public MaterialTextView getCreditCount() {
            return creditCount;
        }

        public MaterialTextView getIndependentHours() {
            return independentHours;
        }

        public MaterialTextView getContactHours() {
            return contactHours;
        }

        public MaterialTextView getBatchCount() {
            return batchCount;
        }
    }
}
