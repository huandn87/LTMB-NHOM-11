package com.example.voltapp.wallet.model;

import com.example.voltapp.R;

public class WalletTransaction {
    private final long id;
    private final String title;
    private final String subtitle;
    private final long amount;
    private final TransactionType type;
    private final String createdAt;

    public WalletTransaction(long id, String title, String subtitle, long amount, TransactionType type, String createdAt) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.type = type;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
