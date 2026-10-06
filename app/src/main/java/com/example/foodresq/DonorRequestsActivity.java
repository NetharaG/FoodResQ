package com.example.foodresq;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodresq.adapters.DonorRequestAdapter;
import com.example.foodresq.models.FoodRequest;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.FirebaseUtil;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DonorRequestsActivity extends AppCompatActivity implements DonorRequestAdapter.OnRequestActionListener {

    private Toolbar toolbarDonorRequests;
    private ChipGroup chipGroupRequestsFilter;
    private ProgressBar pbDonorRequests;
    private TextView tvEmptyDonorRequests;
    private RecyclerView rvDonorRequests;

    private DonorRequestAdapter requestAdapter;
    private final List<FoodRequest> allRequestsList = new ArrayList<>();
    private final List<FoodRequest> filteredRequestsList = new ArrayList<>();
    private String currentUserId;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_requests);

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
        loadRequests();
    }

    private void initViews() {
        toolbarDonorRequests = findViewById(R.id.toolbarDonorRequests);
        chipGroupRequestsFilter = findViewById(R.id.chipGroupRequestsFilter);
        pbDonorRequests = findViewById(R.id.pbDonorRequests);
        tvEmptyDonorRequests = findViewById(R.id.tvEmptyDonorRequests);
        rvDonorRequests = findViewById(R.id.rvDonorRequests);
    }

    private void setupToolbar() {
        toolbarDonorRequests.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvDonorRequests.setLayoutManager(new LinearLayoutManager(this));
        requestAdapter = new DonorRequestAdapter(this, filteredRequestsList, this);
        rvDonorRequests.setAdapter(requestAdapter);
    }

    private void setupFilters() {
        chipGroupRequestsFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipReqPending) {
                currentFilter = Constants.REQUEST_PENDING;
            } else if (checkedId == R.id.chipReqAccepted) {
                currentFilter = Constants.REQUEST_ACCEPTED;
            } else if (checkedId == R.id.chipReqCollected) {
                currentFilter = Constants.REQUEST_COLLECTED;
            } else if (checkedId == R.id.chipReqRejected) {
                currentFilter = Constants.REQUEST_REJECTED;
            } else {
                currentFilter = "All";
            }
            applyFilter();
        });
    }

    private void loadRequests() {
        pbDonorRequests.setVisibility(View.VISIBLE);
        tvEmptyDonorRequests.setVisibility(View.GONE);

        FirebaseUtil.getRequestsCollection()
                .whereEqualTo("donorId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pbDonorRequests.setVisibility(View.GONE);
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
                    pbDonorRequests.setVisibility(View.GONE);
                    tvEmptyDonorRequests.setText("Error loading requests: " + e.getMessage());
                    tvEmptyDonorRequests.setVisibility(View.VISIBLE);
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
            tvEmptyDonorRequests.setText("No requests found matching '" + currentFilter + "'");
            tvEmptyDonorRequests.setVisibility(View.VISIBLE);
        } else {
            tvEmptyDonorRequests.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAccept(FoodRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Accept Food Request")
                .setMessage("Accept request from " + request.getReceiverName() + " for " + request.getFoodName() + "?\nThis will mark the food as Reserved.")
                .setPositiveButton("Accept", (dialog, which) -> {
                    // Update request status to Accepted
                    FirebaseUtil.getRequestsCollection().document(request.getRequestId())
                            .update("status", Constants.REQUEST_ACCEPTED)
                            .addOnSuccessListener(aVoid -> {
                                // Update donation status to Reserved
                                if (!TextUtils.isEmpty(request.getDonationId())) {
                                    FirebaseUtil.getDonationsCollection().document(request.getDonationId())
                                            .update("status", Constants.DONATION_RESERVED);
                                }
                                Toast.makeText(DonorRequestsActivity.this, "Request accepted! Donation marked as Reserved.", Toast.LENGTH_SHORT).show();
                                loadRequests();
                            })
                            .addOnFailureListener(e -> Toast.makeText(DonorRequestsActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onReject(FoodRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Reject Food Request")
                .setMessage("Decline this request from " + request.getReceiverName() + "?")
                .setPositiveButton("Reject", (dialog, which) -> {
                    FirebaseUtil.getRequestsCollection().document(request.getRequestId())
                            .update("status", Constants.REQUEST_REJECTED)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(DonorRequestsActivity.this, "Request rejected", Toast.LENGTH_SHORT).show();
                                loadRequests();
                            })
                            .addOnFailureListener(e -> Toast.makeText(DonorRequestsActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
