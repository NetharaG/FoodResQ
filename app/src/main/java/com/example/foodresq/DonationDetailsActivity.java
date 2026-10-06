package com.example.foodresq;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.foodresq.models.FoodDonation;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.DateTimeUtil;
import com.example.foodresq.utils.FirebaseUtil;
import com.google.android.material.button.MaterialButton;

public class DonationDetailsActivity extends AppCompatActivity {

    private Toolbar toolbarDonationDetails;
    private ImageView ivDetailFoodImage;
    private TextView tvDetailStatusBadge, tvDetailFoodTypeBadge, tvDetailFoodName, tvDetailPostedDate;
    private TextView tvDetailQuantity, tvDetailPrepTime, tvDetailPickupLocation, tvDetailDescription;
    private MaterialButton btnViewRequests, btnCancelDonation;

    private String donationId;
    private FoodDonation currentDonation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_details);

        donationId = getIntent().getStringExtra(Constants.EXTRA_DONATION_ID);
        if (TextUtils.isEmpty(donationId)) {
            Toast.makeText(this, "Donation not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadDonationDetails();

        btnViewRequests.setOnClickListener(v -> {
            Intent intent = new Intent(this, DonorRequestsActivity.class);
            startActivity(intent);
        });

        btnCancelDonation.setOnClickListener(v -> confirmCancelDonation());
    }

    private void initViews() {
        toolbarDonationDetails = findViewById(R.id.toolbarDonationDetails);
        ivDetailFoodImage = findViewById(R.id.ivDetailFoodImage);
        tvDetailStatusBadge = findViewById(R.id.tvDetailStatusBadge);
        tvDetailFoodTypeBadge = findViewById(R.id.tvDetailFoodTypeBadge);
        tvDetailFoodName = findViewById(R.id.tvDetailFoodName);
        tvDetailPostedDate = findViewById(R.id.tvDetailPostedDate);
        tvDetailQuantity = findViewById(R.id.tvDetailQuantity);
        tvDetailPrepTime = findViewById(R.id.tvDetailPrepTime);
        tvDetailPickupLocation = findViewById(R.id.tvDetailPickupLocation);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        btnViewRequests = findViewById(R.id.btnViewRequests);
        btnCancelDonation = findViewById(R.id.btnCancelDonation);
    }

    private void setupToolbar() {
        toolbarDonationDetails.setNavigationOnClickListener(v -> finish());
    }

    private void loadDonationDetails() {
        FirebaseUtil.getDonationsCollection().document(donationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentDonation = FoodDonation.fromSnapshot(documentSnapshot);
                        if (currentDonation != null) {
                            displayDonationData(currentDonation);
                        }
                    } else {
                        Toast.makeText(this, "Donation record does not exist", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayDonationData(FoodDonation donation) {
        tvDetailFoodName.setText(donation.getFoodName());
        tvDetailQuantity.setText("Quantity: " + donation.getQuantity());
        tvDetailPrepTime.setText("Prepared: " + (!TextUtils.isEmpty(donation.getPreparedTime()) ? donation.getPreparedTime() : "N/A"));
        tvDetailPickupLocation.setText(!TextUtils.isEmpty(donation.getPickupLocation()) ? donation.getPickupLocation() : "Location not specified");
        tvDetailDescription.setText(!TextUtils.isEmpty(donation.getDescription()) ? donation.getDescription() : "No extra description provided.");
        tvDetailPostedDate.setText("Posted: " + DateTimeUtil.formatDateTime(donation.getCreatedAt()));
        tvDetailFoodTypeBadge.setText(!TextUtils.isEmpty(donation.getFoodType()) ? donation.getFoodType() : "Food");

        if (!TextUtils.isEmpty(donation.getImageUrl())) {
            Glide.with(this)
                    .load(donation.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(ivDetailFoodImage);
        } else {
            ivDetailFoodImage.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Status badge styling
        String status = !TextUtils.isEmpty(donation.getStatus()) ? donation.getStatus() : Constants.DONATION_AVAILABLE;
        tvDetailStatusBadge.setText(status);

        int textColor;
        int bgColor;
        if (Constants.DONATION_AVAILABLE.equalsIgnoreCase(status)) {
            textColor = ContextCompat.getColor(this, R.color.status_available);
            bgColor = ContextCompat.getColor(this, R.color.status_available_bg);
            btnCancelDonation.setVisibility(View.VISIBLE);
        } else if (Constants.DONATION_RESERVED.equalsIgnoreCase(status)) {
            textColor = ContextCompat.getColor(this, R.color.status_reserved);
            bgColor = ContextCompat.getColor(this, R.color.status_reserved_bg);
            btnCancelDonation.setVisibility(View.GONE);
        } else if (Constants.DONATION_COMPLETED.equalsIgnoreCase(status)) {
            textColor = ContextCompat.getColor(this, R.color.status_completed);
            bgColor = ContextCompat.getColor(this, R.color.status_completed_bg);
            btnCancelDonation.setVisibility(View.GONE);
        } else {
            textColor = ContextCompat.getColor(this, R.color.status_cancelled);
            bgColor = ContextCompat.getColor(this, R.color.status_cancelled_bg);
            btnCancelDonation.setVisibility(View.GONE);
        }

        tvDetailStatusBadge.setTextColor(textColor);
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(50f);
        shape.setColor(bgColor);
        tvDetailStatusBadge.setBackground(shape);
    }

    private void confirmCancelDonation() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Donation")
                .setMessage("Are you sure you want to cancel this food donation? It will no longer be available for receivers.")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    FirebaseUtil.getDonationsCollection().document(donationId)
                            .update("status", Constants.DONATION_CANCELLED)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Donation cancelled", Toast.LENGTH_SHORT).show();
                                loadDonationDetails();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }
}
