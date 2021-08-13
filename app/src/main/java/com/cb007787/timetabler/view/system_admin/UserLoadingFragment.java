package com.cb007787.timetabler.view.system_admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.model.User;
import com.cb007787.timetabler.recyclers.UserRecycler;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserLoadingFragment extends Fragment {


    private LinearProgressIndicator progressIndicator;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private UserRecycler adapter;
    private String token;
    private UserService userService;
    private List<User> loadedUsers = new ArrayList<>();
    private AuthReturn loggedInUser;
    private MaterialTextView headerTitle;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String userListToLoad;

    public UserLoadingFragment() {
    }

    public void search(String input) {
        //method executed to search the data.
        if (adapter != null) {
            input = input.trim();
            if (input.length() != 0) {
                String finalInput = input;
                List<User> filteredList = loadedUsers.stream().filter((eachUser) -> eachUser.getUsername().toLowerCase().contains(finalInput.toLowerCase())).collect(Collectors.toList());
                adapter.setUserList(filteredList);
            } else {
                adapter.setUserList(loadedUsers); //notify data set changed
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static UserLoadingFragment newInstance(String loadingType) {

        Bundle args = new Bundle();

        UserLoadingFragment fragment = new UserLoadingFragment();
        args.putString("loadingType", loadingType.toUpperCase());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            //retrieve the user type to be loaded that was passed from the SystemAdminUserManagement activity.
            userListToLoad = arguments.getString("loadingType", "academic_administrator");
        }

        //inflate the layout for the fragment
        View inflatedView = inflater.inflate(R.layout.fragment_user_loading, container, false);
        try {
            getReferences(inflatedView);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        layoutManager = new LinearLayoutManager(requireContext()); //create a linear layout manager for recycler view
        adapter = new UserRecycler(requireContext(), loggedInUser.getRole()); //construct the adapter for the recycler
        adapter.setUserList(loadedUsers); //assign user list (notify data set will get called allowing auto update)
        recyclerView.setLayoutManager(layoutManager); //set layout for recycler
        recyclerView.setAdapter(adapter); //set adapter for recycler


        //swiped
        swipeRefreshLayout.setOnRefreshListener(this::loadUsers);

        //assign the callback events to the adapter
        adapter.setOnDeleteCallbacks(new UserRecycler.DeleteCallbacks() {
            @Override
            public void onDeleteSuccessResponse(SuccessResponseAPI theSuccessObject) {
                //user has been deleted successfully
                Snackbar theSnackBar = Snackbar.make(requireView(), theSuccessObject.getMessage(), Snackbar.LENGTH_LONG);
                theSnackBar.setAnchorView(recyclerView);
                theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_success, null));
                theSnackBar.show();
                progressIndicator.setVisibility(View.GONE); //hide the progress bar

                loadUsers(); //load the users for the inflated user type in the bundle to refresh the list.
            }

            @Override
            public void onDeleteFailure(String message) {
                //user has not been deleted
                constructError(message);
                progressIndicator.setVisibility(View.GONE); //hide the progress bar
            }

            @Override
            public void onDeleteCalled() {
                //the user has click "Delete"
                //display the loading bar when user clicks delete.
                progressIndicator.setVisibility(View.VISIBLE);
            }
        });

        return inflatedView; //return the view that can be hosted on the fragment container
    }

    private void getReferences(View inflatedView) throws JsonProcessingException {
        token = SharedPreferenceService.getToken(requireContext(), PreferenceInformation.PREFERENCE_NAME);
        loggedInUser = SharedPreferenceService.getLoggedInUser(requireContext(), PreferenceInformation.PREFERENCE_NAME);
        userService = APIConfigurer.getApiConfigurer().getUserService();
        progressIndicator = inflatedView.findViewById(R.id.progress_bar);
        recyclerView = inflatedView.findViewById(R.id.recycler);
        headerTitle = inflatedView.findViewById(R.id.header);
        swipeRefreshLayout = inflatedView.findViewById(R.id.swiper);
    }


    @Override
    public void onStart() {
        super.onStart();
        loadUsers(); //load the academic admins on the initial fragment inflation
    }

    private void loadUsers() {
        //api call
        progressIndicator.setVisibility(View.VISIBLE);
        //load the users based on the type selected from the bottom navigation view.
        if (userListToLoad.equalsIgnoreCase("ACADEMIC_ADMINISTRATOR")) {
            headerTitle.setText("Academic Administrators");
            loadAcademicAdminList();
        } else if (userListToLoad.equalsIgnoreCase("STUDENT")) {
            headerTitle.setText("Students");
            loadStudentList();
        } else if (userListToLoad.equalsIgnoreCase("LECTURER")) {
            headerTitle.setText("Lecturers");
            loadLecturers();
        }
    }


    private void loadAcademicAdminList() {
        Call<List<User>> getAdminCall = userService.getAcademicAdmins(token);
        getAdminCall.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                handleOnResponse(response);
                headerTitle.setText(String.format(Locale.ENGLISH, "Academic Administrators (%d)", adapter.getItemCount()));
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                constructError("We ran into an error while loading the academic administrators");
            }
        });
    }


    private void loadStudentList() {
        Call<List<User>> allStudents = userService.getAllStudents(token);
        allStudents.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                handleOnResponse(response);
                headerTitle.setText(String.format(Locale.ENGLISH, "Students (%d)", adapter.getItemCount()));
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                constructError("We ran into an error while loading the students");
            }
        });
    }

    private void loadLecturers() {
        Call<List<User>> allLecturers = userService.getAllLecturers(token);
        allLecturers.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                handleOnResponse(response);
                headerTitle.setText(String.format(Locale.ENGLISH, "Lecturers (%d)", adapter.getItemCount()));
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                constructError("We ran into an error while loading the lecturers");
            }
        });
    }

    private void handleOnResponse(Response<List<User>> response) {
        progressIndicator.setVisibility(View.GONE);
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        if (response.isSuccessful()) {
            loadedUsers = response.body();
            adapter.setUserList(loadedUsers); //trigger notify data set changed to update recycler.
        } else {
            try {
                ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                constructError(theErrorReturned.getErrorMessage());

            } catch (IOException e) {
                e.printStackTrace();
                //construct error messages based on the role needed to be loaded.
                if (userListToLoad.equalsIgnoreCase("ACADEMIC_ADMINISTRATOR")) {
                    constructError("We ran into an error while loading the academic administrators");
                } else if (userListToLoad.equalsIgnoreCase("STUDENT")) {
                    constructError("We ran into an error while loading the students");
                } else if (userListToLoad.equalsIgnoreCase("LECTURER")) {
                    constructError("We ran into an error while loading the lecturers");
                }
            }
        }
    }


    private void constructError(String errorMessage) {
        if (getView() != null) {
            Snackbar theSnackBar = Snackbar.make(getView(), errorMessage, Snackbar.LENGTH_LONG);
            theSnackBar.setAnchorView(recyclerView);
            theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
            View view = theSnackBar.getView();
            //retrieve the underling text view on the snack bar and increase the lines on it to display full message
            TextView snackBarText = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
            snackBarText.setMaxLines(5);
            theSnackBar.show();
        }
    }
}