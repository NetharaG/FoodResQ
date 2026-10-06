package com.example.foodresq.utils;

import android.content.Context;
import android.content.Intent;

import com.example.foodresq.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtil {

    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public static FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }

    public static CollectionReference getUsersCollection() {
        return getFirestore().collection(Constants.COLLECTION_USERS);
    }

    public static CollectionReference getDonationsCollection() {
        return getFirestore().collection(Constants.COLLECTION_DONATIONS);
    }

    public static CollectionReference getRequestsCollection() {
        return getFirestore().collection(Constants.COLLECTION_REQUESTS);
    }

    public static FirebaseStorage getStorage() {
        return FirebaseStorage.getInstance();
    }

    public static StorageReference getFoodImagesStorageRef() {
        return getStorage().getReference().child("food_images");
    }

    public static boolean isPlaceholderApiKey(String errorMessage) {
        if (errorMessage == null) return false;
        return errorMessage.contains("API key not valid") || errorMessage.contains("INVALID_API_KEY");
    }

    public static void logout(Context context) {
        getAuth().signOut();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
