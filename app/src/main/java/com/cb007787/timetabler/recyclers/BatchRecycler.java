package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.model.BatchShow;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.BatchService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BatchRecycler extends RecyclerView.Adapter<BatchRecycler.ViewHolder> {
    private Context theContext;
    private List<BatchShow> batchList;
    private DeleteCallbacks deleteCallbacks;
    private BatchService batchService;

    public BatchRecycler(Context theContext) {
        this.theContext = theContext;
        this.batchList = new ArrayList<>();
        this.batchService = APIConfigurer.getApiConfigurer().getBatchService();
    }

    public void setBatchList(List<BatchShow> batchList) {
        this.batchList = batchList;
        notifyDataSetChanged();
    }

    public void setDeleteCallbacks(DeleteCallbacks deleteCallbacks) {
        this.deleteCallbacks = deleteCallbacks;
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

            theMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.update_batch_name) {
                    //launch update batch name modal
                    launchUpdateModal(batchAtPosition);
                } else if (item.getItemId() == R.id.delete_batch) {
                    //launch delete batch
                    launchDeleteModal(batchAtPosition);
                }
                return true;
            });
            theMenu.show();
        });
    }

    private void launchUpdateModal(BatchShow batchAtPosition) {
        new MaterialAlertDialogBuilder(theContext)
                .setTitle("Update Batch Name")
                .setNegativeButton("Close", (dialog, which) -> {

                });
    }


    private void launchDeleteModal(BatchShow theBatch) {
        new MaterialAlertDialogBuilder(theContext)
                .setTitle("Delete Batch")
                .setMessage("Are you sure that you want to delete this batch?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteBatchInDB(theBatch);
                })
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
