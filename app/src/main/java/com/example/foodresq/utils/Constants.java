package com.example.foodresq.utils;

public class Constants {

    // Firestore Collections
    public static final String COLLECTION_USERS = "Users";
    public static final String COLLECTION_DONATIONS = "FoodDonations";
    public static final String COLLECTION_REQUESTS = "Requests";

    // User Roles
    public static final String ROLE_DONOR = "Donor";
    public static final String ROLE_RECEIVER = "Receiver";

    // Donation Statuses
    public static final String DONATION_AVAILABLE = "Available";
    public static final String DONATION_RESERVED = "Reserved";
    public static final String DONATION_COMPLETED = "Completed";
    public static final String DONATION_CANCELLED = "Cancelled";

    // Request Statuses
    public static final String REQUEST_PENDING = "Pending";
    public static final String REQUEST_ACCEPTED = "Accepted";
    public static final String REQUEST_REJECTED = "Rejected";
    public static final String REQUEST_COLLECTED = "Collected";

    // Intent Extra Keys
    public static final String EXTRA_DONATION_ID = "extra_donation_id";
    public static final String EXTRA_REQUEST_ID = "extra_request_id";
}