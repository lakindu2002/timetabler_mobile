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
import com.cb007787.timetabler.model.AuthReturn;
import com.cb007787.timetabler.model.BatchShow;
import com.cb007787.timetabler.model.ErrorResponseAPI;
import com.cb007787.timetabler.model.User;
import com.cb007787.timetabler.recyclers.BatchRecycler;
import com.cb007787.timetabler.recyclers.UserRecycler;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.BatchService;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.service.UserService;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademicAdminLectureView extends Fragment {

    private BatchService batchService;
    private UserService userService;
    private String token;
    private String loadType;

    private LinearProgressIndicator progressIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private BatchRecycler batchAdapter;

    public AcademicAdminLectureView() {
    }

    public static AcademicAdminLectureView newInstance(String type) {
        Bundle args = new Bundle();
        args.putString("type", type);
        AcademicAdminLectureView fragment = new AcademicAdminLectureView();
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
        View inflatedView = inflater.inflate(R.layout.fragment_academic_admin_lecture_view, container, false);

        token = SharedPreferenceService.getToken(requireContext(), PreferenceInformation.PREFERENCE_NAME);
        batchService = APIConfigurer.getApiConfigurer().getBatchService();
        userService = APIConfigurer.getApiConfigurer().getUserService();
        progressIndicator = inflatedView.findViewById(R.id.progress_bar);
        swipeRefreshLayout = inflatedView.findViewById(R.id.swiper);
        recyclerView = inflatedView.findViewById(R.id.recycler);

        swipeRefreshLayout.setOnRefreshListener(this::loadData);


        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        batchAdapter = new BatchRecycler(requireContext());


        //default load type to batch.
        loadType = getArguments() != null ? getArguments().getString("type") : "batch";
        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
    }

    public void loadData() {
        if (loadType.equalsIgnoreCase("batch")) {
            batchAdapter.setBatchList(new ArrayList<>());
            batchAdapter.setTimetableComponent(true);
            recyclerView.setAdapter(batchAdapter);
            loadBatchesWithLectures();
        } else {
            //load all lectures
            loadAllLecturers();
        }
    }

    private void loadAllLecturers() {
        progressIndicator.setVisibility(View.VISIBLE);
        userService.getAllLecturers(token).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                progressIndicator.setVisibility(View.GONE);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful()) {

                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while loading all lecturers", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                constructError("We ran into an error while loading all lecturers", false);
            }
        });
    }

    private void loadBatchesWithLectures() {
        progressIndicator.setVisibility(View.VISIBLE);
        batchService.getAllBatchesWithLectures(token).enqueue(new Callback<List<BatchShow>>() {
            @Override
            public void onResponse(@NonNull Call<List<BatchShow>> call, @NonNull Response<List<BatchShow>> response) {
                progressIndicator.setVisibility(View.GONE);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful()) {
                    batchAdapter.setBatchList(response.body());
                    if (response.body().size() == 0) {
                        constructError("There are no batches with lectures schedule", true);
                    }
                } else {
                    try {
                        ErrorResponseAPI theErrorReturned = APIConfigurer.getTheErrorReturned(response.errorBody());
                        constructError(theErrorReturned.getErrorMessage(), false);
                    } catch (IOException e) {
                        constructError("We ran into an error while loading all batches with lectures", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BatchShow>> call, @NonNull Throwable t) {
                progressIndicator.setVisibility(View.GONE);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                constructError("We ran into an error while loading all batches with lectures", false);
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