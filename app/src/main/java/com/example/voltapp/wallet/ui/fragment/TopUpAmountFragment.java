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
import com.example.voltapp.databinding.FragmentTopUpAmountBinding;
import com.example.voltapp.wallet.ui.WalletViewModel;
import com.example.voltapp.wallet.ui.adapter.MethodSelectAdapter;
import com.example.voltapp.wallet.util.CurrencyUtils;

public class TopUpAmountFragment extends Fragment {
    private FragmentTopUpAmountBinding binding;
    private MethodSelectAdapter methodAdapter;

    private WalletFeatureActivity activityHost() {
        return (WalletFeatureActivity) requireActivity();
    }

    private WalletViewModel viewModel() {
        return activityHost().getWalletViewModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTopUpAmountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        methodAdapter = new MethodSelectAdapter(method -> viewModel().selectPaymentMethod(method.getId()));
        setupRecyclerView();
        setupActions();
        observeState();
    }

    private void setupRecyclerView() {
        binding.rvMethods.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        binding.rvMethods.setAdapter(methodAdapter);
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        binding.chip10k.setOnClickListener(v -> viewModel().setAmount(10_000L));
        binding.chip20k.setOnClickListener(v -> viewModel().setAmount(20_000L));
        binding.chip30k.setOnClickListener(v -> viewModel().setAmount(30_000L));
        binding.chip50k.setOnClickListener(v -> viewModel().setAmount(50_000L));
        binding.chip100k.setOnClickListener(v -> viewModel().setAmount(100_000L));
        binding.chip200k.setOnClickListener(v -> viewModel().setAmount(200_000L));
        binding.chip300k.setOnClickListener(v -> viewModel().setAmount(300_000L));
        binding.chip400k.setOnClickListener(v -> viewModel().setAmount(400_000L));
        binding.chip500k.setOnClickListener(v -> viewModel().setAmount(500_000L));

        binding.etAmount.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String val = s.toString().trim();
                if (!val.isEmpty()) {
                    try {
                        long amount = Long.parseLong(val);
                        if (amount != viewModel().getUiState().getValue().getSelectedAmount()) {
                            viewModel().setAmount(amount);
                        }
                    } catch (NumberFormatException ignored) {}
                } else if (viewModel().getUiState().getValue().getSelectedAmount() != 0) {
                    viewModel().setAmount(0L);
                }
            }
        });

        binding.btnContinue.setOnClickListener(v -> {
            boolean success = viewModel().confirmTopUp();
            if (success) {
                activityHost().openSuccess();
            }
        });
    }

    private void observeState() {
        viewModel().getUiState().observe(getViewLifecycleOwner(), state -> {
            // Chỉ cập nhật EditText nếu giá trị thực sự thay đổi (để tránh vòng lặp TextWatcher)
            String currentStr = binding.etAmount.getText().toString();
            String newStateStr = String.valueOf(state.getSelectedAmount());
            if (!currentStr.equals(newStateStr) && !(currentStr.isEmpty() && state.getSelectedAmount() == 0)) {
                binding.etAmount.setText(newStateStr);
                binding.etAmount.setSelection(newStateStr.length());
            }

            methodAdapter.submitList(state.getPaymentMethods(), state.getSelectedMethodId());

            boolean canConfirm = state.getSelectedAmount() > 0 && state.getSelectedMethodId() != null && !state.getSelectedMethodId().trim().isEmpty();
            binding.btnContinue.setEnabled(canConfirm);
            binding.btnContinue.setAlpha(canConfirm ? 1f : 0.4f);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
