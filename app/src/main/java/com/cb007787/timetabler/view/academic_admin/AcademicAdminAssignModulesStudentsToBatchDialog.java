package com.cb007787.timetabler.view.academic_admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.model.User;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.BatchService;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.service.UserService;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dialog used to assign modules and students to a batch
 */
public class AcademicAdminAssignModulesStudentsToBatchDialog extends DialogFragment {

    private String token;
    private BatchService batchService;
    private UserService userService;
    private ModuleService moduleService;
    private String loadingType = "";
    private String batchCode;
    private List<Module> modulesFromDb = new ArrayList<>();
    private List<User> studentsFromDb = new ArrayList<>();

    private ImageView backButton;
    private TextView toolbarHeader;
    private LinearProgressIndicator linearProgressIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout linearLayout;
    private TextView typeHeader;
    private SearchView search;
    private Button assignBtn;

    private List<String> assigningStudents = new ArrayList<>();
    private List<String> assigningModules = new ArrayList<>();

    public static AcademicAdminAssignModulesStudentsToBatchDialog newInstance(String loadingType, String batchCode) {
        Bundle args = new Bundle();
        args.putString("loadingType", loadingType);
        args.putString("batchCode", batchCode);
        AcademicAdminAssignModulesStudentsToBatchDialog fragment = new AcademicAdminAssignModulesStudentsToBatchDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyles); //assign custom dialog styles declared in styles.
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            loadingType = arguments.getString("loadingType");
            batchCode = arguments.getString("batchCode");
        }

        //validate jwt token
        SharedPreferenceService.validateToken(requireContext(), PreferenceInformation.PREFERENCE_NAME);
        token = SharedPreferenceService.getToken(requireContext(), PreferenceInformation.PREFERENCE_NAME);
        moduleService = APIConfigurer.getApiConfigurer().getModuleService();
        batchService = APIConfigurer.getApiConfigurer().getBatchService();
        userService = APIConfigurer.getApiConfigurer().getUserService();

        //inflate the view for this dialog
        View inflatedView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_academic_admin_assign_modules_students_to_batch,
                container, false);

        backButton = inflatedView.findViewById(R.id.back_button);
        toolbarHeader = inflatedView.findViewById(R.id.custom_toolbar_text);
        linearProgressIndicator = inflatedView.findViewById(R.id.progress_bar);
        swipeRefreshLayout = inflatedView.findViewById(R.id.swiper);
        linearLayout = inflatedView.findViewById(R.id.check_boxes);
        typeHeader = inflatedView.findViewById(R.id.type_header);
        search = inflatedView.findViewById(R.id.search_field);
        assignBtn = inflatedView.findViewById(R.id.assign_btn);

        backButton.setOnClickListener(v -> {
            dismiss(); //dismiss dialog
        });


        swipeRefreshLayout.setOnRefreshListener(() -> {
            //swipe to refresh.
            linearLayout.removeAllViews();
            if ("assign-modules".equals(loadingType.toLowerCase())) {
                executeAssignModulesToBatch();
            } else {
                executeAssignStudentsToBatch();
            }
        });

        assignBtn.setOnClickListener(v -> {
            //when user clicks assign, depending on module or student, execute DB operation
            if (loadingType.equalsIgnoreCase("assign-modules")) {
                assignModulesToBatchInDb();
            } else {
                assignStudentsToBatchInDb();
            }
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //execute when user click search
                query = query.trim().toLowerCase();
                String finalQuery = query;
                if (loadingType.equalsIgnoreCase("assign-modules")) {
                    if (query.length() > 0) {
                        List<Module> filtered = modulesFromDb.stream().filter((module -> module.getModuleName().toLowerCase().contains(finalQuery))).collect(Collectors.toList());
                        showCheckBoxes(studentsFromDb, filtered);
                    } else {
                        showCheckBoxes(studentsFromDb, modulesFromDb);
                    }
                } else {
                    if (query.length() > 0) {
                        List<User> filtered = studentsFromDb.stream().filter((student -> student.getUsername().toLowerCase().contains(finalQuery))).collect(Collectors.toList());
                        showCheckBoxes(filtered, modulesFromDb);
                    } else {
                        showCheckBoxes(studentsFromDb, modulesFromDb);
                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.trim().toLowerCase();
                if (newText.length() == 0) {
                    showCheckBoxes(studentsFromDb, modulesFromDb);
                }
                return true;
            }
        });

        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if ("assign-modules".equals(loadingType.toLowerCase())) {
            toolbarHeader.setText(String.format("Assign Modules - %s", batchCode));
            executeAssignModulesToBatch();
            typeHeader.setText("Assign Modules To Batch");
            search.setQueryHint("Provide Module Name");
            assignBtn.setText("Assign Modules To Batch");
        } else {
            toolbarHeader.setText(String.format("Assign Students - %s", batchCode));
            executeAssignStudentsToBatch();
            search.setQueryHint("Provide Student Username");
            typeHeader.setText("Assign Students To Batch");
            assignBtn.setText("Assign Students To Batch");
        }
    }

    private void executeAssignStudentsToBatch() {
        //load the students that can be assigned to the batch
        linearProgressIndicator.setVisibility(View.VISIBLE);
        userService.getStudentsWithoutABatch(token).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                //response sent from api
                linearProgressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful()) {
                    //successfully loaded
                    studentsFromDb = response.body();
                    if (studentsFromDb.size() == 0) {
                        constructError("All students at TimeTabler have been assigned to a batch", false);
                    } else {
                        showCheckBoxes(studentsFromDb, modulesFromDb);
                    }
                } else {
                    //error
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while retrieving the students with no batch", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                //response did not go or failed to parse response.
                linearProgressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                constructError("We ran into an error while retrieving the students with no batch", false);
            }
        });
    }

    private void executeAssignModulesToBatch() {
        //retrieve all modules that can be assigned to the batcj
        linearProgressIndicator.setVisibility(View.VISIBLE);
        batchService.getModulesNotInBatch(batchCode, token).enqueue(new Callback<List<Module>>() {
            @Override
            public void onResponse(@NonNull Call<List<Module>> call, @NonNull Response<List<Module>> response) {
                linearProgressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful()) {
                    //successful
                    modulesFromDb = response.body();
                    if (modulesFromDb.size() == 0) {
                        constructError("All modules at TimeTabler have been assigned to this batch", false);
                    } else {
                        showCheckBoxes(studentsFromDb, modulesFromDb);
                    }
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while retrieving the modules to assign to this batch", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Module>> call, @NonNull Throwable t) {
                linearProgressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                constructError("We ran into an error while retrieving the modules to assign to this batch", false);
            }
        });
    }

    private void constructError(String errorMessage, boolean isInfo) {
        Snackbar theSnackBar = Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG);
        if (isInfo) {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_info, null));
        } else {
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        }
        View view = theSnackBar.getView();
        //retrieve the underling text view on the snack bar and increase the lines on it to display full message
        TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
        snackBarText.setMaxLines(5);
        theSnackBar.show();
    }

    private void showCheckBoxes(List<User> userList, List<Module> moduleList) {
        linearLayout.removeAllViewsInLayout();
        //show the select checkboxes for the user based on type so assigning operation to the batch can be done.
        if (loadingType.toLowerCase().equalsIgnoreCase("assign-modules")) {
            //show modules
            for (Module eachModule : moduleList) {
                CheckBox eachBox = new CheckBox(requireContext());
                eachBox.setPadding(0, 20, 0, 0);
                eachBox.setTextSize(15);
                eachBox.setText(String.format("%s & Taught By - %s %s", eachModule.getModuleName(), eachModule.getTheLecturer().getFirstName(), eachModule.getTheLecturer().getLastName()));

                eachBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    //when user checks, unchecks.
                    if (isChecked) {
                        if (!assigningModules.contains(String.valueOf(eachModule.getModuleId()))) {
                            //if the assigning array does not already have the module, add it
                            insertModule(eachModule.getModuleId());
                        }
                    } else {
                        if (assigningModules.contains(String.valueOf(eachModule.getModuleId()))) {
                            //if array has module only, remove it.
                            removeModule(eachModule.getModuleId());
                        }
                    }
                });

                if (assigningModules.contains(String.valueOf(eachModule.getModuleId()))) {
                    //if the assigning list already has the module, set checkbox to true
                    eachBox.setChecked(true);
                } else {
                    eachBox.setChecked(false);
                }

                linearLayout.addView(eachBox);
            }
        } else {
            //show students.
            for (User eachUser : userList) {
                CheckBox eachBox = new CheckBox(requireContext());
                eachBox.setText(String.format("%s (%s %s)", eachUser.getUsername(), eachUser.getFirstName(), eachUser.getLastName()));
                eachBox.setPadding(0, 20, 0, 0);
                eachBox.setTextSize(15);

                eachBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    //when user checks or unchecks.
                    if (isChecked) {
                        if (!assigningStudents.contains(eachUser.getUsername())) {
                            //if array has the student, do not add again
                            insertStudent(eachUser.getUsername());
                        }
                    } else {
                        if (assigningStudents.contains(eachUser.getUsername())) {
                            //if array only has the student, remove
                            removeStudent(eachUser.getUsername());
                        }
                    }
                });

                if (assigningStudents.contains(eachUser.getUsername())) {
                    //if condition on onCheckedChange is added because setting changed here triggers the onCheckChanged listener
                    eachBox.setChecked(true);
                } else {
                    eachBox.setChecked(false);
                }

                linearLayout.addView(eachBox);
            }
        }
    }

    private void insertModule(int moduleId) {
        assigningModules.add(String.valueOf(moduleId));
    }

    private void removeModule(int moduleId) {
        assigningModules.remove(String.valueOf(moduleId));
    }

    private void insertStudent(String username) {
        assigningStudents.add(username);
    }

    private void removeStudent(String username) {
        assigningStudents.remove(username);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        dialog.dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        dialog.cancel();
    }

    private void assignModulesToBatchInDb() {
        //assign the modules to the batch in the database.
        linearProgressIndicator.setVisibility(View.VISIBLE);
        if (assigningModules.size() == 0) {
            //if none selected, display error
            constructError("Please select modules to assign to this batch", false);
            linearProgressIndicator.setVisibility(View.GONE);
        } else {
            //pass a string array to backend
            String[] moduleIdList = assigningModules.toArray(new String[assigningModules.size()]);
            HashMap<String, String[]> reqBody = new HashMap<>();
            reqBody.put("moduleList", moduleIdList); //hashmap contains req body

            batchService.assignModulesToBatch(reqBody, batchCode, token).enqueue(new Callback<SuccessResponseAPI>() {
                @Override
                public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                    linearProgressIndicator.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        //successfully assigned modules to batch,
                        //show success message and navigate back to the batch management class
                        Toast.makeText(requireContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                        requireContext().startActivity(new Intent(requireActivity(), AcademicAdministratorBatchManagement.class));
                        requireActivity().finish(); //do not allow accessing this dialog on back button
                    } else {
                        try {
                            ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                            constructError(theErrorReturned.getErrorMessage(), false);
                        } catch (IOException e) {
                            constructError("We ran into an error while assigning the modules to the batch", false);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                    linearProgressIndicator.setVisibility(View.GONE);
                    constructError("We ran into an error while assigning the modules to the batch", false);
                }
            });
        }
    }

    private void assignStudentsToBatchInDb() {
        //assign students to batch in the DB
        linearProgressIndicator.setVisibility(View.VISIBLE);
        if (assigningStudents.size() == 0) {
            //if no students are selected, show an error
            constructError("Please select students to assign to this batch", false);
            linearProgressIndicator.setVisibility(View.GONE);
        } else {
            //pass string array in a hashmap as the req body
            String[] studentList = assigningStudents.toArray(new String[assigningStudents.size()]);
            HashMap<String, String[]> reqBody = new HashMap<>();
            reqBody.put("students", studentList);

            batchService.assignStudentsToBatch(reqBody, batchCode, token).enqueue(new Callback<SuccessResponseAPI>() {
                @Override
                public void onResponse(@NonNull Call<SuccessResponseAPI> call, @NonNull Response<SuccessResponseAPI> response) {
                    linearProgressIndicator.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        //students successfully assigned to the batch
                        Toast.makeText(requireContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                        requireContext().startActivity(new Intent(requireActivity(), AcademicAdministratorBatchManagement.class));
                        requireActivity().finish();
                    } else {
                        try {
                            ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                            constructError(theErrorReturned.getErrorMessage(), false);
                        } catch (IOException e) {
                            constructError("We ran into an error while assigning the students to the batch", false);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SuccessResponseAPI> call, @NonNull Throwable t) {
                    linearProgressIndicator.setVisibility(View.GONE);
                    constructError("We ran into an error while assigning the students to the batch", false);
                }
            });
        }
    }
}
