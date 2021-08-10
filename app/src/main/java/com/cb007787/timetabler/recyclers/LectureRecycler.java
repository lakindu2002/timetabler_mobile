package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.BatchShow;
import com.cb007787.timetabler.model.Classroom;
import com.cb007787.timetabler.model.LectureShow;
import com.cb007787.timetabler.model.User;

import java.util.List;

public class LectureRecycler extends RecyclerView.Adapter<LectureRecycler.LectureHolder> {

    private final Context theContext;
    private List<LectureShow> theLectureList;
    private static String userRole;

    public LectureRecycler(Context theContext, List<LectureShow> theLectureList) {
        this.theContext = theContext;
        this.theLectureList = theLectureList;
    }

    public static String getUserRole() {
        return userRole;
    }

    public static void setUserRole(String userRole) {
        LectureRecycler.userRole = userRole;
    }

    @NonNull
    @Override
    public LectureHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //return a view for the holder.
        LayoutInflater inflater = LayoutInflater.from(theContext);
        View inflateHolder = inflater.inflate(R.layout.lecture_card, parent, false);
        return new LectureHolder(inflateHolder); //return a holder with a view attached to it.
    }

    @Override
    public void onBindViewHolder(@NonNull LectureRecycler.LectureHolder holder, int position) {
        //bind data of each lecture to each view associated to each holder.
        LectureShow theLectureForView = theLectureList.get(position);
        String lecturerName = String.format("%s %s", theLectureForView.getTheModule().getTheLecturer().getFirstName(), theLectureForView.getTheModule().getTheLecturer().getLastName());
        String classroomName = "";

        if (theLectureForView.getTheClassroom() == null) {
            classroomName = "Classroom Not Available";
        } else {
            classroomName = String.format("%s", theLectureForView.getTheClassroom().getClassroomName());
        }

        if (holder.getDeleteButton().getVisibility() == View.VISIBLE) {
            holder.getDeleteButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(theContext, "Delete", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (userRole.equalsIgnoreCase("lecturer")) {
            StringBuilder batchList = new StringBuilder();
            batchList.append("Batches: ");

            for (BatchShow eachBatch : theLectureForView.getBatchesLectureConducedTo()) {
                batchList.append(String.format("%s, ", eachBatch.getBatchCode()));
            }
            holder.getBatchLecturerView().setText(batchList.toString());
        } else {
            holder.getBatchLecturerView().setText(lecturerName);
        }
        holder.getModuleName().setText(theLectureForView.getTheModule().getModuleName());
        holder.getTimeDuration().setText(String.format("%s - %s", theLectureForView.getStartTime(), theLectureForView.getEndTime()));
        holder.getVenue().setText(String.format("at %s", classroomName));
    }

    @Override
    public int getItemCount() {
        return theLectureList.size();
    }

    public void setLoadedLectures(List<LectureShow> loadedLectures) {
        this.theLectureList = loadedLectures;
    }

    public static class LectureHolder extends RecyclerView.ViewHolder {

        private final TextView moduleName;
        private final TextView batchLecturerView;
        private final TextView timeDuration;
        private final TextView venue;
        private ImageView deleteButton;

        public LectureHolder(@NonNull View itemView) {
            super(itemView);
            moduleName = itemView.findViewById(R.id.module_name);
            batchLecturerView = itemView.findViewById(R.id.batch_lecturer_name);
            timeDuration = itemView.findViewById(R.id.duration);
            venue = itemView.findViewById(R.id.venue);
            deleteButton = itemView.findViewById(R.id.cancel_lecture);

            if (userRole.equalsIgnoreCase("student")) {
                deleteButton.setVisibility(View.GONE);
            } else {
                deleteButton.setVisibility(View.VISIBLE);
            }
        }

        public TextView getModuleName() {
            return moduleName;
        }

        public TextView getBatchLecturerView() {
            return batchLecturerView;
        }

        public TextView getTimeDuration() {
            return timeDuration;
        }

        public TextView getVenue() {
            return venue;
        }

        public ImageView getDeleteButton() {
            return deleteButton;
        }

        public void setDeleteButton(ImageView deleteButton) {
            this.deleteButton = deleteButton;
        }
    }
}
