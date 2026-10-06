package com.example.foodresq;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.foodresq.models.FoodDonation;
import com.example.foodresq.models.FoodRequest;
import com.example.foodresq.models.User;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.DateTimeUtil;
import com.example.foodresq.utils.FirebaseUtil;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;

public class FoodDetailsActivity extends AppCompatActivity {

    private Toolbar toolbarFoodDetails;
    private ImageView ivReceiverFoodImage;
    private TextView tvReceiverFoodStatus, tvReceiverFoodType, tvReceiverFoodName, tvReceiverPostedDate;
    private TextView tvReceiverQuantity, tvReceiverPrepTime, tvReceiverPickupLocation;
    private TextView tvReceiverDonorName, tvReceiverDonorPhone, tvReceiverDescription;
    private ProgressBar pbRequestFood;
    private MaterialButton btnRequestFood;

    private String donationId;
    private FoodDonation currentDonation;
    private String currentUserId;
    private String receiverName = "Food Receiver / NGO";
    private String receiverPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);

        currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId == null) {
            FirebaseUtil.logout(this);
            return;
        }

        donationId = getIntent().getStringExtra(Constants.EXTRA_DONATION_ID);
        if (TextUtils.isEmpty(donationId)) {
            Toast.makeText(this, "Food item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadReceiverProfile();
        loadFoodDetails();

        btnRequestFood.setOnClickListener(v -> confirmAndSendRequest());
    }

    private void initViews() {
        toolbarFoodDetails = findViewById(R.id.toolbarFoodDetails);
        ivReceiverFoodImage = findViewById(R.id.ivReceiverFoodImage);
        tvReceiverFoodStatus = findViewById(R.id.tvReceiverFoodStatus);
        tvReceiverFoodType = findViewById(R.id.tvReceiverFoodType);
        tvReceiverFoodName = findViewById(R.id.tvReceiverFoodName);
        tvReceiverPostedDate = findViewById(R.id.tvReceiverPostedDate);
        tvReceiverQuantity = findViewById(R.id.tvReceiverQuantity);
        tvReceiverPrepTime = findViewById(R.id.tvReceiverPrepTime);
        tvReceiverPickupLocation = findViewById(R.id.tvReceiverPickupLocation);
        tvReceiverDonorName = findViewById(R.id.tvReceiverDonorName);
        tvReceiverDonorPhone = findViewById(R.id.tvReceiverDonorPhone);
        tvReceiverDescription = findViewById(R.id.tvReceiverDescription);
        pbRequestFood = findViewById(R.id.pbRequestFood);
        btnRequestFood = findViewById(R.id.btnRequestFood);
    }

    private void setupToolbar() {
        toolbarFoodDetails.setNavigationOnClickListener(v -> finish());
    }

    private void loadReceiverProfile() {
        FirebaseUtil.getUsersCollection().document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            if (!TextUtils.isEmpty(user.getName())) receiverName = user.getName();
                            if (!TextUtils.isEmpty(user.getPhone())) receiverPhone = user.getPhone();
                        }
                    }
                });
    }

    private void loadFoodDetails() {
        FirebaseUtil.getDonationsCollection().document(donationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentDonation = FoodDonation.fromSnapshot(documentSnapshot);
                        if (currentDonation != null) {
                            displayFoodData(currentDonation);
                            checkExistingRequest();
                        }
                    } else {
                        Toast.makeText(this, "Food donation no longer available", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load food details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void displayFoodData(FoodDonation donation) {
        tvReceiverFoodName.setText(donation.getFoodName());
        tvReceiverQuantity.setText("Quantity: " + donation.getQuantity());
        tvReceiverPrepTime.setText("Prepared: " + (!TextUtils.isEmpty(donation.getPreparedTime()) ? donation.getPreparedTime() : "N/A"));
        tvReceiverPickupLocation.setText(!TextUtils.isEmpty(donation.getPickupLocation()) ? donation.getPickupLocation() : "Pickup location not specified");
        tvReceiverDescription.setText(!TextUtils.isEmpty(donation.getDescription()) ? donation.getDescription() : "No special instructions provided.");
        tvReceiverPostedDate.setText("Posted: " + DateTimeUtil.formatDateTime(donation.getCreatedAt()));
        tvReceiverFoodType.setText(!TextUtils.isEmpty(donation.getFoodType()) ? donation.getFoodType() : "Food");

        tvReceiverDonorName.setText("Donor: " + (!TextUtils.isEmpty(donation.getDonorName()) ? donation.getDonorName() : "Community Donor"));
        tvReceiverDonorPhone.setText("Donor Phone: " + (!TextUtils.isEmpty(donation.getDonorPhone()) ? donation.getDonorPhone() : "Available upon request acceptance"));

        if (!TextUtils.isEmpty(donation.getImageUrl())) {
            Glide.with(this)
                    .load(donation.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(ivReceiverFoodImage);
        } else {
            ivReceiverFoodImage.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Status badge
        String status = !TextUtils.isEmpty(donation.getStatus()) ? donation.getStatus() : Constants.DONATION_AVAILABLE;
        tvReceiverFoodStatus.setText(status);

        int textColor = ContextCompat.getColor(this, R.color.status_available);
        int bgColor = ContextCompat.getColor(this, R.color.status_available_bg);

        if (!Constants.DONATION_AVAILABLE.equalsIgnoreCase(status)) {
            textColor = ContextCompat.getColor(this, R.color.status_reserved);
            bgColor = ContextCompat.getColor(this, R.color.status_reserved_bg);
            btnRequestFood.setEnabled(false);
            btnRequestFood.setText("Food Already " + status);
        }

        tvReceiverFoodStatus.setTextColor(textColor);
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(50f);
        shape.setColor(bgColor);
        tvReceiverFoodStatus.setBackground(shape);
    }

    private void checkExistingRequest() {
        FirebaseUtil.getRequestsCollection()
                .whereEqualTo("donationId", donationId)
                .whereEqualTo("receiverId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        FoodRequest existingReq = queryDocumentSnapshots.getDocuments().get(0).toObject(FoodRequest.class);
                        if (existingReq != null) {
                            btnRequestFood.setEnabled(false);
                            btnRequestFood.setText("Request Already Sent (" + existingReq.getStatus() + ")");
                        }
                    }
                });
    }

    private void confirmAndSendRequest() {
        if (currentDonation == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Confirm Food Request")
                .setMessage("Would you like to request \"" + currentDonation.getFoodName() + "\" (" + currentDonation.getQuantity() + ") from " + currentDonation.getDonorName() + "?\n\nYour contact details will be shared with the donor.")
                .setPositiveButton("Send Request", (dialog, which) -> sendRequestToFirestore())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendRequestToFirestore() {
        showLoading(true);

        DocumentReference reqRef = FirebaseUtil.getRequestsCollection().document();
        String requestId = reqRef.getId();

        FoodRequest request = new FoodRequest(
                requestId,
                currentDonation.getDonationId(),
                currentUserId,
                receiverName,
                receiverPhone,
                currentDonation.getDonorId(),
                currentDonation.getFoodName(),
                currentDonation.getPickupLocation(),
                System.currentTimeMillis(),
                Constants.REQUEST_PENDING
        );

        reqRef.set(request)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(FoodDetailsActivity.this, "Request submitted! Track status in My Requests.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(FoodDetailsActivity.this, MyRequestsActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(FoodDetailsActivity.this, "Failed to submit request: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        pbRequestFood.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRequestFood.setEnabled(!isLoading);
    }
}
