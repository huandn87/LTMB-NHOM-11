package com.example.voltapp.wallet.ui.adapter;

import com.example.voltapp.R;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voltapp.R;
import com.example.voltapp.databinding.ItemMethodSelectBinding;
import com.example.voltapp.wallet.model.PaymentMethod;

import java.util.ArrayList;
import java.util.List;

public class MethodSelectAdapter extends RecyclerView.Adapter<MethodSelectAdapter.MethodSelectViewHolder> {
    public interface OnMethodClickListener {
        void onClick(PaymentMethod method);
    }

    private final List<PaymentMethod> items = new ArrayList<>();
    private final OnMethodClickListener onClickListener;
    private String selectedMethodId;

    public MethodSelectAdapter(OnMethodClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void submitList(List<PaymentMethod> data, String selectedId) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        selectedMethodId = selectedId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MethodSelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMethodSelectBinding binding = ItemMethodSelectBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MethodSelectViewHolder(binding, onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MethodSelectViewHolder holder, int position) {
        PaymentMethod item = items.get(position);
        holder.bind(item, item.getId().equals(selectedMethodId));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MethodSelectViewHolder extends RecyclerView.ViewHolder {
        private final ItemMethodSelectBinding binding;
        private final OnMethodClickListener onClickListener;

        MethodSelectViewHolder(ItemMethodSelectBinding binding, OnMethodClickListener onClickListener) {
            super(binding.getRoot());
            this.binding = binding;
            this.onClickListener = onClickListener;
        }

        void bind(PaymentMethod item, boolean selected) {
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
            binding.ivSelected.setBackground(ContextCompat.getDrawable(
                    binding.getRoot().getContext(),
                    selected ? R.drawable.bg_circle_selected : R.drawable.bg_circle_unselected
            ));
            binding.getRoot().setOnClickListener(v -> onClickListener.onClick(item));
        }
    }
}
