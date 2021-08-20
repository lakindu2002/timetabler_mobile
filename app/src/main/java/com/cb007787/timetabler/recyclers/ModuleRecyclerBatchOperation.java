package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.BatchService;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Recycler view used to show the modules when the single back information is viewed.
 * contains layout configs for de-assigning modules from the batch.
 */
public class ModuleRecyclerBatchOperation extends RecyclerView.Adapter<ModuleRecyclerBatchOperation.ViewHolder> {
    private Context context;
    private List<Module> moduleList;
    private BatchService batchService;
    private final String batchCode;
    private DeleteCallbacks deleteCallbacks;

    public ModuleRecyclerBatchOperation(Context context, String batchCode) {
        this.context = context;
        this.batchCode = batchCode;
        batchService = APIConfigurer.getApiConfigurer().getBatchService();
    }

    public void setDeleteCallbacks(DeleteCallbacks deleteCallbacks) {
        this.deleteCallbacks = deleteCallbacks;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setModuleList(List<Module> moduleList) {
        this.moduleList = moduleList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ModuleRecyclerBatchOperation.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.batch_module_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleRecyclerBatchOperation.ViewHolder holder, int position) {
        Module module = moduleList.get(position);
        holder.lecturerName.setText(String.format("%s %s", module.getTheLecturer().getFirstName(), module.getTheLecturer().getLastName()));
        holder.moduleName.setText(module.getModuleName());
        holder.deAssignButton.setOnClickListener(v -> {
            //de-assign module.
            showConfirmationDialog(module.getModuleId());
        });
    }

    private void showConfirmationDialog(int moduleId) {
        //ask user if they want to actually de-assign
        new MaterialAlertDialogBuilder(context)
                .setTitle("De-Assign Module")
                .setMessage("Are you sure you want to de-assign module from this batch?")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .setPositiveButton("De-Assign Module", (dialog, which) -> {
                    //de-assign in db
                    deleteCallbacks.onDeleteCalled();

                    Call<SuccessResponseAPI> deAssignCall = batchService.deAssignModuleFromBatch(
                            SharedPreferenceService.getToken(context, PreferenceInformation.PREFERENCE_NAME),
                            moduleId,
                            batchCode
                    );

                    deAssignCall.enqueue(new Callback<SuccessResponseAPI>() {
                        @Override
                        public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                            if (response.isSuccessful()) {
                                //successfully de-assigned
                                deleteCallbacks.onDeleteSuccessResponse(response.body());
                            } else {
                                try {
                                    ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                                    deleteCallbacks.onDeleteFailure(theErrorReturned.getErrorMessage());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    deleteCallbacks.onDeleteFailure("We ran into an error while de-assigning the module");
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                            t.printStackTrace();
                            deleteCallbacks.onDeleteFailure("We ran into an error while de-assigning the module");
                        }
                    });
                })
                .show(); //show the modal
    }

    @Override
    public int getItemCount() {
        return moduleList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected MaterialTextView moduleName;
        protected MaterialTextView lecturerName;
        protected Button deAssignButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleName = itemView.findViewById(R.id.module_title);
            lecturerName = itemView.findViewById(R.id.taught_by);
            deAssignButton = itemView.findViewById(R.id.de_assign_module_btn);
        }
    }
}
