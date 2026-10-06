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

import com.example.foodresq.adapters.FoodDonationAdapter;
import com.example.foodresq.models.FoodDonation;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.FirebaseUtil;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyDonationsActivity extends AppCompatActivity {

    private Toolbar toolbarMyDonations;
    private ChipGroup chipGroupDonationFilter;
    private ProgressBar pbMyDonations;
    private TextView tvEmptyMyDonations;
    private RecyclerView rvMyDonations;
    private FloatingActionButton fabAddDonation;

    private FoodDonationAdapter donationAdapter;
    private final List<FoodDonation> allDonationsList = new ArrayList<>();
    private final List<FoodDonation> filteredDonationsList = new ArrayList<>();
    private String currentUserId;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_donations);

        currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId == null) {
            FirebaseUtil.logout(this);
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();

        fabAddDonation.setOnClickListener(v -> startActivity(new Intent(this, AddDonationActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyDonations();
    }

    private void initViews() {
        toolbarMyDonations = findViewById(R.id.toolbarMyDonations);
        chipGroupDonationFilter = findViewById(R.id.chipGroupDonationFilter);
        pbMyDonations = findViewById(R.id.pbMyDonations);
        tvEmptyMyDonations = findViewById(R.id.tvEmptyMyDonations);
        rvMyDonations = findViewById(R.id.rvMyDonations);
        fabAddDonation = findViewById(R.id.fabAddDonation);
    }

    private void setupToolbar() {
        toolbarMyDonations.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvMyDonations.setLayoutManager(new LinearLayoutManager(this));
        donationAdapter = new FoodDonationAdapter(this, filteredDonationsList, donation -> {
            Intent intent = new Intent(MyDonationsActivity.this, DonationDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_DONATION_ID, donation.getDonationId());
            startActivity(intent);
        });
        rvMyDonations.setAdapter(donationAdapter);
    }

    private void setupFilters() {
        chipGroupDonationFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipFilterAvailable) {
                currentFilter = Constants.DONATION_AVAILABLE;
            } else if (checkedId == R.id.chipFilterReserved) {
                currentFilter = Constants.DONATION_RESERVED;
            } else if (checkedId == R.id.chipFilterCompleted) {
                currentFilter = Constants.DONATION_COMPLETED;
            } else {
                currentFilter = "All";
            }
            applyFilter();
        });
    }

    private void loadMyDonations() {
        pbMyDonations.setVisibility(View.VISIBLE);
        tvEmptyMyDonations.setVisibility(View.GONE);

        FirebaseUtil.getDonationsCollection()
                .whereEqualTo("donorId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pbMyDonations.setVisibility(View.GONE);
                    allDonationsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodDonation donation = FoodDonation.fromSnapshot(doc);
                        if (donation != null) {
                            allDonationsList.add(donation);
                        }
                    }
                    // Sort descending by creation date
                    allDonationsList.sort((d1, d2) -> Long.compare(d2.getCreatedAt(), d1.getCreatedAt()));
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    pbMyDonations.setVisibility(View.GONE);
                    tvEmptyMyDonations.setText("Error loading donations: " + e.getMessage());
                    tvEmptyMyDonations.setVisibility(View.VISIBLE);
                });
    }

    private void applyFilter() {
        filteredDonationsList.clear();
        if ("All".equalsIgnoreCase(currentFilter)) {
            filteredDonationsList.addAll(allDonationsList);
        } else {
            for (FoodDonation donation : allDonationsList) {
                if (currentFilter.equalsIgnoreCase(donation.getStatus())) {
                    filteredDonationsList.add(donation);
                }
            }
        }

        donationAdapter.updateList(filteredDonationsList);

        if (filteredDonationsList.isEmpty()) {
            tvEmptyMyDonations.setText("No donations matching '" + currentFilter + "'");
            tvEmptyMyDonations.setVisibility(View.VISIBLE);
        } else {
            tvEmptyMyDonations.setVisibility(View.GONE);
        }
    }
}
