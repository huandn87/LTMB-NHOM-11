package com.example.voltapp.wallet.ui.adapter;

import com.example.voltapp.R;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voltapp.databinding.ItemMethodOverviewBinding;
import com.example.voltapp.wallet.model.PaymentMethod;

import java.util.ArrayList;
import java.util.List;

public class MethodOverviewAdapter extends RecyclerView.Adapter<MethodOverviewAdapter.MethodViewHolder> {
    private final List<PaymentMethod> items = new ArrayList<>();

    public void submitList(List<PaymentMethod> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMethodOverviewBinding binding = ItemMethodOverviewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MethodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MethodViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MethodViewHolder extends RecyclerView.ViewHolder {
        private final ItemMethodOverviewBinding binding;

        MethodViewHolder(ItemMethodOverviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PaymentMethod item) {
            if (item.getLogoResId() != 0) {
                binding.ivLogo.setVisibility(android.view.View.VISIBLE);
                binding.ivLogo.setImageResource(item.getLogoResId());
                binding.tvBadge.setVisibility(android.view.View.GONE);
            } else {
                binding.ivLogo.setVisibility(android.view.View.GONE);
                binding.tvBadge.setVisibility(android.view.View.VISIBLE);
                binding.tvBadge.setText(item.getBadgeText());
                binding.tvBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(binding.getRoot().getContext(), item.getBadgeColorRes())
                ));
            }
            binding.tvMethodName.setText(item.getName());
            binding.tvMethodDescription.setText(item.getDescription());
            binding.tvStatus.setText(item.isEnabled() ? "Đã kích hoạt" : "Chưa kích hoạt");
        }
    }
}
