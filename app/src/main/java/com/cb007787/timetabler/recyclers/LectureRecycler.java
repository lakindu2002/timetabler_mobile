package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.Classroom;
import com.cb007787.timetabler.model.LectureShow;
import com.cb007787.timetabler.model.User;

import java.util.List;

public class LectureRecycler extends RecyclerView.Adapter<LectureRecycler.LectureHolder> {

    private final Context theContext;
    private List<LectureShow> theLectureList;

    public LectureRecycler(Context theContext, List<LectureShow> theLectureList) {
        this.theContext = theContext;
        this.theLectureList = theLectureList;
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

        holder.getLecturerName().setText(lecturerName);
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
        private final TextView lecturerName;
        private final TextView timeDuration;
        private final TextView venue;

        public LectureHolder(@NonNull View itemView) {
            super(itemView);
            moduleName = itemView.findViewById(R.id.module_name);
            lecturerName = itemView.findViewById(R.id.lecturer_name);
            timeDuration = itemView.findViewById(R.id.duration);
            venue = itemView.findViewById(R.id.venue);
        }

        public TextView getModuleName() {
            return moduleName;
        }

        public TextView getLecturerName() {
            return lecturerName;
        }

        public TextView getTimeDuration() {
            return timeDuration;
        }

        public TextView getVenue() {
            return venue;
        }
    }
}
