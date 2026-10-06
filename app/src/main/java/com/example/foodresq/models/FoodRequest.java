package com.example.foodresq.models;

import java.io.Serializable;

public class FoodRequest implements Serializable {
    private String requestId;
    private String donationId;
    private String receiverId;
    private String receiverName;
    private String receiverPhone;
    private String donorId;
    private String foodName;
    private String pickupLocation;
    private long requestTime;
    private String status; // Pending, Accepted, Rejected, Collected

    // Required empty constructor for Firestore
    public FoodRequest() {
    }

    public FoodRequest(String requestId, String donationId, String receiverId, String receiverName,
                       String receiverPhone, String donorId, String foodName, String pickupLocation,
                       long requestTime, String status) {
        this.requestId = requestId;
        this.donationId = donationId;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.donorId = donorId;
        this.foodName = foodName;
        this.pickupLocation = pickupLocation;
        this.requestTime = requestTime;
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
