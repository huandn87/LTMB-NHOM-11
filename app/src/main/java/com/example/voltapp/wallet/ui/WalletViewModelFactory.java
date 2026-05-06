package com.example.voltapp.wallet.ui;

import com.example.voltapp.R;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.voltapp.wallet.data.WalletRepository;

public class WalletViewModelFactory implements ViewModelProvider.Factory {
    private final WalletRepository repository;

    public WalletViewModelFactory(WalletRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(WalletViewModel.class)) {
            return (T) new WalletViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
