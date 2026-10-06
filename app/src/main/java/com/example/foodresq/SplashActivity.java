package com.example.foodresq;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodresq.models.User;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthAndRoute, SPLASH_DELAY);
    }

    private void checkAuthAndRoute() {
        FirebaseUser currentUser = FirebaseUtil.getCurrentUser();
        if (currentUser == null) {
            goToLogin();
            return;
        }

        // Fetch user role from Firestore
        FirebaseUtil.getUsersCollection()
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && Constants.ROLE_RECEIVER.equalsIgnoreCase(user.getRole())) {
                            goToReceiverHome();
                        } else {
                            // Default to Donor if role is Donor or unspecified
                            goToDonorHome();
                        }
                    } else {
                        // User record not yet created in Firestore
                        goToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SplashActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    goToLogin();
                });
    }

    private void goToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToDonorHome() {
        Intent intent = new Intent(SplashActivity.this, DonorHomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToReceiverHome() {
        Intent intent = new Intent(SplashActivity.this, ReceiverHomeActivity.class);
        startActivity(intent);
        finish();
    }
}
