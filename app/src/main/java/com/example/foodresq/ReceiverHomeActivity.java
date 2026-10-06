package com.example.foodresq;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodresq.adapters.FoodDonationAdapter;
import com.example.foodresq.models.FoodDonation;
import com.example.foodresq.models.User;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.FirebaseUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReceiverHomeActivity extends AppCompatActivity {

    private TextView tvReceiverHeaderName, tvReceiverRequestsCount, tvEmptyReceiverDonations;
    private ImageButton btnReceiverProfile, btnReceiverLogout;
    private EditText etSearchDonations;
    private MaterialCardView cardReceiverMyRequests;
    private ChipGroup chipGroupFoodType;
    private RecyclerView rvReceiverAvailableDonations;
    private ProgressBar pbReceiverDonations;

    private FoodDonationAdapter donationAdapter;
    private final List<FoodDonation> allAvailableList = new ArrayList<>();
    private final List<FoodDonation> displayedList = new ArrayList<>();
    private String currentUserId;
    private String currentTypeFilter = "All";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_home);

        currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId == null) {
            FirebaseUtil.logout(this);
            return;
        }

        initViews();
        setupListeners();
        setupRecyclerView();
        setupFiltersAndSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReceiverProfile();
        loadAvailableDonations();
        loadMyRequestsCount();
    }

    private void initViews() {
        tvReceiverHeaderName = findViewById(R.id.tvReceiverHeaderName);
        tvReceiverRequestsCount = findViewById(R.id.tvReceiverRequestsCount);
        tvEmptyReceiverDonations = findViewById(R.id.tvEmptyReceiverDonations);
        btnReceiverProfile = findViewById(R.id.btnReceiverProfile);
        btnReceiverLogout = findViewById(R.id.btnReceiverLogout);
        etSearchDonations = findViewById(R.id.etSearchDonations);
        cardReceiverMyRequests = findViewById(R.id.cardReceiverMyRequests);
        chipGroupFoodType = findViewById(R.id.chipGroupFoodType);
        rvReceiverAvailableDonations = findViewById(R.id.rvReceiverAvailableDonations);
        pbReceiverDonations = findViewById(R.id.pbReceiverDonations);
    }

    private void setupListeners() {
        cardReceiverMyRequests.setOnClickListener(v -> startActivity(new Intent(this, MyRequestsActivity.class)));
        btnReceiverProfile.setOnClickListener(v -> startActivity(new Intent(this, ReceiverProfileActivity.class)));

        btnReceiverLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out of FoodResQ?")
                    .setPositiveButton("Logout", (dialog, which) -> FirebaseUtil.logout(this))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupRecyclerView() {
        rvReceiverAvailableDonations.setLayoutManager(new LinearLayoutManager(this));
        donationAdapter = new FoodDonationAdapter(this, displayedList, donation -> {
            Intent intent = new Intent(ReceiverHomeActivity.this, FoodDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_DONATION_ID, donation.getDonationId());
            startActivity(intent);
        });
        rvReceiverAvailableDonations.setAdapter(donationAdapter);
    }

    private void setupFiltersAndSearch() {
        chipGroupFoodType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipTypeCooked) {
                currentTypeFilter = "Cooked Meals";
            } else if (checkedId == R.id.chipTypeBakery) {
                currentTypeFilter = "Bakery & Bread";
            } else if (checkedId == R.id.chipTypeProduce) {
                currentTypeFilter = "Fresh Produce & Veggies";
            } else if (checkedId == R.id.chipTypePackaged) {
                currentTypeFilter = "Packaged / Canned Food";
            } else {
                currentTypeFilter = "All";
            }
            applyFilters();
        });

        etSearchDonations.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString().trim().toLowerCase() : "";
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadReceiverProfile() {
        FirebaseUtil.getUsersCollection().document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getName() != null) {
                            tvReceiverHeaderName.setText(user.getName());
                        }
                    }
                });
    }

    private void loadAvailableDonations() {
        pbReceiverDonations.setVisibility(View.VISIBLE);
        tvEmptyReceiverDonations.setVisibility(View.GONE);

        FirebaseUtil.getDonationsCollection()
                .whereEqualTo("status", Constants.DONATION_AVAILABLE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pbReceiverDonations.setVisibility(View.GONE);
                    allAvailableList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodDonation donation = FoodDonation.fromSnapshot(doc);
                        if (donation != null) {
                            allAvailableList.add(donation);
                        }
                    }
                    // Sort descending by creation date
                    allAvailableList.sort((d1, d2) -> Long.compare(d2.getCreatedAt(), d1.getCreatedAt()));
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    pbReceiverDonations.setVisibility(View.GONE);
                    tvEmptyReceiverDonations.setText("Error loading food donations: " + e.getMessage());
                    tvEmptyReceiverDonations.setVisibility(View.VISIBLE);
                });
    }

    private void applyFilters() {
        displayedList.clear();

        for (FoodDonation donation : allAvailableList) {
            // Category check
            boolean matchesCategory = "All".equalsIgnoreCase(currentTypeFilter)
                    || (donation.getFoodType() != null && donation.getFoodType().toLowerCase().contains(currentTypeFilter.toLowerCase()));

            // Search query check
            boolean matchesSearch = TextUtils.isEmpty(currentSearchQuery)
                    || (donation.getFoodName() != null && donation.getFoodName().toLowerCase().contains(currentSearchQuery))
                    || (donation.getPickupLocation() != null && donation.getPickupLocation().toLowerCase().contains(currentSearchQuery))
                    || (donation.getDescription() != null && donation.getDescription().toLowerCase().contains(currentSearchQuery));

            if (matchesCategory && matchesSearch) {
                displayedList.add(donation);
            }
        }

        donationAdapter.updateList(displayedList);

        if (displayedList.isEmpty()) {
            tvEmptyReceiverDonations.setVisibility(View.VISIBLE);
        } else {
            tvEmptyReceiverDonations.setVisibility(View.GONE);
        }
    }

    private void loadMyRequestsCount() {
        FirebaseUtil.getRequestsCollection()
                .whereEqualTo("receiverId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    if (count > 0) {
                        tvReceiverRequestsCount.setText(count + " active request" + (count == 1 ? "" : "s") + " • Tap to view");
                    } else {
                        tvReceiverRequestsCount.setText("Check approval & confirm pickup");
                    }
                });
    }
}
