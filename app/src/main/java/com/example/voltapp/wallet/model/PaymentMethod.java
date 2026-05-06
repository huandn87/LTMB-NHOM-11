package com.example.voltapp.wallet.model;

import com.example.voltapp.R;

import androidx.annotation.ColorRes;

public class PaymentMethod {
    private final String id;
    private final String name;
    private final String description;
    private final String badgeText;
    @ColorRes
    private final int badgeColorRes;
    private final int logoResId;
    private final boolean enabled;

    public PaymentMethod(String id, String name, String description, String badgeText, int badgeColorRes) {
        this(id, name, description, badgeText, badgeColorRes, 0, true);
    }

    public PaymentMethod(String id, String name, String description, String badgeText, int badgeColorRes, int logoResId) {
        this(id, name, description, badgeText, badgeColorRes, logoResId, true);
    }

    public PaymentMethod(String id, String name, String description, String badgeText, int badgeColorRes, int logoResId, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.badgeText = badgeText;
        this.badgeColorRes = badgeColorRes;
        this.logoResId = logoResId;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public int getBadgeColorRes() {
        return badgeColorRes;
    }

    public int getLogoResId() {
        return logoResId;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
