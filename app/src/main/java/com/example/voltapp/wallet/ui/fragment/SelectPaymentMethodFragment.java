package com.example.voltapp.wallet.ui.fragment;

import com.example.voltapp.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.voltapp.wallet.WalletFeatureActivity;
import com.example.voltapp.databinding.FragmentSelectPaymentMethodBinding;
import com.example.voltapp.wallet.ui.WalletViewModel;
import com.example.voltapp.wallet.ui.adapter.MethodSelectAdapter;

public class SelectPaymentMethodFragment extends Fragment {
    private FragmentSelectPaymentMethodBinding binding;
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
        binding = FragmentSelectPaymentMethodBinding.inflate(inflater, container, false);
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
        binding.rvMethods.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMethods.setAdapter(methodAdapter);
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnAddMethod.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Demo: thêm phương thức mới sau", Toast.LENGTH_SHORT).show()
        );
        binding.btnContinue.setOnClickListener(v -> {
            boolean success = viewModel().confirmTopUp();
            if (success) {
                activityHost().openSuccess();
            }
        });
    }

    private void observeState() {
        viewModel().getUiState().observe(getViewLifecycleOwner(), state -> {
            methodAdapter.submitList(state.getPaymentMethods(), state.getSelectedMethodId());
            boolean canContinue = state.getSelectedMethodId() != null && !state.getSelectedMethodId().trim().isEmpty();
            binding.btnContinue.setEnabled(canContinue);
            binding.btnContinue.setAlpha(canContinue ? 1f : 0.4f);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
