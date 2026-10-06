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

public class ReceiverProfileActivity extends AppCompatActivity {

    private Toolbar toolbarReceiverProfile;
    private TextView tvReceiverProfileName;
    private TextInputEditText etReceiverName, etReceiverEmail, etReceiverPhone, etReceiverLocation;
    private ProgressBar pbReceiverProfileSave;
    private MaterialButton btnSaveReceiverProfile, btnReceiverLogoutAction;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_profile);

        currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId == null) {
            FirebaseUtil.logout(this);
            return;
        }

        initViews();
        setupToolbar();
        loadProfile();

        btnSaveReceiverProfile.setOnClickListener(v -> saveProfileChanges());
        btnReceiverLogoutAction.setOnClickListener(v -> confirmLogout());
    }

    private void initViews() {
        toolbarReceiverProfile = findViewById(R.id.toolbarReceiverProfile);
        tvReceiverProfileName = findViewById(R.id.tvReceiverProfileName);
        etReceiverName = findViewById(R.id.etReceiverName);
        etReceiverEmail = findViewById(R.id.etReceiverEmail);
        etReceiverPhone = findViewById(R.id.etReceiverPhone);
        etReceiverLocation = findViewById(R.id.etReceiverLocation);
        pbReceiverProfileSave = findViewById(R.id.pbReceiverProfileSave);
        btnSaveReceiverProfile = findViewById(R.id.btnSaveReceiverProfile);
        btnReceiverLogoutAction = findViewById(R.id.btnReceiverLogoutAction);
    }

    private void setupToolbar() {
        toolbarReceiverProfile.setNavigationOnClickListener(v -> finish());
    }

    private void loadProfile() {
        FirebaseUtil.getUsersCollection().document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            tvReceiverProfileName.setText(!TextUtils.isEmpty(user.getName()) ? user.getName() : "Receiver / NGO");
                            etReceiverName.setText(user.getName());
                            etReceiverEmail.setText(user.getEmail());
                            etReceiverPhone.setText(user.getPhone());
                            etReceiverLocation.setText(user.getLocation());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveProfileChanges() {
        String name = etReceiverName.getText() != null ? etReceiverName.getText().toString().trim() : "";
        String phone = etReceiverPhone.getText() != null ? etReceiverPhone.getText().toString().trim() : "";
        String location = etReceiverLocation.getText() != null ? etReceiverLocation.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etReceiverName.setError("Name / Organization is required");
            etReceiverName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etReceiverPhone.setError("Phone is required");
            etReceiverPhone.requestFocus();
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
                    tvReceiverProfileName.setText(name);
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
        pbReceiverProfileSave.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveReceiverProfile.setEnabled(!isLoading);
    }
}
