package com.example.foodresq.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;

public class FoodDonation implements Serializable {
    private String donationId;
    private String donorId;
    private String donorName;
    private String donorPhone;
    private String foodName;
    private long quantity;
    private String foodType;
    private String preparedTime;
    private String pickupLocation;
    private String description;
    private String imageUrl;
    private String status; // Available, Reserved, Completed, Cancelled
    private long createdAt;

    // Required empty constructor for Firestore
    public FoodDonation() {
    }

    public FoodDonation(String donationId, String donorId, String donorName, String donorPhone,
                        String foodName, long quantity, String foodType, String preparedTime,
                        String pickupLocation, String description, String imageUrl, String status,
                        long createdAt) {
        this.donationId = donationId;
        this.donorId = donorId;
        this.donorName = donorName;
        this.donorPhone = donorPhone;
        this.foodName = foodName;
        this.quantity = quantity;
        this.foodType = foodType;
        this.preparedTime = preparedTime;
        this.pickupLocation = pickupLocation;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDonorPhone() {
        return donorPhone;
    }

    public void setDonorPhone(String donorPhone) {
        this.donorPhone = donorPhone;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getPreparedTime() {
        return preparedTime;
    }

    public void setPreparedTime(String preparedTime) {
        this.preparedTime = preparedTime;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Safely constructs a FoodDonation from a Firestore DocumentSnapshot,
     * converting quantity whether it was stored as a Number (Long/Integer/Double) or a String.
     */
    public static FoodDonation fromSnapshot(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return null;
        }

        FoodDonation donation = new FoodDonation();

        String id = getStringSafe(doc, "donationId");
        if (id.isEmpty()) {
            id = doc.getId();
        }
        donation.setDonationId(id);
        donation.setDonorId(getStringSafe(doc, "donorId"));
        donation.setDonorName(getStringSafe(doc, "donorName"));
        donation.setDonorPhone(getStringSafe(doc, "donorPhone"));
        donation.setFoodName(getStringSafe(doc, "foodName"));
        donation.setFoodType(getStringSafe(doc, "foodType"));
        donation.setPreparedTime(getStringSafe(doc, "preparedTime"));
        donation.setPickupLocation(getStringSafe(doc, "pickupLocation"));
        donation.setDescription(getStringSafe(doc, "description"));
        donation.setImageUrl(getStringSafe(doc, "imageUrl"));

        String status = getStringSafe(doc, "status");
        donation.setStatus(!status.isEmpty() ? status : "Available");

        // Parse quantity safely from Number or String
        long quantity = 0L;
        Object qtyObj = doc.get("quantity");
        if (qtyObj instanceof Number) {
            quantity = ((Number) qtyObj).longValue();
        } else if (qtyObj instanceof String) {
            String qtyStr = ((String) qtyObj).trim();
            try {
                quantity = Long.parseLong(qtyStr);
            } catch (NumberFormatException e) {
                String digits = qtyStr.replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    try {
                        quantity = Long.parseLong(digits);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        donation.setQuantity(quantity);

        // Parse createdAt safely from Number or Timestamp
        long createdAt = 0L;
        Object createdObj = doc.get("createdAt");
        if (createdObj instanceof Number) {
            createdAt = ((Number) createdObj).longValue();
        } else if (createdObj instanceof Timestamp) {
            createdAt = ((Timestamp) createdObj).toDate().getTime();
        }
        donation.setCreatedAt(createdAt);

        return donation;
    }

    private static String getStringSafe(DocumentSnapshot doc, String field) {
        Object val = doc.get(field);
        return val != null ? String.valueOf(val).trim() : "";
    }
}
