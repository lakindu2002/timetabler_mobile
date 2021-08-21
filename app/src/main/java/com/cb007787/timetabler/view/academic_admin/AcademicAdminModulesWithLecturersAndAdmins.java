package com.cb007787.timetabler.view.academic_admin;

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

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.Module;
import com.cb007787.timetabler.recyclers.ModuleListWithBatchesAndLecturersAdapter;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.LectureService;
import com.cb007787.timetabler.service.ModuleService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademicAdminModulesWithLecturersAndAdmins extends Fragment {

    private ModuleService moduleService;
    private String token;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearProgressIndicator progressIndicator;
    private List<Module> loadedModules = new ArrayList<>();
    private ModuleListWithBatchesAndLecturersAdapter adapter;

    public AcademicAdminModulesWithLecturersAndAdmins() {
    }

    public static AcademicAdminModulesWithLecturersAndAdmins newInstance() {
        AcademicAdminModulesWithLecturersAndAdmins fragment = new AcademicAdminModulesWithLecturersAndAdmins();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_academic_admin_modules_with_lecturers_and_admins, container, false);

        SharedPreferenceService.validateToken(requireContext(), PreferenceInformation.PREFERENCE_NAME);

        moduleService = APIConfigurer.getApiConfigurer().getModuleService();
        token = SharedPreferenceService.getToken(requireContext(), PreferenceInformation.PREFERENCE_NAME);
        recyclerView = inflatedView.findViewById(R.id.recycler);
        swipeRefreshLayout = inflatedView.findViewById(R.id.swiper);
        progressIndicator = inflatedView.findViewById(R.id.progress_bar);

        adapter = new ModuleListWithBatchesAndLecturersAdapter(requireContext());
        adapter.setModuleList(loadedModules);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::getModulesWithBatchesAndLecturers);
        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getModulesWithBatchesAndLecturers();
    }

    public void getModulesWithBatchesAndLecturers() {
        progressIndicator.setVisibility(View.VISIBLE);
        moduleService.getAllModulesWithBatchesAndLecturers(token).enqueue(new Callback<List<Module>>() {
            @Override
            public void onResponse(@NonNull Call<List<Module>> call, @NonNull Response<List<Module>> response) {
                progressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful()) {
                    loadedModules = response.body();
                    adapter.setModuleList(loadedModules);
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while loading the modules for scheduling", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Module>> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                constructError("We ran into an error while loading the modules for scheduling", false);
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
}