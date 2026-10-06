package com.example.foodresq;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.foodresq.models.FoodDonation;
import com.example.foodresq.models.User;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.DateTimeUtil;
import com.example.foodresq.utils.FirebaseUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.StorageReference;

public class AddDonationActivity extends AppCompatActivity {

    private Toolbar toolbarAddDonation;
    private MaterialCardView cardFoodImagePicker;
    private ImageView ivDonationPreview;
    private LinearLayout layoutPickImagePrompt;
    private TextInputEditText etFoodName, etQuantity, etPreparedTime, etPickupLocation, etDescription;
    private Spinner spinnerFoodType;
    private ProgressBar pbAddDonation;
    private MaterialButton btnSubmitDonation;

    private Uri selectedImageUri = null;
    private String currentUserId;
    private String donorName = "Food Donor";
    private String donorPhone = "";

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivDonationPreview.setImageURI(uri);
                    layoutPickImagePrompt.setVisibility(View.GONE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_donation);

        currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId == null) {
            FirebaseUtil.logout(this);
            return;
        }

        initViews();
        setupToolbar();
        setupSpinner();
        loadDonorInfo();

        cardFoodImagePicker.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSubmitDonation.setOnClickListener(v -> submitDonation());
    }

    private void initViews() {
        toolbarAddDonation = findViewById(R.id.toolbarAddDonation);
        cardFoodImagePicker = findViewById(R.id.cardFoodImagePicker);
        ivDonationPreview = findViewById(R.id.ivDonationPreview);
        layoutPickImagePrompt = findViewById(R.id.layoutPickImagePrompt);
        etFoodName = findViewById(R.id.etFoodName);
        etQuantity = findViewById(R.id.etQuantity);
        spinnerFoodType = findViewById(R.id.spinnerFoodType);
        etPreparedTime = findViewById(R.id.etPreparedTime);
        etPickupLocation = findViewById(R.id.etPickupLocation);
        etDescription = findViewById(R.id.etDescription);
        pbAddDonation = findViewById(R.id.pbAddDonation);
        btnSubmitDonation = findViewById(R.id.btnSubmitDonation);

        // Default prepared time
        etPreparedTime.setText("Today, " + DateTimeUtil.formatTimeOnly(System.currentTimeMillis()));
    }

    private void setupToolbar() {
        toolbarAddDonation.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.food_types_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFoodType.setAdapter(adapter);
    }

    private void loadDonorInfo() {
        FirebaseUtil.getUsersCollection().document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            donorName = user.getName() != null ? user.getName() : "Food Donor";
                            donorPhone = user.getPhone() != null ? user.getPhone() : "";
                            if (!TextUtils.isEmpty(user.getLocation()) && TextUtils.isEmpty(etPickupLocation.getText())) {
                                etPickupLocation.setText(user.getLocation());
                            }
                        }
                    }
                });
    }

    private void submitDonation() {
        String foodName = etFoodName.getText() != null ? etFoodName.getText().toString().trim() : "";
        String quantityStr = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";
        String foodType = spinnerFoodType.getSelectedItem() != null ? spinnerFoodType.getSelectedItem().toString() : "Other";
        String prepTime = etPreparedTime.getText() != null ? etPreparedTime.getText().toString().trim() : "";
        String pickupLocation = etPickupLocation.getText() != null ? etPickupLocation.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(foodName)) {
            etFoodName.setError("Food name is required");
            etFoodName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }

        long quantity;
        try {
            quantity = Long.parseLong(quantityStr);
        } catch (NumberFormatException e) {
            String digits = quantityStr.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                try {
                    quantity = Long.parseLong(digits);
                } catch (NumberFormatException ex) {
                    etQuantity.setError("Please enter a valid numeric quantity (e.g. 10)");
                    etQuantity.requestFocus();
                    return;
                }
            } else {
                etQuantity.setError("Please enter a valid numeric quantity (e.g. 10)");
                etQuantity.requestFocus();
                return;
            }
        }

        if (quantity <= 0) {
            etQuantity.setError("Quantity must be greater than 0");
            etQuantity.requestFocus();
            return;
        }

        final long finalQuantity = quantity;

        if (TextUtils.isEmpty(prepTime)) {
            etPreparedTime.setError("Preparation time is required");
            etPreparedTime.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(pickupLocation)) {
            etPickupLocation.setError("Pickup location is required");
            etPickupLocation.requestFocus();
            return;
        }

        showLoading(true);

        DocumentReference newDonationRef = FirebaseUtil.getDonationsCollection().document();
        String donationId = newDonationRef.getId();

        if (selectedImageUri != null) {
            // Upload food image to Firebase Storage
            StorageReference imageRef = FirebaseUtil.getFoodImagesStorageRef().child(donationId + ".jpg");
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();
                                saveDonationDocument(newDonationRef, donationId, foodName, finalQuantity, foodType, prepTime, pickupLocation, description, downloadUrl);
                            })
                            .addOnFailureListener(e -> {
                                // In case download URL fails, proceed without image URL
                                saveDonationDocument(newDonationRef, donationId, foodName, finalQuantity, foodType, prepTime, pickupLocation, description, "");
                            }))
                    .addOnFailureListener(e -> {
                        // Storage failure (e.g. storage rules or quota), fallback to save without image
                        saveDonationDocument(newDonationRef, donationId, foodName, finalQuantity, foodType, prepTime, pickupLocation, description, "");
                    });
        } else {
            // Save without image
            saveDonationDocument(newDonationRef, donationId, foodName, finalQuantity, foodType, prepTime, pickupLocation, description, "");
        }
    }

    private void saveDonationDocument(DocumentReference ref, String donationId, String foodName,
                                      long quantity, String foodType, String prepTime,
                                      String pickupLocation, String description, String imageUrl) {

        FoodDonation donation = new FoodDonation(
                donationId,
                currentUserId,
                donorName,
                donorPhone,
                foodName,
                quantity,
                foodType,
                prepTime,
                pickupLocation,
                description,
                imageUrl,
                Constants.DONATION_AVAILABLE,
                System.currentTimeMillis()
        );

        ref.set(donation)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(AddDonationActivity.this, "Food donation posted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(AddDonationActivity.this, "Failed to post donation: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        pbAddDonation.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmitDonation.setEnabled(!isLoading);
        cardFoodImagePicker.setEnabled(!isLoading);
    }
}
