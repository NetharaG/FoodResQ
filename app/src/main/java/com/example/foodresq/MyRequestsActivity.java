package com.example.foodresq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodresq.adapters.ReceiverRequestAdapter;
import com.example.foodresq.models.FoodRequest;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.FirebaseUtil;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyRequestsActivity extends AppCompatActivity {

    private Toolbar toolbarMyRequests;
    private ChipGroup chipGroupMyRequestsFilter;
    private ProgressBar pbMyRequests;
    private TextView tvEmptyMyRequests;
    private RecyclerView rvMyRequests;

    private ReceiverRequestAdapter requestAdapter;
    private final List<FoodRequest> allRequestsList = new ArrayList<>();
    private final List<FoodRequest> filteredRequestsList = new ArrayList<>();
    private String currentUserId;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId == null) {
            FirebaseUtil.logout(this);
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyRequests();
    }

    private void initViews() {
        toolbarMyRequests = findViewById(R.id.toolbarMyRequests);
        chipGroupMyRequestsFilter = findViewById(R.id.chipGroupMyRequestsFilter);
        pbMyRequests = findViewById(R.id.pbMyRequests);
        tvEmptyMyRequests = findViewById(R.id.tvEmptyMyRequests);
        rvMyRequests = findViewById(R.id.rvMyRequests);
    }

    private void setupToolbar() {
        toolbarMyRequests.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvMyRequests.setLayoutManager(new LinearLayoutManager(this));
        requestAdapter = new ReceiverRequestAdapter(this, filteredRequestsList, request -> {
            Intent intent = new Intent(MyRequestsActivity.this, RequestDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_REQUEST_ID, request.getRequestId());
            startActivity(intent);
        });
        rvMyRequests.setAdapter(requestAdapter);
    }

    private void setupFilters() {
        chipGroupMyRequestsFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipMyReqPending) {
                currentFilter = Constants.REQUEST_PENDING;
            } else if (checkedId == R.id.chipMyReqAccepted) {
                currentFilter = Constants.REQUEST_ACCEPTED;
            } else if (checkedId == R.id.chipMyReqCollected) {
                currentFilter = Constants.REQUEST_COLLECTED;
            } else if (checkedId == R.id.chipMyReqRejected) {
                currentFilter = Constants.REQUEST_REJECTED;
            } else {
                currentFilter = "All";
            }
            applyFilter();
        });
    }

    private void loadMyRequests() {
        pbMyRequests.setVisibility(View.VISIBLE);
        tvEmptyMyRequests.setVisibility(View.GONE);

        FirebaseUtil.getRequestsCollection()
                .whereEqualTo("receiverId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pbMyRequests.setVisibility(View.GONE);
                    allRequestsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodRequest req = doc.toObject(FoodRequest.class);
                        allRequestsList.add(req);
                    }
                    // Sort descending by request time
                    allRequestsList.sort((r1, r2) -> Long.compare(r2.getRequestTime(), r1.getRequestTime()));
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    pbMyRequests.setVisibility(View.GONE);
                    tvEmptyMyRequests.setText("Error loading requests: " + e.getMessage());
                    tvEmptyMyRequests.setVisibility(View.VISIBLE);
                });
    }

    private void applyFilter() {
        filteredRequestsList.clear();
        if ("All".equalsIgnoreCase(currentFilter)) {
            filteredRequestsList.addAll(allRequestsList);
        } else {
            for (FoodRequest req : allRequestsList) {
                if (currentFilter.equalsIgnoreCase(req.getStatus())) {
                    filteredRequestsList.add(req);
                }
            }
        }

        requestAdapter.updateList(filteredRequestsList);

        if (filteredRequestsList.isEmpty()) {
            tvEmptyMyRequests.setText("No requests found matching '" + currentFilter + "'");
            tvEmptyMyRequests.setVisibility(View.VISIBLE);
        } else {
            tvEmptyMyRequests.setVisibility(View.GONE);
        }
    }
}
