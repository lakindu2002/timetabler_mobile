package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.view.lecturer.ScheduleLecture;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class ModuleRecycler extends RecyclerView.Adapter<ModuleRecycler.ModuleViewHolder> {

    private final List<Module> moduleList;
    private final Context theContext;
    private static AuthReturn loggedInUser;

    public ModuleRecycler(List<Module> moduleList, Context theContext) {
        this.moduleList = moduleList;
        this.theContext = theContext;
        try {
            loggedInUser = SharedPreferenceService.getLoggedInUser(theContext, PreferenceInformation.PREFERENCE_NAME);
        } catch (JsonProcessingException e) {
            Log.e("ModuleRecycler", "Error Parsing JSON");
        }
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //create and initialize view holder
        //load the UI of the module holder and return it.
        View inflatedViewHolder = LayoutInflater.from(parent.getContext()).inflate(R.layout.module_card, parent, false);
        return new ModuleViewHolder(inflatedViewHolder);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleRecycler.ModuleViewHolder holder, int position) {
        //get module at position
        Module theModuleAtPosition = moduleList.get(position);
        //associate view holder with data from api
        holder.getModuleName().setText(theModuleAtPosition.getModuleName());
        holder.getLecturerName().setText(String.format("%s %s", theModuleAtPosition.getTheLecturer().getFirstName(), theModuleAtPosition.getTheLecturer().getLastName()));
        holder.getCreditCount().setText(theModuleAtPosition.getCreditCount());
        holder.getIndependentHours().setText(String.format("%s Hours", theModuleAtPosition.getIndependentHours()));
        holder.getContactLearning().setText(String.format("%s Hours", theModuleAtPosition.getContactHours()));

        if (holder.getContactLecturerButton().getVisibility() == View.VISIBLE) {
            holder.getContactLecturerButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent theEmailIntent = new Intent(Intent.ACTION_SEND); //email intent
                    theEmailIntent.setType("text/plain");
                    theEmailIntent.putExtra(Intent.EXTRA_EMAIL, theModuleAtPosition.getTheLecturer().getEmailAddress());
                    theEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contacting The Lecturer");

                    //create chooser will open the popup required to select the app to launch the email
                    theContext.startActivity(Intent.createChooser(theEmailIntent, "Contact The Lecturer For This Module"));
                }
            });
        }

        if (holder.getScheduleLectureButton().getVisibility() == View.VISIBLE) {
            holder.getScheduleLectureButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //method to handle click of schedule lecture button
                    if (theModuleAtPosition.getTheBatchList().size() > 0) {
                        Intent createModuleIntent = new Intent(theContext, ScheduleLecture.class);
                        createModuleIntent.putExtra("theModuleId", theModuleAtPosition.getModuleId()); //put the module id as a string to intent so it can be accessed from next activity
                        theContext.startActivity(createModuleIntent);
                    } else {
                        Snackbar theSnackbar = Snackbar.make(v, "This module has no batches enrolled to it. Therefore, you cannot schedule any lectures for it", Snackbar.LENGTH_LONG);
                        theSnackbar.setBackgroundTint(theContext.getResources().getColor(R.color.btn_danger, null));
                        theSnackbar.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        //get size of data in the recycler view
        return moduleList.size();
    }

    public static class ModuleViewHolder extends RecyclerView.ViewHolder {
        //UI elements and everything for one module to be defined here.

        private MaterialTextView moduleName;
        private MaterialTextView taughtByLabel;
        private MaterialTextView lecturerName;
        private MaterialTextView creditCount;
        private MaterialTextView independentHours;
        private MaterialTextView contactLearning;
        private Button contactLecturerButton; //used by student
        private Button scheduleLectureButton; //used by lecturer

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            lecturerName = itemView.findViewById(R.id.taught_by);
            creditCount = itemView.findViewById(R.id.credit_count);
            independentHours = itemView.findViewById(R.id.independent);
            contactLearning = itemView.findViewById(R.id.contact_learning);
            moduleName = itemView.findViewById(R.id.module_title);
            contactLecturerButton = itemView.findViewById(R.id.contact_lecturer_button);
            taughtByLabel = itemView.findViewById(R.id.taught_by_label);
            scheduleLectureButton = itemView.findViewById(R.id.confirm_lecture_button);

            if (loggedInUser.getRole().equalsIgnoreCase("lecturer")) {
                //not a student, do not show contact lecturer and taught by
                //show schedule lecture btn
                contactLecturerButton.setVisibility(View.GONE);
                scheduleLectureButton.setVisibility(View.VISIBLE);
                lecturerName.setVisibility(View.GONE);
                taughtByLabel.setVisibility(View.GONE);
            } else if (loggedInUser.getRole().equalsIgnoreCase("student")) {
                contactLecturerButton.setVisibility(View.VISIBLE);
            }
        }

        public Button getScheduleLectureButton() {
            return scheduleLectureButton;
        }

        public Button getContactLecturerButton() {
            return contactLecturerButton;
        }

        public void setContactLecturerButton(Button contactLecturerButton) {
            this.contactLecturerButton = contactLecturerButton;
        }

        public MaterialTextView getModuleName() {
            return moduleName;
        }

        public void setModuleName(MaterialTextView moduleName) {
            this.moduleName = moduleName;
        }

        public MaterialTextView getLecturerName() {
            return lecturerName;
        }

        public void setLecturerName(MaterialTextView lecturerName) {
            this.lecturerName = lecturerName;
        }

        public MaterialTextView getCreditCount() {
            return creditCount;
        }

        public void setCreditCount(MaterialTextView creditCount) {
            this.creditCount = creditCount;
        }

        public MaterialTextView getIndependentHours() {
            return independentHours;
        }

        public void setIndependentHours(MaterialTextView independentHours) {
            this.independentHours = independentHours;
        }

        public MaterialTextView getContactLearning() {
            return contactLearning;
        }

        public void setContactLearning(MaterialTextView contactLearning) {
            this.contactLearning = contactLearning;
        }
    }
}