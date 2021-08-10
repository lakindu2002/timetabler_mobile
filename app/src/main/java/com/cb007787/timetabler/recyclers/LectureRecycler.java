package com.cb007787.timetabler.recyclers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
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
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.model.User;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.LectureService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.lecturer.LecturerHome;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LectureRecycler extends RecyclerView.Adapter<LectureRecycler.LectureHolder> {

    private final Context theContext;
    private List<LectureShow> theLectureList;
    private static String userRole;
    private LectureService lectureService;

    public LectureRecycler(Context theContext, List<LectureShow> theLectureList) {
        this.theContext = theContext;
        this.theLectureList = theLectureList;
        this.lectureService = APIConfigurer.getApiConfigurer().getLectureService();
    }

    public void setTheLectureList(List<LectureShow> theLectureList) {
        this.theLectureList = theLectureList;
        notifyDataSetChanged(); //trigger a notification to the adapter so that it updates the data respectively.
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
            //when the academic admin or the lecturer clicks cancel lecture
            holder.getDeleteButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //show the popup modal for the delete
                    showDeleteModal(theLectureForView, holder.getBatchLecturerView());
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

    private void showDeleteModal(LectureShow theLectureForView, View holder) {
        //construct a material dialog
        MaterialAlertDialogBuilder deleteDialog = new MaterialAlertDialogBuilder(theContext);
        deleteDialog.setTitle("Cancel Session");
        deleteDialog.setMessage("Are you sure that you want to cancel this session?");
        deleteDialog.setNegativeButton("Close", (dialog, which) -> {
            //user click close.
            //do nothing
        });
        deleteDialog.setPositiveButton("Cancel Lecture", (dialog, which) -> {
            //user click cancel, cancel the session from the database.
            //construct loading spinner to show action occuring
            ProgressDialog theDeleteProgress = new ProgressDialog(theContext);
            theDeleteProgress.setCancelable(false);
            theDeleteProgress.setMessage("Cancelling the lecture...");
            theDeleteProgress.show();

            //execute api call
            deleteLectureFromDB(theDeleteProgress, holder, theLectureForView);
        });

        deleteDialog.show(); //show delete popup
    }

    private void deleteLectureFromDB(ProgressDialog theDeleteProgress, View theView, LectureShow theLectureForView) {
        Call<SuccessResponseAPI> deleteCall = lectureService.cancelLecture(SharedPreferenceService.getToken(theContext, PreferenceInformation.PREFERENCE_NAME), theLectureForView.getLectureId());
        //hit the call asynchronously
        deleteCall.enqueue(new Callback<SuccessResponseAPI>() {
            @Override
            public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                //api sent success or failure
                theDeleteProgress.hide();
                if (response.isSuccessful()) {
                    //cancelled successfully

                    //check user role and refresh the page for lectures
                    if (getUserRole().equalsIgnoreCase("lecturer")) {
                        LecturerHome theHomeOfLecturer = (LecturerHome) theContext;
                        theHomeOfLecturer.getLecturesForSelectedDate(new Date()); //load today lectures for lecturer to refresh from DB
                        //fill lecturer home with today lectures
                    } else {
                        //fill academic admin
                    }
                    //general success message
                    Snackbar theSnackbar = Snackbar.make(theView, response.body().getMessage(), Snackbar.LENGTH_LONG);
                    theSnackbar.setBackgroundTint(theContext.getResources().getColor(R.color.btn_success, null));
                    theSnackbar.show();
                } else {
                    //response didn't execute successfully
                    theDeleteProgress.hide();
                    try {
                        //show error message
                        Snackbar theSnackbar = Snackbar.make(theView, APIConfigurer.getTheErrorReturned(response.errorBody()).getErrorMessage(), Snackbar.LENGTH_INDEFINITE);
                        theSnackbar.setBackgroundTint(theContext.getResources().getColor(R.color.btn_danger, null));
                        theSnackbar.show();
                    } catch (IOException e) {
                        //failed to obtain pojo from json error
                        Log.e("Error", e.getLocalizedMessage());
                        Snackbar theSnackbar = Snackbar.make(theView, "We ran into an unexpected error. Please try again.", Snackbar.LENGTH_INDEFINITE);
                        theSnackbar.setBackgroundTint(theContext.getResources().getColor(R.color.btn_danger, null));
                        theSnackbar.show();
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                //failed to send or parse request
                theDeleteProgress.hide();
                Snackbar theSnackbar = Snackbar.make(theView, "We ran into an unexpected error. Please try again.", Snackbar.LENGTH_INDEFINITE);
                theSnackbar.setBackgroundTint(theContext.getResources().getColor(R.color.btn_danger, null));
                theSnackbar.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return theLectureList.size();
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
