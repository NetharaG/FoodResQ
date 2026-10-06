package com.example.foodresq.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodresq.R;
import com.example.foodresq.models.FoodRequest;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.DateTimeUtil;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DonorRequestAdapter extends RecyclerView.Adapter<DonorRequestAdapter.RequestViewHolder> {

    public interface OnRequestActionListener {
        void onAccept(FoodRequest request);
        void onReject(FoodRequest request);
    }

    private final Context context;
    private List<FoodRequest> requestList;
    private final OnRequestActionListener listener;

    public DonorRequestAdapter(Context context, List<FoodRequest> requestList, OnRequestActionListener listener) {
        this.context = context;
        this.requestList = requestList;
        this.listener = listener;
    }

    public void updateList(List<FoodRequest> newList) {
        this.requestList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donor_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        FoodRequest request = requestList.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvRequestFoodName;
        TextView tvDonorRequestStatus;
        TextView tvReceiverName;
        TextView tvReceiverPhone;
        TextView tvRequestPickupLocation;
        TextView tvRequestTime;
        LinearLayout layoutActionButtons;
        MaterialButton btnAcceptRequest;
        MaterialButton btnRejectRequest;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRequestFoodName = itemView.findViewById(R.id.tvRequestFoodName);
            tvDonorRequestStatus = itemView.findViewById(R.id.tvDonorRequestStatus);
            tvReceiverName = itemView.findViewById(R.id.tvReceiverName);
            tvReceiverPhone = itemView.findViewById(R.id.tvReceiverPhone);
            tvRequestPickupLocation = itemView.findViewById(R.id.tvRequestPickupLocation);
            tvRequestTime = itemView.findViewById(R.id.tvRequestTime);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnAcceptRequest = itemView.findViewById(R.id.btnAcceptRequest);
            btnRejectRequest = itemView.findViewById(R.id.btnRejectRequest);

            btnAcceptRequest.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAccept(requestList.get(pos));
                }
            });

            btnRejectRequest.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onReject(requestList.get(pos));
                }
            });
        }

        public void bind(FoodRequest request) {
            tvRequestFoodName.setText(!TextUtils.isEmpty(request.getFoodName()) ? request.getFoodName() : "Food Item");
            tvReceiverName.setText("Receiver: " + (!TextUtils.isEmpty(request.getReceiverName()) ? request.getReceiverName() : "Unknown"));
            tvReceiverPhone.setText(!TextUtils.isEmpty(request.getReceiverPhone()) ? request.getReceiverPhone() : "No contact provided");
            tvRequestPickupLocation.setText(!TextUtils.isEmpty(request.getPickupLocation()) ? request.getPickupLocation() : "Location not specified");
            tvRequestTime.setText("Requested: " + DateTimeUtil.formatDateTime(request.getRequestTime()));

            String status = request.getStatus();
            if (TextUtils.isEmpty(status)) {
                status = Constants.REQUEST_PENDING;
            }
            tvDonorRequestStatus.setText(status);

            int textColor;
            int bgColor;

            if (Constants.REQUEST_PENDING.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.status_pending);
                bgColor = ContextCompat.getColor(context, R.color.status_pending_bg);
                layoutActionButtons.setVisibility(View.VISIBLE);
            } else if (Constants.REQUEST_ACCEPTED.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.status_accepted);
                bgColor = ContextCompat.getColor(context, R.color.status_accepted_bg);
                layoutActionButtons.setVisibility(View.GONE);
            } else if (Constants.REQUEST_COLLECTED.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.status_collected);
                bgColor = ContextCompat.getColor(context, R.color.status_collected_bg);
                layoutActionButtons.setVisibility(View.GONE);
            } else { // Rejected
                textColor = ContextCompat.getColor(context, R.color.status_rejected);
                bgColor = ContextCompat.getColor(context, R.color.status_rejected_bg);
                layoutActionButtons.setVisibility(View.GONE);
            }

            tvDonorRequestStatus.setTextColor(textColor);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(50f);
            shape.setColor(bgColor);
            tvDonorRequestStatus.setBackground(shape);
        }
    }
}
