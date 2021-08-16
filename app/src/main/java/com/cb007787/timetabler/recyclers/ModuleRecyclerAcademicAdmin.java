package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.model.User;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModuleRecyclerAcademicAdmin extends RecyclerView.Adapter<ModuleRecyclerAcademicAdmin.ViewHolder> {
    private final Context theContext;
    private final ModuleService moduleService;
    private List<Module> modulesList;
    private DeleteCallbacks callbacks;

    public ModuleRecyclerAcademicAdmin(Context theContext) {
        this.theContext = theContext;
        moduleService = APIConfigurer.getApiConfigurer().getModuleService();
        modulesList = new ArrayList<>();
    }

    public void setModulesList(List<Module> modulesList) {
        this.modulesList = modulesList;
        notifyDataSetChanged();
    }

    public void setCallbacks(DeleteCallbacks callbacks) {
        this.callbacks = callbacks;
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
        holder.getMoreButton().setOnClickListener(v -> {
            PopupMenu thePopupMenu = new PopupMenu(theContext, holder.getMoreButton());
            thePopupMenu.inflate(R.menu.academic_admin_module_menu);
            thePopupMenu.setOnDismissListener(PopupMenu::dismiss);
            thePopupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.delete_module) {
                    //create a popup to denote whether to delete the module.
                    new MaterialAlertDialogBuilder(theContext)
                            .setTitle("Are you sure you want to delete this module?")
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.cancel();
                            })
                            .setPositiveButton("Delete", (dialog, which) -> {
                                callbacks.onDeleteCalled();
                                Call<SuccessResponseAPI> deleteCall = moduleService.deleteModule(module.getModuleId(), SharedPreferenceService.getToken(theContext, PreferenceInformation.PREFERENCE_NAME));

                                deleteCall.enqueue(new Callback<SuccessResponseAPI>() {
                                    @Override
                                    public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                                        if (response.isSuccessful()) {
                                            //deleted successfully
                                            callbacks.onDeleteSuccessResponse(response.body());
                                        } else {
                                            //failed
                                            try {
                                                ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                                                callbacks.onDeleteFailure(theErrorReturned.getErrorMessage());
                                            } catch (IOException e) {
                                                callbacks.onDeleteFailure("We ran into an error while deleting the module");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                                        callbacks.onDeleteFailure("We ran into an error while deleting the module");
                                    }
                                });
                            })
                            .show();
                }
                return true;
            });
            thePopupMenu.show(); //show popup box
        });
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
