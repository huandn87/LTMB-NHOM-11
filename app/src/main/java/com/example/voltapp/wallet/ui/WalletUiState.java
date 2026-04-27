package com.example.voltapp.wallet.ui;

import com.example.voltapp.R;

import com.example.voltapp.wallet.model.PaymentMethod;
import com.example.voltapp.wallet.model.TopUpReceipt;
import com.example.voltapp.wallet.model.WalletTransaction;

import java.util.ArrayList;
import java.util.List;

public class WalletUiState {
    private final long balance;
    private final List<PaymentMethod> paymentMethods;
    private final List<WalletTransaction> transactions;
    private final long selectedAmount;
    private final String selectedMethodId;
    private final TopUpReceipt topUpReceipt;

    public WalletUiState(long balance,
                         List<PaymentMethod> paymentMethods,
                         List<WalletTransaction> transactions,
                         long selectedAmount,
                         String selectedMethodId,
                         TopUpReceipt topUpReceipt) {
        this.balance = balance;
        this.paymentMethods = paymentMethods == null ? new ArrayList<>() : new ArrayList<>(paymentMethods);
        this.transactions = transactions == null ? new ArrayList<>() : new ArrayList<>(transactions);
        this.selectedAmount = selectedAmount;
        this.selectedMethodId = selectedMethodId;
        this.topUpReceipt = topUpReceipt;
    }

    public long getBalance() {
        return balance;
    }

    public List<PaymentMethod> getPaymentMethods() {
        return new ArrayList<>(paymentMethods);
    }

    public List<WalletTransaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public long getSelectedAmount() {
        return selectedAmount;
    }

    public String getSelectedMethodId() {
        return selectedMethodId;
    }

    public TopUpReceipt getTopUpReceipt() {
        return topUpReceipt;
    }
}
