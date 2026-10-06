package com.example.foodresq;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodresq.models.User;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.FirebaseUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etRegName, etRegEmail, etRegPassword, etRegPhone, etRegLocation;
    private RadioGroup rgRole;
    private RadioButton rbDonor, rbReceiver;
    private MaterialButton btnRegister;
    private TextView tvGoToLogin;
    private ProgressBar registerProgressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseUtil.getAuth();

        etRegName = findViewById(R.id.etRegName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegPhone = findViewById(R.id.etRegPhone);
        etRegLocation = findViewById(R.id.etRegLocation);
        rgRole = findViewById(R.id.rgRole);
        rbDonor = findViewById(R.id.rbDonor);
        rbReceiver = findViewById(R.id.rbReceiver);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        registerProgressBar = findViewById(R.id.registerProgressBar);

        btnRegister.setOnClickListener(v -> performRegistration());

        tvGoToLogin.setOnClickListener(v -> {
            finish(); // return to Login
        });
    }

    private void performRegistration() {
        String name = etRegName.getText() != null ? etRegName.getText().toString().trim() : "";
        String email = etRegEmail.getText() != null ? etRegEmail.getText().toString().trim() : "";
        String password = etRegPassword.getText() != null ? etRegPassword.getText().toString().trim() : "";
        String phone = etRegPhone.getText() != null ? etRegPhone.getText().toString().trim() : "";
        String location = etRegLocation.getText() != null ? etRegLocation.getText().toString().trim() : "";

        String role = rbDonor.isChecked() ? Constants.ROLE_DONOR : Constants.ROLE_RECEIVER;

        if (TextUtils.isEmpty(name)) {
            etRegName.setError("Name is required");
            etRegName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegEmail.setError("Enter a valid email address");
            etRegEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etRegPassword.setError("Password must be at least 6 characters");
            etRegPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etRegPhone.setError("Phone number is required");
            etRegPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(location)) {
            etRegLocation.setError("Location / Address is required");
            etRegLocation.requestFocus();
            return;
        }

        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser.getUid(), name, email, phone, role, location);
                        } else {
                            showLoading(false);
                            Toast.makeText(RegisterActivity.this, "Failed to create user account.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showLoading(false);
                        String error = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, String email, String phone, String role, String location) {
        User user = new User(userId, name, email, phone, role, location, System.currentTimeMillis());

        FirebaseUtil.getUsersCollection()
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    if (Constants.ROLE_RECEIVER.equalsIgnoreCase(role)) {
                        Intent intent = new Intent(RegisterActivity.this, ReceiverHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(RegisterActivity.this, DonorHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        registerProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
    }
}
