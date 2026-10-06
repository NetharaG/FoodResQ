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

import com.example.foodresq.models.User;
import com.example.foodresq.utils.FirebaseUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class DonorProfileActivity extends AppCompatActivity {

    private Toolbar toolbarDonorProfile;
    private TextView tvProfileTitleName;
    private TextInputEditText etProfileName, etProfileEmail, etProfilePhone, etProfileLocation;
    private ProgressBar pbProfileSave;
    private MaterialButton btnSaveProfile, btnProfileLogout;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_profile);

        currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId == null) {
            FirebaseUtil.logout(this);
            return;
        }

        initViews();
        setupToolbar();
        loadProfile();

        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        btnProfileLogout.setOnClickListener(v -> confirmLogout());
    }

    private void initViews() {
        toolbarDonorProfile = findViewById(R.id.toolbarDonorProfile);
        tvProfileTitleName = findViewById(R.id.tvProfileTitleName);
        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        etProfileLocation = findViewById(R.id.etProfileLocation);
        pbProfileSave = findViewById(R.id.pbProfileSave);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnProfileLogout = findViewById(R.id.btnProfileLogout);
    }

    private void setupToolbar() {
        toolbarDonorProfile.setNavigationOnClickListener(v -> finish());
    }

    private void loadProfile() {
        FirebaseUtil.getUsersCollection().document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            tvProfileTitleName.setText(!TextUtils.isEmpty(user.getName()) ? user.getName() : "Food Donor");
                            etProfileName.setText(user.getName());
                            etProfileEmail.setText(user.getEmail());
                            etProfilePhone.setText(user.getPhone());
                            etProfileLocation.setText(user.getLocation());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveProfileChanges() {
        String name = etProfileName.getText() != null ? etProfileName.getText().toString().trim() : "";
        String phone = etProfilePhone.getText() != null ? etProfilePhone.getText().toString().trim() : "";
        String location = etProfileLocation.getText() != null ? etProfileLocation.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etProfileName.setError("Name is required");
            etProfileName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etProfilePhone.setError("Phone is required");
            etProfilePhone.requestFocus();
            return;
        }

        showLoading(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("location", location);

        FirebaseUtil.getUsersCollection().document(currentUserId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    tvProfileTitleName.setText(name);
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out of FoodResQ?")
                .setPositiveButton("Logout", (dialog, which) -> FirebaseUtil.logout(this))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLoading(boolean isLoading) {
        pbProfileSave.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveProfile.setEnabled(!isLoading);
    }
}
