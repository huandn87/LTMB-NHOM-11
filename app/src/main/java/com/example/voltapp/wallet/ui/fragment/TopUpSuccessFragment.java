package com.example.voltapp.wallet.ui.fragment;

import com.example.voltapp.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.voltapp.wallet.WalletFeatureActivity;
import com.example.voltapp.databinding.FragmentTopUpSuccessBinding;
import com.example.voltapp.wallet.model.TopUpReceipt;
import com.example.voltapp.wallet.ui.WalletViewModel;
import com.example.voltapp.wallet.util.CurrencyUtils;

public class TopUpSuccessFragment extends Fragment {
    private FragmentTopUpSuccessBinding binding;

    private WalletFeatureActivity activityHost() {
        return (WalletFeatureActivity) requireActivity();
    }

    private WalletViewModel viewModel() {
        return activityHost().getWalletViewModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTopUpSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeState();
        binding.btnOk.setOnClickListener(v -> {
            viewModel().clearTopUpForm();
            viewModel().consumeReceipt();
            activityHost().openWalletHome(true);
        });
    }

    private void observeState() {
        viewModel().getUiState().observe(getViewLifecycleOwner(), state -> {
            TopUpReceipt receipt = state.getTopUpReceipt();
            if (receipt == null) {
                return;
            }
            binding.tvDescription.setText("Bạn vừa nạp "
                    + CurrencyUtils.formatVnd(receipt.getAmount())
                    + " vào ví EVCharger bằng "
                    + receipt.getMethodName()
                    + ".");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
