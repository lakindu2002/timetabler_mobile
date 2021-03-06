package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.interfaces.UpdateCallbacks;
import com.cb007787.timetabler.model.BatchShow;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.BatchService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.academic_admin.AcademicAdminAssignModulesStudentsToBatchDialog;
import com.cb007787.timetabler.view.academic_admin.AcademicAdminManageSingleBatch;
import com.cb007787.timetabler.view.academic_admin.AcademicAdminViewLecturesPerBatch;
import com.cb007787.timetabler.view.academic_admin.AcademicAdministratorBatchManagement;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Utilized by academic admin to view all batches and to view batches with lectures in it.
 */
public class BatchRecycler extends RecyclerView.Adapter<BatchRecycler.ViewHolder> {
    private final Context theContext;
    private List<BatchShow> batchList;
    private DeleteCallbacks deleteCallbacks;
    private final BatchService batchService;
    private UpdateCallbacks updateCallback;
    private boolean isTimetableComponent; //check to see if recycler inflated in Timetable management

    public BatchRecycler(Context theContext) {
        this.theContext = theContext;
        this.batchList = new ArrayList<>();
        this.batchService = APIConfigurer.getApiConfigurer().getBatchService();
        isTimetableComponent = false;
    }

    public void setBatchList(List<BatchShow> batchList) {
        this.batchList = batchList;
        notifyDataSetChanged();
    }

    public void setTimetableComponent(boolean timetableComponent) {
        isTimetableComponent = timetableComponent;
    }

    public void setDeleteCallbacks(DeleteCallbacks deleteCallbacks) {
        this.deleteCallbacks = deleteCallbacks;
    }

    public void setUpdateCallback(UpdateCallbacks updateCallback) {
        this.updateCallback = updateCallback;
    }

    @NonNull
    @Override
    public BatchRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(theContext).inflate(R.layout.batch_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BatchRecycler.ViewHolder holder, int position) {
        BatchShow batchAtPosition = batchList.get(position);
        holder.batchName.setText(String.format("Name: %s", batchAtPosition.getBatchName()));
        holder.batchCode.setText(batchAtPosition.getBatchCode());
        holder.modulesInBatch.setText(String.format(Locale.ENGLISH, "Modules Enrolled: %d", batchAtPosition.getModuleList().size()));
        holder.studentsInBatch.setText(String.format(Locale.ENGLISH, "Students Enrolled: %s", batchAtPosition.getStudentList().size()));
        holder.moreButton.setOnClickListener(v -> {
            PopupMenu theMenu = new PopupMenu(theContext, holder.moreButton);
            //anchor popup on the more button
            theMenu.inflate(R.menu.batch_popup);
            Menu inflatedMenu = theMenu.getMenu();

            theMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.update_batch_name) {
                    //launch update batch name modal
                    launchUpdateModal(batchAtPosition);
                } else if (item.getItemId() == R.id.delete_batch) {
                    //launch delete batch
                    launchDeleteModal(batchAtPosition);
                } else if (item.getItemId() == R.id.manage_batch) {
                    //open manage students and modules panel for batch
                    Intent navigationIntent = new Intent(theContext, AcademicAdminManageSingleBatch.class);
                    navigationIntent.putExtra("batchCode", batchAtPosition.getBatchCode());
                    theContext.startActivity(navigationIntent);
                } else if (item.getItemId() == R.id.view_lectures_batch) {
                    //open view lecture for the batch.
                    Intent navigationIntent = new Intent(theContext, AcademicAdminViewLecturesPerBatch.class);
                    navigationIntent.putExtra("batchCode", batchAtPosition.getBatchCode());
                    theContext.startActivity(navigationIntent);
                } else if (item.getItemId() == R.id.assign_modules_batch) {
                    //assign modules to batch
                    //admin on batch management to assign modules to batch.
                    AcademicAdministratorBatchManagement loadedContext = (AcademicAdministratorBatchManagement) theContext;
                    AcademicAdminAssignModulesStudentsToBatchDialog theDialog = AcademicAdminAssignModulesStudentsToBatchDialog.newInstance("assign-modules", batchAtPosition.getBatchCode());
                    theDialog.show(loadedContext.getSupportFragmentManager(), "ASSIGN_MODULES");
                    return true;
                } else if (item.getItemId() == R.id.assign_students_batch) {
                    //assign students to batch
                    AcademicAdministratorBatchManagement loadedContext = (AcademicAdministratorBatchManagement) theContext;
                    AcademicAdminAssignModulesStudentsToBatchDialog theDialog = AcademicAdminAssignModulesStudentsToBatchDialog.newInstance("assign-batches", batchAtPosition.getBatchCode());
                    theDialog.show(loadedContext.getSupportFragmentManager(), "ASSIGN_BATCHES");
                    return true;
                }
                return true;
            });

            if (isTimetableComponent) {
                //recycler loaded inside timetable section (batches with lectures)
                //remove everything else show lectures and manage batch
                inflatedMenu.removeItem(R.id.update_batch_name);
                inflatedMenu.removeItem(R.id.delete_batch);
                inflatedMenu.removeItem(R.id.assign_students_batch);
                inflatedMenu.removeItem(R.id.assign_modules_batch);
            } else {
                //batch recycler inflated inside Batch Management,
                //remove the view lectures button
                inflatedMenu.removeItem(R.id.view_lectures_batch);
            }

            theMenu.show();
        });


    }

    private void launchUpdateModal(BatchShow batchAtPosition) {
        AlertDialog theDialog = new MaterialAlertDialogBuilder(theContext)
                .setTitle("Update Batch Name")
                .setView(R.layout.dialog_update_batch_name)
                .setPositiveButton("Update Batch Name", null) //attach custom listener
                .setNegativeButton("Close", (dialog, which) -> dialog.cancel()).show(); //need to first show before accessing ui elements

        Button updateBtn = theDialog.getButton(DialogInterface.BUTTON_POSITIVE); //get positive button
        //attach custom click listener to it.
        updateBtn.setOnClickListener(v -> {
            //this prevents it from closing on click.
            TextInputLayout theLayout = theDialog.findViewById(R.id.new_name_layout);
            TextInputEditText theNewNameField = theDialog.findViewById(R.id.new_name);

            String enteredNewBatchName = theNewNameField.getText().toString().trim();
            if (enteredNewBatchName.length() == 0) {
                theLayout.setError("Provide a Valid Batch Name");
            } else {
                theLayout.setError(null);
                //update in db
                batchAtPosition.setBatchName(enteredNewBatchName);
                updateInDB(batchAtPosition, theDialog);
            }
        });
    }

    private void updateInDB(BatchShow batchAtPosition, AlertDialog theDialog) {
        updateCallback.onUpdate(); //trigger update for batch name.

        Call<SuccessResponseAPI> updateCall = batchService.updateBatchName(
                batchAtPosition,
                SharedPreferenceService.getToken(theContext, PreferenceInformation.PREFERENCE_NAME));

        updateCall.enqueue(new Callback<SuccessResponseAPI>() {
            @Override
            public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                //response sent back
                if (response.isSuccessful()) {
                    updateCallback.onUpdateCompleted(response.body());
                    theDialog.cancel(); //close the dialog
                } else {
                    //error encountered
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        updateCallback.onUpdateFailed(theErrorReturned.getErrorMessage());
                    } catch (IOException e) {
                        updateCallback.onUpdateFailed("We ran into an error while updating the batch name");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                //failed parse response.
                updateCallback.onUpdateFailed("We ran into an error while updating the batch name");
            }
        });
    }


    private void launchDeleteModal(BatchShow theBatch) {
        new MaterialAlertDialogBuilder(theContext)
                .setTitle("Delete Batch")
                .setMessage("Are you sure that you want to delete this batch?")
                .setPositiveButton("Delete", (dialog, which) -> deleteBatchInDB(theBatch))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show(); //show the constructed dialog to the user.
    }

    private void deleteBatchInDB(BatchShow theBatch) {
        deleteCallbacks.onDeleteCalled(); //delete triggered
        Call<SuccessResponseAPI> deleteCall = batchService.deleteBatch(
                SharedPreferenceService.getToken(theContext, PreferenceInformation.PREFERENCE_NAME),
                theBatch.getBatchCode());
        deleteCall.enqueue(new Callback<SuccessResponseAPI>() {
            @Override
            public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                if (response.isSuccessful()) {
                    //deleted successfully
                    deleteCallbacks.onDeleteSuccessResponse(response.body());
                } else {
                    //delete failed
                    try {
                        ErrorResponseAPI theError = APIConfigurer.getTheErrorReturned(response.errorBody());
                        deleteCallbacks.onDeleteFailure(theError.getErrorMessage());
                    } catch (IOException e) {
                        deleteCallbacks.onDeleteFailure("We ran into an error while deleting the batch");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                deleteCallbacks.onDeleteFailure("We ran into an error while deleting the batch");
            }
        });
    }

    @Override
    public int getItemCount() {
        return batchList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected MaterialTextView batchName;
        protected MaterialTextView batchCode;
        protected MaterialTextView studentsInBatch;
        protected MaterialTextView modulesInBatch;
        protected ImageView moreButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            batchName = itemView.findViewById(R.id.batch_name);
            batchCode = itemView.findViewById(R.id.batch_code);
            studentsInBatch = itemView.findViewById(R.id.students_enrolled);
            modulesInBatch = itemView.findViewById(R.id.modules_enrolled);
            moreButton = itemView.findViewById(R.id.more_options_batch);
        }
    }
}
