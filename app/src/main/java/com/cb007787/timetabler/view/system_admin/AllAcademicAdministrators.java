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

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.ErrorResponseAPI;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllAcademicAdministrators extends Fragment {


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

    public AllAcademicAdministrators() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_all_academic_administrators, container, false);
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
        Call<List<User>> getAdminCall = userService.getAcademicAdmins(token);
        getAdminCall.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                progressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (response.isSuccessful()) {
                    loadedUsers = response.body();
                    adapter.setUserList(loadedUsers); //trigger notify data set changed to update recycler.
                    headerTitle.setText(String.format(Locale.ENGLISH, "Academic Administrators (%d)", adapter.getItemCount()));
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage());

                    } catch (IOException e) {
                        constructError("We ran into an error while loading the academic administrators");
                    }
                }
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


    private void constructError(String errorMessage) {
        Snackbar theSnackBar = Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG);
        theSnackBar.setAnchorView(recyclerView);
        theSnackBar.setBackgroundTint(getResources().getColor(R.color.btn_danger, null));
        theSnackBar.show();
    }
}