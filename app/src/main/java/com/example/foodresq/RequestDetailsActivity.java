package com.example.foodresq;

import android.content.res.ColorStateList;
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

import com.example.foodresq.models.FoodRequest;
import com.example.foodresq.models.User;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.DateTimeUtil;
import com.example.foodresq.utils.FirebaseUtil;
import com.google.android.material.button.MaterialButton;

public class RequestDetailsActivity extends AppCompatActivity {

    private Toolbar toolbarRequestDetails;
    private ImageView ivStep1, ivStep2, ivStep3;
    private View lineStep1to2, lineStep2to3;
    private TextView tvStep1Label, tvStep2Label, tvStep3Label, tvStatusInstructions;
    private TextView tvReqDetailFoodName, tvReqDetailTime, tvReqDetailLocation, tvReqDetailDonorPhone;
    private ProgressBar pbConfirmCollection;
    private MaterialButton btnConfirmCollection;

    private String requestId;
    private FoodRequest currentRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        requestId = getIntent().getStringExtra(Constants.EXTRA_REQUEST_ID);
        if (TextUtils.isEmpty(requestId)) {
            Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadRequestDetails();

        btnConfirmCollection.setOnClickListener(v -> confirmCollectionDialog());
    }

    private void initViews() {
        toolbarRequestDetails = findViewById(R.id.toolbarRequestDetails);
        ivStep1 = findViewById(R.id.ivStep1);
        ivStep2 = findViewById(R.id.ivStep2);
        ivStep3 = findViewById(R.id.ivStep3);
        lineStep1to2 = findViewById(R.id.lineStep1to2);
        lineStep2to3 = findViewById(R.id.lineStep2to3);
        tvStep1Label = findViewById(R.id.tvStep1Label);
        tvStep2Label = findViewById(R.id.tvStep2Label);
        tvStep3Label = findViewById(R.id.tvStep3Label);
        tvStatusInstructions = findViewById(R.id.tvStatusInstructions);
        tvReqDetailFoodName = findViewById(R.id.tvReqDetailFoodName);
        tvReqDetailTime = findViewById(R.id.tvReqDetailTime);
        tvReqDetailLocation = findViewById(R.id.tvReqDetailLocation);
        tvReqDetailDonorPhone = findViewById(R.id.tvReqDetailDonorPhone);
        pbConfirmCollection = findViewById(R.id.pbConfirmCollection);
        btnConfirmCollection = findViewById(R.id.btnConfirmCollection);
    }

    private void setupToolbar() {
        toolbarRequestDetails.setNavigationOnClickListener(v -> finish());
    }

    private void loadRequestDetails() {
        FirebaseUtil.getRequestsCollection().document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentRequest = documentSnapshot.toObject(FoodRequest.class);
                        if (currentRequest != null) {
                            displayRequestData(currentRequest);
                            loadDonorContact(currentRequest.getDonorId());
                        }
                    } else {
                        Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadDonorContact(String donorId) {
        if (TextUtils.isEmpty(donorId)) return;
        FirebaseUtil.getUsersCollection().document(donorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User donor = documentSnapshot.toObject(User.class);
                        if (donor != null && !TextUtils.isEmpty(donor.getPhone())) {
                            tvReqDetailDonorPhone.setText("Donor Contact: " + donor.getPhone() + " (" + donor.getName() + ")");
                        }
                    }
                });
    }

    private void displayRequestData(FoodRequest req) {
        tvReqDetailFoodName.setText(req.getFoodName());
        tvReqDetailLocation.setText(!TextUtils.isEmpty(req.getPickupLocation()) ? req.getPickupLocation() : "Location not specified");
        tvReqDetailTime.setText("Requested: " + DateTimeUtil.formatDateTime(req.getRequestTime()));

        String status = !TextUtils.isEmpty(req.getStatus()) ? req.getStatus() : Constants.REQUEST_PENDING;

        int activeColor = ContextCompat.getColor(this, R.color.primary);
        int inactiveColor = ContextCompat.getColor(this, R.color.text_hint);

        // Step 1: always active for any created request
        ivStep1.setImageTintList(ColorStateList.valueOf(activeColor));
        tvStep1Label.setTextColor(activeColor);

        if (Constants.REQUEST_PENDING.equalsIgnoreCase(status)) {
            // Step 2 & 3 inactive
            ivStep2.setImageTintList(ColorStateList.valueOf(inactiveColor));
            tvStep2Label.setTextColor(inactiveColor);
            lineStep1to2.setBackgroundColor(inactiveColor);

            ivStep3.setImageTintList(ColorStateList.valueOf(inactiveColor));
            tvStep3Label.setTextColor(inactiveColor);
            lineStep2to3.setBackgroundColor(inactiveColor);

            tvStatusInstructions.setText("Your request is pending donor approval. Once the donor accepts, you can proceed to the pickup location.");
            btnConfirmCollection.setEnabled(false);
            btnConfirmCollection.setText("Awaiting Donor Acceptance");
        } else if (Constants.REQUEST_ACCEPTED.equalsIgnoreCase(status)) {
            // Step 1 & 2 active, Step 3 inactive
            ivStep2.setImageTintList(ColorStateList.valueOf(activeColor));
            tvStep2Label.setTextColor(activeColor);
            lineStep1to2.setBackgroundColor(activeColor);

            ivStep3.setImageTintList(ColorStateList.valueOf(inactiveColor));
            tvStep3Label.setTextColor(inactiveColor);
            lineStep2to3.setBackgroundColor(inactiveColor);

            tvStatusInstructions.setText("The donor has accepted your request! Please proceed to the pickup location and confirm collection once received.");
            btnConfirmCollection.setEnabled(true);
            btnConfirmCollection.setText("Confirm Food Collection");
        } else if (Constants.REQUEST_COLLECTED.equalsIgnoreCase(status)) {
            // All steps active
            ivStep2.setImageTintList(ColorStateList.valueOf(activeColor));
            tvStep2Label.setTextColor(activeColor);
            lineStep1to2.setBackgroundColor(activeColor);

            ivStep3.setImageTintList(ColorStateList.valueOf(activeColor));
            tvStep3Label.setTextColor(activeColor);
            lineStep2to3.setBackgroundColor(activeColor);

            tvStatusInstructions.setText("Food collection completed! Thank you for participating in FoodResQ and reducing food waste.");
            btnConfirmCollection.setEnabled(false);
            btnConfirmCollection.setText("Food Collected Successfully ✓");
        } else { // Rejected
            int rejectColor = ContextCompat.getColor(this, R.color.status_rejected);
            tvStatusInstructions.setText("This request was declined by the food donor. Please browse other available donations.");
            tvStatusInstructions.setTextColor(rejectColor);
            btnConfirmCollection.setEnabled(false);
            btnConfirmCollection.setText("Request Declined");
        }
    }

    private void confirmCollectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Food Collection")
                .setMessage("Are you sure you have collected \"" + currentRequest.getFoodName() + "\" from the donor?\n\nThis will mark the donation as Completed.")
                .setPositiveButton("Yes, Collected", (dialog, which) -> executeConfirmCollection())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void executeConfirmCollection() {
        showLoading(true);

        // 1. Update request status to Collected
        FirebaseUtil.getRequestsCollection().document(requestId)
                .update("status", Constants.REQUEST_COLLECTED)
                .addOnSuccessListener(aVoid -> {
                    // 2. Update food donation status to Completed
                    if (!TextUtils.isEmpty(currentRequest.getDonationId())) {
                        FirebaseUtil.getDonationsCollection().document(currentRequest.getDonationId())
                                .update("status", Constants.DONATION_COMPLETED);
                    }

                    showLoading(false);
                    Toast.makeText(RequestDetailsActivity.this, "Collection confirmed! Thank you!", Toast.LENGTH_SHORT).show();
                    loadRequestDetails();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(RequestDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean isLoading) {
        pbConfirmCollection.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnConfirmCollection.setEnabled(!isLoading);
    }
}
