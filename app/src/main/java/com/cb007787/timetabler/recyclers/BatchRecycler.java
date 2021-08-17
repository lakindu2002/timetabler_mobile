package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.model.BatchShow;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.BatchService;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
