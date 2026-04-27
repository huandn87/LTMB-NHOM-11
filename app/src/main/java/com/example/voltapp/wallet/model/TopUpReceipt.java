package com.example.voltapp.wallet.model;

import com.example.voltapp.R;

public class TopUpReceipt {
    private final long amount;
    private final String methodName;

    public TopUpReceipt(long amount, String methodName) {
        this.amount = amount;
        this.methodName = methodName;
    }

    public long getAmount() {
        return amount;
    }

    public String getMethodName() {
        return methodName;
    }
}
