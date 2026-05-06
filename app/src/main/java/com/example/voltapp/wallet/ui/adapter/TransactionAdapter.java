package com.example.voltapp.wallet.ui.adapter;

import com.example.voltapp.R;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voltapp.R;
import com.example.voltapp.databinding.ItemTransactionBinding;
import com.example.voltapp.wallet.model.TransactionType;
import com.example.voltapp.wallet.model.WalletTransaction;
import com.example.voltapp.wallet.util.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private final List<WalletTransaction> items = new ArrayList<>();

    public void submitList(List<WalletTransaction> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionBinding binding;

        TransactionViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(WalletTransaction item) {
            binding.tvTitle.setText(item.getTitle());
            binding.tvSubtitle.setText(item.getSubtitle());
            binding.tvTime.setText(item.getCreatedAt());

            int color = item.getType() == TransactionType.CREDIT
                    ? ContextCompat.getColor(binding.getRoot().getContext(), R.color.success_green)
                    : ContextCompat.getColor(binding.getRoot().getContext(), R.color.error_red);
            String prefix = item.getType() == TransactionType.CREDIT ? "+" : "-";
            binding.tvAmount.setText(prefix + CurrencyUtils.formatVnd(item.getAmount()));
            binding.tvAmount.setTextColor(color);
        }
    }
}
