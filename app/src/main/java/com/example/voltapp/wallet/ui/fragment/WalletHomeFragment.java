package com.example.voltapp.wallet.ui.fragment;

import com.example.voltapp.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.voltapp.wallet.WalletFeatureActivity;
import com.example.voltapp.databinding.FragmentWalletHomeBinding;
import com.example.voltapp.wallet.ui.WalletViewModel;
import com.example.voltapp.wallet.ui.adapter.MethodOverviewAdapter;
import com.example.voltapp.wallet.ui.adapter.TransactionAdapter;
import com.example.voltapp.wallet.util.CurrencyUtils;

public class WalletHomeFragment extends Fragment {
    private FragmentWalletHomeBinding binding;
    private final MethodOverviewAdapter methodAdapter = new MethodOverviewAdapter();
    private final TransactionAdapter transactionAdapter = new TransactionAdapter();

    private WalletFeatureActivity activityHost() {
        return (WalletFeatureActivity) requireActivity();
    }

    private WalletViewModel viewModel() {
        return activityHost().getWalletViewModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWalletHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerViews();
        setupActions();
        observeState();
    }

    private void setupRecyclerViews() {
        binding.rvMethods.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMethods.setAdapter(methodAdapter);
        binding.rvMethods.setNestedScrollingEnabled(false);

        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(transactionAdapter);
        binding.rvTransactions.setNestedScrollingEnabled(false);
    }

    private void setupActions() {
        binding.btnTopUp.setOnClickListener(v -> {
            viewModel().clearTopUpForm();
            activityHost().openTopUpAmount();
        });
    }

    private void observeState() {
        viewModel().getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.tvBalance.setText(CurrencyUtils.formatVnd(state.getBalance()));
            methodAdapter.submitList(state.getPaymentMethods());
            transactionAdapter.submitList(state.getTransactions());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
