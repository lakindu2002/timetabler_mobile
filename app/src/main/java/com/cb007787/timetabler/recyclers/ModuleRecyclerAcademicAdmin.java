package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.interfaces.DeleteCallbacks;
import com.cb007787.timetabler.interfaces.UpdateCallbacks;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.model.User;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.service.UserService;
import com.cb007787.timetabler.view.academic_admin.AcademicAdminCreateModule;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Utilized by the academic admin in the "ModuleManagement Component"
 */
public class ModuleRecyclerAcademicAdmin extends RecyclerView.Adapter<ModuleRecyclerAcademicAdmin.ViewHolder> {
    private final Context theContext;
    private final ModuleService moduleService;
    private List<Module> modulesList;
    private DeleteCallbacks callbacks;
    private UpdateCallbacks updateCallbacks;
    private final UserService userService;

    public ModuleRecyclerAcademicAdmin(Context theContext) {
        this.theContext = theContext;
        moduleService = APIConfigurer.getApiConfigurer().getModuleService();
        userService = APIConfigurer.getApiConfigurer().getUserService();
        modulesList = new ArrayList<>();
    }

    public void setModulesList(List<Module> modulesList) {
        this.modulesList = modulesList;
        notifyDataSetChanged();
    }

    public void setUpdateCallbacks(UpdateCallbacks updateCallbacks) {
        this.updateCallbacks = updateCallbacks;
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

        String lecturerName = theLecturer != null ? String.format("%s %s", theLecturer.getFirstName(), theLecturer.getLastName()) : "No Lecturer";
        String batchCount = batchCountInModule == 0 ? "No Batches" : String.format(Locale.ENGLISH, "%d", batchCountInModule);

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

            Menu theInflatedMenuOnPopup = thePopupMenu.getMenu();
            //if the lecturer has a module, show only "Change Lecturer"
            //if lecturer has no module, show only "Assign Lecturer"

            if (module.getTheLecturer() != null) {
                //lecturer is assigned, show only change lecturer
                theInflatedMenuOnPopup.removeItem(R.id.assign_lecturer);
            } else {
                //remove change lecturer button as no lecturer in module exists, can only assign
                theInflatedMenuOnPopup.removeItem(R.id.change_lecturer_from_module);
            }

            thePopupMenu.setOnDismissListener(PopupMenu::dismiss);
            thePopupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.delete_module) {
                    //create a popup to denote whether to delete the module.
                    launchDeleteModal(module);
                } else if (item.getItemId() == R.id.update_module) {
                    //navigate to edit page.
                    Intent theEditIntent = new Intent(theContext, AcademicAdminCreateModule.class);
                    //parse the module id to fetch from server in next call.
                    theEditIntent.putExtra("theModule", module.getModuleId());
                    theContext.startActivity(theEditIntent);
                } else if (item.getItemId() == R.id.assign_lecturer) {
                    //load all lecturers to be assigned to module
                    loadAllLecturersAndShowConfirmDialog(module, false);
                } else if (item.getItemId() == R.id.change_lecturer_from_module) {
                    //load re-assign section
                    loadAllLecturersAndShowConfirmDialog(module, true);
                }
                return true;
            });
            thePopupMenu.show(); //show popup box
        });
    }

    private void loadAllLecturersAndShowConfirmDialog(Module theModule, boolean isReAssignLecturer) {
        //load all the lecturers from the server to enable re-assignment and assignment of lecturer to a module
        Call<List<User>> allLecturersCall = userService.getAllLecturers(SharedPreferenceService.getToken(theContext, PreferenceInformation.PREFERENCE_NAME));
        allLecturersCall.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful()) {
                    //response occurred successfully
                    //launch the modal based on assign/re-assign mode
                    launchLecturersManagementModal(response.body(), theModule, isReAssignLecturer);
                } else {
                    //error
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        Toast.makeText(theContext, theErrorReturned.getErrorMessage(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(theContext, "Failed To Load Lecturers", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                //error
                Toast.makeText(theContext, "Failed To Load Lecturers", Toast.LENGTH_LONG).show();
            }
        });
    }

    //method will launch an alert dialog that is of confirmation so user can select the academic administrators.
    private void launchLecturersManagementModal(List<User> lecturersList, Module theModule, boolean isReAssignLecturer) {
        CharSequence[] alertRequiredList = new CharSequence[lecturersList.size()];
        List<String> userList = new ArrayList<>(); //used to hold each username from the lecturerList.

        for (User eachLecturer : lecturersList) {
            //insert each username into the userlist.
            userList.add(String.format("%s", eachLecturer.getUsername()));
        }

        alertRequiredList = userList.toArray(new CharSequence[userList.size()]); //alert requires CharSequence array
        int checkedItem = -1; //initially none checked

        final CharSequence[] finalAlertRequiredList = alertRequiredList; //required to access inside callback

        //create a modal to assign/re-assign
        MaterialAlertDialogBuilder theBuilder = new MaterialAlertDialogBuilder(theContext);

        theBuilder.setSingleChoiceItems(alertRequiredList, checkedItem, (dialog, which) -> {
            //assign the items displayed
            //when user selects an input from the selection, assign it to the module as te lecturer
            String selectedLecturerUsername = finalAlertRequiredList[which].toString();
            User selectedUser = lecturersList.stream().filter((eachUser) -> eachUser.getUsername().equalsIgnoreCase(selectedLecturerUsername)).findFirst().get();
            theModule.setTheLecturer(selectedUser); //assign the lecturer for the module
        });
        theBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        if (isReAssignLecturer) {
            //lecturer being re-assigned
            //have initial lecturer information
            User initiallyPresentLecturer = theModule.getTheLecturer();
            theBuilder.setTitle("Re-Assign Lecturer In Module");
            //user click positive button
            theBuilder.setPositiveButton("Re-Assign lecturer In Module", (dialog, which) -> {
                if (theModule.getTheLecturer().getUsername().equalsIgnoreCase(initiallyPresentLecturer.getUsername())) {
                    updateCallbacks.onUpdateFailed("This lecturer is already teaching this module");
                } else {
                    //confirm re-assignment in DB using API
                    reAssignLecturerInDb(theModule);
                }
            });
        } else {
            //assigning a new lecturer for the module
            theBuilder.setTitle("Assign Lecturer For Module");
            theBuilder.setPositiveButton("Assign Lecturer To Module", (dialog, which) -> {
                //user click "confirm"
                if (theModule.getTheLecturer() == null) {
                    updateCallbacks.onUpdateFailed("Please select a lecturer before proceeding");
                } else {
                    assignLecturerToModuleInDB(theModule); //update in database over api.
                }
            });
        }
        theBuilder.show(); //launch modal.
    }

    //module will update the lecturer teaching the module and re-assign new lecturer
    private void reAssignLecturerInDb(Module theModule) {
        updateCallbacks.onUpdate();
        Call<SuccessResponseAPI> reAssignCall = moduleService.reAssignLecturerInModule(theModule, SharedPreferenceService.getToken(theContext, PreferenceInformation.PREFERENCE_NAME));

        reAssignCall.enqueue(new Callback<SuccessResponseAPI>() {
            @Override
            public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                if (response.isSuccessful()) {
                    //lecturer assigned to module successfully
                    updateCallbacks.onUpdateCompleted(response.body());
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        updateCallbacks.onUpdateFailed(theErrorReturned.getErrorMessage());
                    } catch (IOException e) {
                        updateCallbacks.onUpdateFailed("We ran into an error while re-assigning the lecturer for this module");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                updateCallbacks.onUpdateFailed("We ran into an error while re-assigning the lecturer for this module");
            }
        });

    }

    //method will assign the lecturer in the api call
    private void assignLecturerToModuleInDB(Module theModule) {
        //update in DB
        updateCallbacks.onUpdate(); //update triggered
        Call<SuccessResponseAPI> assignCall = moduleService.assignLecturerToModule(theModule, SharedPreferenceService.getToken(theContext, PreferenceInformation.PREFERENCE_NAME));

        assignCall.enqueue(new Callback<SuccessResponseAPI>() {
            @Override
            public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                if (response.isSuccessful()) {
                    //lecturer assigned to module successfully
                    updateCallbacks.onUpdateCompleted(response.body());
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        updateCallbacks.onUpdateFailed(theErrorReturned.getErrorMessage());
                    } catch (IOException e) {
                        updateCallbacks.onUpdateFailed("We ran into an error while assigning a lecturer to this module");

                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                //error
                updateCallbacks.onUpdateFailed("We ran into an error while assigning a lecturer to this module");
            }
        });
    }

    private void launchDeleteModal(Module theModule) {
        new MaterialAlertDialogBuilder(theContext)
                .setTitle("Delete Module")
                .setMessage("Are you sure you want to delete this module?")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .setPositiveButton("Delete", (dialog, which) -> {
                    callbacks.onDeleteCalled();
                    Call<SuccessResponseAPI> deleteCall = moduleService.deleteModule(theModule.getModuleId(), SharedPreferenceService.getToken(theContext, PreferenceInformation.PREFERENCE_NAME));

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
