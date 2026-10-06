package com.example.foodresq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DonorHomeActivity extends AppCompatActivity {

    private TextView tvDonorName, tvTotalDonationsCount, tvPendingRequestsCount, tvViewAllDonations, tvEmptyRecentDonations;
    private ImageButton btnDonorProfile, btnDonorLogout;
    private MaterialCardView cardAddDonation, cardMyDonations, cardIncomingRequests;
    private RecyclerView rvRecentDonations;
    private ProgressBar pbRecentDonations;

    private FoodDonationAdapter donationAdapter;
    private final List<FoodDonation> recentDonationList = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_home);

        currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId == null) {
            FirebaseUtil.logout(this);
            return;
        }

        initViews();
        setupListeners();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDonorProfile();
        loadRecentDonations();
        loadCounts();
    }

    private void initViews() {
        tvDonorName = findViewById(R.id.tvDonorName);
        tvTotalDonationsCount = findViewById(R.id.tvTotalDonationsCount);
        tvPendingRequestsCount = findViewById(R.id.tvPendingRequestsCount);
        tvViewAllDonations = findViewById(R.id.tvViewAllDonations);
        tvEmptyRecentDonations = findViewById(R.id.tvEmptyRecentDonations);
        btnDonorProfile = findViewById(R.id.btnDonorProfile);
        btnDonorLogout = findViewById(R.id.btnDonorLogout);
        cardAddDonation = findViewById(R.id.cardAddDonation);
        cardMyDonations = findViewById(R.id.cardMyDonations);
        cardIncomingRequests = findViewById(R.id.cardIncomingRequests);
        rvRecentDonations = findViewById(R.id.rvRecentDonations);
        pbRecentDonations = findViewById(R.id.pbRecentDonations);
    }

    private void setupListeners() {
        cardAddDonation.setOnClickListener(v -> startActivity(new Intent(this, AddDonationActivity.class)));
        cardMyDonations.setOnClickListener(v -> startActivity(new Intent(this, MyDonationsActivity.class)));
        tvViewAllDonations.setOnClickListener(v -> startActivity(new Intent(this, MyDonationsActivity.class)));
        cardIncomingRequests.setOnClickListener(v -> startActivity(new Intent(this, DonorRequestsActivity.class)));
        btnDonorProfile.setOnClickListener(v -> startActivity(new Intent(this, DonorProfileActivity.class)));

        btnDonorLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out of FoodResQ?")
                    .setPositiveButton("Logout", (dialog, which) -> FirebaseUtil.logout(this))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupRecyclerView() {
        rvRecentDonations.setLayoutManager(new LinearLayoutManager(this));
        donationAdapter = new FoodDonationAdapter(this, recentDonationList, donation -> {
            Intent intent = new Intent(DonorHomeActivity.this, DonationDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_DONATION_ID, donation.getDonationId());
            startActivity(intent);
        });
        rvRecentDonations.setAdapter(donationAdapter);
    }

    private void loadDonorProfile() {
        FirebaseUtil.getUsersCollection().document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getName() != null) {
                            tvDonorName.setText(user.getName());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Ignored, user might be offline
                });
    }

    private void loadRecentDonations() {
        pbRecentDonations.setVisibility(View.VISIBLE);
        tvEmptyRecentDonations.setVisibility(View.GONE);

        FirebaseUtil.getDonationsCollection()
                .whereEqualTo("donorId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pbRecentDonations.setVisibility(View.GONE);
                    recentDonationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodDonation donation = FoodDonation.fromSnapshot(doc);
                        if (donation != null) {
                            recentDonationList.add(donation);
                        }
                    }
                    donationAdapter.updateList(recentDonationList);

                    if (recentDonationList.isEmpty()) {
                        tvEmptyRecentDonations.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyRecentDonations.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    pbRecentDonations.setVisibility(View.GONE);
                    // In case an index is building or composite query fails, fallback to simple query without orderBy
                    loadDonationsFallback();
                });
    }

    private void loadDonationsFallback() {
        FirebaseUtil.getDonationsCollection()
                .whereEqualTo("donorId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pbRecentDonations.setVisibility(View.GONE);
                    recentDonationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodDonation donation = FoodDonation.fromSnapshot(doc);
                        if (donation != null) {
                            recentDonationList.add(donation);
                        }
                    }
                    // Sort locally
                    recentDonationList.sort((d1, d2) -> Long.compare(d2.getCreatedAt(), d1.getCreatedAt()));
                    donationAdapter.updateList(recentDonationList);

                    if (recentDonationList.isEmpty()) {
                        tvEmptyRecentDonations.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyRecentDonations.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    pbRecentDonations.setVisibility(View.GONE);
                    tvEmptyRecentDonations.setVisibility(View.VISIBLE);
                });
    }

    private void loadCounts() {
        // Total donations count
        FirebaseUtil.getDonationsCollection()
                .whereEqualTo("donorId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = queryDocumentSnapshots.size();
                    tvTotalDonationsCount.setText(total + " item" + (total == 1 ? "" : "s") + " posted");
                });

        // Pending requests count
        FirebaseUtil.getRequestsCollection()
                .whereEqualTo("donorId", currentUserId)
                .whereEqualTo("status", Constants.REQUEST_PENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int pending = queryDocumentSnapshots.size();
                    tvPendingRequestsCount.setText(pending + " pending request" + (pending == 1 ? "" : "s"));
                });
    }
}
