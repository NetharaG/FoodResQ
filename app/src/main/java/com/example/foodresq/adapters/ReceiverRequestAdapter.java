package com.example.foodresq.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodresq.R;
import com.example.foodresq.models.FoodRequest;
import com.example.foodresq.utils.Constants;
import com.example.foodresq.utils.DateTimeUtil;

import java.util.List;

public class ReceiverRequestAdapter extends RecyclerView.Adapter<ReceiverRequestAdapter.RequestViewHolder> {

    public interface OnReceiverRequestClickListener {
        void onRequestClick(FoodRequest request);
    }

    private final Context context;
    private List<FoodRequest> requestList;
    private final OnReceiverRequestClickListener listener;

    public ReceiverRequestAdapter(Context context, List<FoodRequest> requestList, OnReceiverRequestClickListener listener) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_receiver_request, parent, false);
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
        TextView tvReceiverReqFoodName;
        TextView tvReceiverReqStatus;
        TextView tvReceiverReqLocation;
        TextView tvReceiverReqDate;
        TextView tvActionHint;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReceiverReqFoodName = itemView.findViewById(R.id.tvReceiverReqFoodName);
            tvReceiverReqStatus = itemView.findViewById(R.id.tvReceiverReqStatus);
            tvReceiverReqLocation = itemView.findViewById(R.id.tvReceiverReqLocation);
            tvReceiverReqDate = itemView.findViewById(R.id.tvReceiverReqDate);
            tvActionHint = itemView.findViewById(R.id.tvActionHint);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRequestClick(requestList.get(pos));
                }
            });
        }

        public void bind(FoodRequest request) {
            tvReceiverReqFoodName.setText(!TextUtils.isEmpty(request.getFoodName()) ? request.getFoodName() : "Food Item");
            tvReceiverReqLocation.setText(!TextUtils.isEmpty(request.getPickupLocation()) ? request.getPickupLocation() : "Location not specified");
            tvReceiverReqDate.setText(DateTimeUtil.formatDateTime(request.getRequestTime()));

            String status = request.getStatus();
            if (TextUtils.isEmpty(status)) {
                status = Constants.REQUEST_PENDING;
            }
            tvReceiverReqStatus.setText(status);

            int textColor;
            int bgColor;

            if (Constants.REQUEST_PENDING.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.status_pending);
                bgColor = ContextCompat.getColor(context, R.color.status_pending_bg);
                tvActionHint.setText("Pending Donor Approval >");
                tvActionHint.setTextColor(textColor);
            } else if (Constants.REQUEST_ACCEPTED.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.status_accepted);
                bgColor = ContextCompat.getColor(context, R.color.status_accepted_bg);
                tvActionHint.setText("Ready! Tap to Confirm Collection >");
                tvActionHint.setTextColor(textColor);
            } else if (Constants.REQUEST_COLLECTED.equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.status_collected);
                bgColor = ContextCompat.getColor(context, R.color.status_collected_bg);
                tvActionHint.setText("Collected Successfully ✓");
                tvActionHint.setTextColor(textColor);
            } else { // Rejected
                textColor = ContextCompat.getColor(context, R.color.status_rejected);
                bgColor = ContextCompat.getColor(context, R.color.status_rejected_bg);
                tvActionHint.setText("Declined by Donor");
                tvActionHint.setTextColor(textColor);
            }

            tvReceiverReqStatus.setTextColor(textColor);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(50f);
            shape.setColor(bgColor);
            tvReceiverReqStatus.setBackground(shape);
        }
    }
}
