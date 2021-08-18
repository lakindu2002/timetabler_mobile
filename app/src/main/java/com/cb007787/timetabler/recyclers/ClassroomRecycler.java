package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.model.Classroom;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.ClassroomService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.system_admin.SystemAdminCreateManageClassroom;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassroomRecycler extends RecyclerView.Adapter<ClassroomRecycler.ClassroomViewHolder> {
    private final Context theContext;
    private List<Classroom> classroomList;
    private final ClassroomService classroomService;
    private DeleteCallbacks onDeleteCallbacks; //implementation will be provided by fragments calling adapter.

    public ClassroomRecycler(Context theContext) {
        this.theContext = theContext;
        this.classroomService = APIConfigurer.getApiConfigurer().getTheClassroomService();
    }

    public void setClassroomList(List<Classroom> classroomList) {
        this.classroomList = classroomList;
        notifyDataSetChanged(); //trigger the recycler view observers to update recycler view as data set has changed.
    }

    public void setOnDeleteCallbacks(DeleteCallbacks onDeleteCallbacks) {
        this.onDeleteCallbacks = onDeleteCallbacks;
    }

    @NonNull
    @Override
    public ClassroomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //return a view that inflates the classroom card
        //do not attach the classroom card to the parent view group.
        LayoutInflater layout = LayoutInflater.from(theContext);
        View inflatedCardView = layout.inflate(R.layout.classroom_card, parent, false);
        return new ClassroomViewHolder(inflatedCardView);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassroomViewHolder holder, int position) {
        //bind each classroom to each view holder.
        Classroom classroom = classroomList.get(position);
        String isAcPresent = classroom.isAcPresent() ? "Yes" : "No";
        String isSmartBoardPresent = classroom.isSmartBoardPresent() ? "Yes" : "No";


        holder.getClassroomName().setText(classroom.getClassroomName());
        holder.getMaximumCapacity().setText(String.format(Locale.ENGLISH, "Maximum Capacity: %d", classroom.getMaxCapacity()));
        holder.getAirConditioner().setText(String.format("Air Conditioner: %s", isAcPresent));
        holder.getSmartBoard().setText(String.format("Smart Board: %s", isSmartBoardPresent));

        //when user clicks more, show a popup menu to edit, delete classroom.
        holder.getMoreButton().setOnClickListener(v -> showMoreOptions(holder.getMoreButton(), classroom));
    }

    @Override
    public int getItemCount() {
        return classroomList.size();
    }

    //method called when user clicks "More Button" in the card.
    private void showMoreOptions(ImageView moreButton, Classroom theClassroom) {
        //anchor the popup menu to the more button
        PopupMenu thePopupMenu = new PopupMenu(theContext, moreButton);
        thePopupMenu.inflate(R.menu.classroom_popup); //inflate the classroom more options menu for this popup

        thePopupMenu.setOnMenuItemClickListener(item -> {
            //react to the buttons clicked by the system administrator
            if (item.getItemId() == R.id.edit_classroom) {
                //open the edit dialog
                showEditDialogForClassroom(theClassroom);
                return true;
            } else if (item.getItemId() == R.id.delete_classroom) {
                //show the delete dialog.
                showDeleteBox(theClassroom);
                return true;
            }
            return false;
        });

        thePopupMenu.show(); //show the popup menu
    }

    private void showEditDialogForClassroom(Classroom theClassroom) {
        //create an intent to navigate to the classroom edit page
        Intent navigationToEdit = new Intent(theContext, SystemAdminCreateManageClassroom.class);
        //set edit mode to true so ui will be changed for editing purposes.
        //handled in onCreate of SystemAdminCreateManageClassroom Activity.
        navigationToEdit.putExtra("editMode", true);
        navigationToEdit.putExtra("classroomId", theClassroom.getClassroomId());

        theContext.startActivity(navigationToEdit); //launch edit page.
    }

    private void showDeleteBox(Classroom theClassroom) {
        new MaterialAlertDialogBuilder(theContext)
                .setTitle("Delete Classroom")
                .setMessage("Are you sure you want to delete this classroom?")
                .setNegativeButton("Close", (dialog, which) -> {
                    dialog.cancel(); //cancel the dialog when user clicks close
                })
                .setPositiveButton("Delete", (dialog, which) -> {
                    //when user clicks delete, actually delete the classroom.
                    deleteTheClassroomInDb(theClassroom);
                }).show(); //show the dialog.
    }

    private void deleteTheClassroomInDb(Classroom theClassroom) {
        //call the callbacks to update the UI state in the actual recycler view.
        onDeleteCallbacks.onDeleteCalled(); //executed when delete is first called.
        Call<SuccessResponseAPI> deleteCall = classroomService.deleteClassroom(theClassroom.getClassroomId(), SharedPreferenceService.getToken(theContext, PreferenceInformation.PREFERENCE_NAME));

        deleteCall.enqueue(new Callback<SuccessResponseAPI>() {
            @Override
            public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                if (response.isSuccessful()) {
                    //classroom deleted successfully
                    onDeleteCallbacks.onDeleteSuccessResponse(response.body());
                } else {
                    //classroom did not delete successfully
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        onDeleteCallbacks.onDeleteFailure(theErrorReturned.getErrorMessage());
                    } catch (IOException e) {
                        onDeleteCallbacks.onDeleteFailure("We ran into an error while deleting the classroom");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                onDeleteCallbacks.onDeleteFailure("We ran into an error while deleting the classroom");
            }
        });
    }

    public static class ClassroomViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView classroomName;
        private final MaterialTextView maximumCapacity;
        private final MaterialTextView airConditioner;
        private final MaterialTextView smartBoard;
        private final ImageView moreButton;

        public ClassroomViewHolder(@NonNull View itemView) {
            super(itemView);
            //retrieve UI references from layout file for classroom_card
            classroomName = itemView.findViewById(R.id.classroom_name);
            maximumCapacity = itemView.findViewById(R.id.max_capacity);
            airConditioner = itemView.findViewById(R.id.ac);
            smartBoard = itemView.findViewById(R.id.smart_board);
            moreButton = itemView.findViewById(R.id.more_button);
        }

        public MaterialTextView getClassroomName() {
            return classroomName;
        }

        public MaterialTextView getMaximumCapacity() {
            return maximumCapacity;
        }

        public MaterialTextView getAirConditioner() {
            return airConditioner;
        }

        public MaterialTextView getSmartBoard() {
            return smartBoard;
        }

        public ImageView getMoreButton() {
            return moreButton;
        }
    }
}
