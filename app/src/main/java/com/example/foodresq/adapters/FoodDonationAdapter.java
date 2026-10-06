package com.example.foodresq.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodresq.R;
import com.example.foodresq.models.FoodDonation;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.DateTimeUtil;

import java.util.List;

public class FoodDonationAdapter extends RecyclerView.Adapter<FoodDonationAdapter.DonationViewHolder> {

    public interface OnDonationClickListener {
        void onDonationClick(FoodDonation donation);
    }

    private final Context context;
    private List<FoodDonation> donationList;
    private final OnDonationClickListener listener;

    public FoodDonationAdapter(Context context, List<FoodDonation> donationList, OnDonationClickListener listener) {
        this.context = context;
        this.donationList = donationList;
        this.listener = listener;
    }

    public void updateList(List<FoodDonation> newList) {
        this.donationList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_donation, parent, false);
        return new DonationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder holder, int position) {
        FoodDonation donation = donationList.get(position);
        holder.bind(donation);
    }

    @Override
    public int getItemCount() {
        return donationList != null ? donationList.size() : 0;
    }

    class DonationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvStatusBadge;
        TextView tvFoodTypeBadge;
        TextView tvFoodName;
        TextView tvQuantity;
        TextView tvPreparedTime;
        TextView tvPickupLocation;
        TextView tvDatePosted;

        public DonationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvFoodTypeBadge = itemView.findViewById(R.id.tvFoodTypeBadge);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPreparedTime = itemView.findViewById(R.id.tvPreparedTime);
            tvPickupLocation = itemView.findViewById(R.id.tvPickupLocation);
            tvDatePosted = itemView.findViewById(R.id.tvDatePosted);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDonationClick(donationList.get(pos));
                }
            });
        }

        public void bind(FoodDonation donation) {
            tvFoodName.setText(donation.getFoodName());
            tvQuantity.setText("Quantity: " + donation.getQuantity());
            tvFoodTypeBadge.setText(!TextUtils.isEmpty(donation.getFoodType()) ? donation.getFoodType() : "Food");
            tvPreparedTime.setText("Prep: " + (!TextUtils.isEmpty(donation.getPreparedTime()) ? donation.getPreparedTime() : "N/A"));
            tvPickupLocation.setText(!TextUtils.isEmpty(donation.getPickupLocation()) ? donation.getPickupLocation() : "Location not specified");
            tvDatePosted.setText("Posted: " + DateTimeUtil.formatDateOnly(donation.getCreatedAt()));

            // Load Food Image with Glide
            if (!TextUtils.isEmpty(donation.getImageUrl())) {
                Glide.with(context)
                        .load(donation.getImageUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(ivFoodImage);
            } else {
                ivFoodImage.setImageResource(R.drawable.ic_image_placeholder);
            }

            // Status Badge Styling
            String status = donation.getStatus();
            if (TextUtils.isEmpty(status)) {
                status = Constants.DONATION_AVAILABLE;
            }
            tvStatusBadge.setText(status);

            int textColor;
            int bgColor;
            if (Constants.DONATION_AVAILABLE.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.status_available);
                bgColor = ContextCompat.getColor(context, R.color.status_available_bg);
            } else if (Constants.DONATION_RESERVED.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.status_reserved);
                bgColor = ContextCompat.getColor(context, R.color.status_reserved_bg);
            } else if (Constants.DONATION_COMPLETED.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.status_completed);
                bgColor = ContextCompat.getColor(context, R.color.status_completed_bg);
            } else {
                textColor = ContextCompat.getColor(context, R.color.status_cancelled);
                bgColor = ContextCompat.getColor(context, R.color.status_cancelled_bg);
            }

            tvStatusBadge.setTextColor(textColor);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(50f);
            shape.setColor(bgColor);
            tvStatusBadge.setBackground(shape);
        }
    }
}
