package com.example.voltapp.wallet.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.voltapp.databinding.FragmentTopUpAmountBinding;
import com.example.voltapp.wallet.WalletFeatureActivity;
import com.example.voltapp.wallet.model.PaymentMethod;
import com.example.voltapp.wallet.ui.WalletUiState;
import com.example.voltapp.wallet.ui.WalletViewModel;
import com.example.voltapp.wallet.ui.adapter.MethodSelectAdapter;
import com.example.voltapp.wallet.util.CurrencyUtils;

import java.util.Random;

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
                WalletUiState currentState = viewModel().getUiState().getValue();
                long currentAmount = currentState == null ? 0L : currentState.getSelectedAmount();

                String val = s.toString().trim();
                if (!val.isEmpty()) {
                    try {
                        long amount = Long.parseLong(val);
                        if (amount != currentAmount) {
                            viewModel().setAmount(amount);
                        }
                    } catch (NumberFormatException ignored) {}
                } else if (currentAmount != 0) {
                    viewModel().setAmount(0L);
                }
            }
        });

        binding.btnContinue.setOnClickListener(v -> showConfirmPaymentDialog());
    }

    private void showConfirmPaymentDialog() {
        WalletUiState state = viewModel().getUiState().getValue();
        if (state == null || state.getSelectedAmount() <= 0) {
            Toast.makeText(requireContext(), "Vui lòng nhập số tiền cần nạp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (state.getSelectedMethodId() == null || state.getSelectedMethodId().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountText = CurrencyUtils.formatVnd(state.getSelectedAmount());
        String methodName = getSelectedPaymentMethodName(state);

        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận thanh toán")
                .setMessage("Bạn có chắc chắn muốn nạp " + amountText
                        + " vào ví EVCharger bằng " + methodName
                        + " không?\n\nHệ thống sẽ gửi mã OTP để xác thực giao dịch trước khi thanh toán.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Gửi OTP", (dialog, which) -> sendOtpAndShowDialog())
                .show();
    }

    private void sendOtpAndShowDialog() {
        String otpCode = generateOtpCode();

        Toast.makeText(requireContext(), "OTP demo đã gửi: " + otpCode, Toast.LENGTH_LONG).show();

        showOtpInputDialog(otpCode);
    }

    private void showOtpInputDialog(String otpCode) {
        EditText edtOtp = new EditText(requireContext());
        edtOtp.setHint("Nhập mã OTP 4 số");
        edtOtp.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtOtp.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        edtOtp.setSingleLine(true);
        edtOtp.setPadding(32, 24, 32, 24);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, 8, padding, 0);
        container.addView(edtOtp);

        AlertDialog otpDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Xác thực OTP")
                .setMessage("Mã OTP đã được gửi đến số điện thoại của bạn. Vui lòng nhập mã OTP để hoàn tất thanh toán.")
                .setView(container)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", null)
                .create();

        otpDialog.setOnShowListener(dialog -> {
            Button confirmButton = otpDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            confirmButton.setOnClickListener(v -> {
                String userOtp = edtOtp.getText().toString().trim();

                if (userOtp.isEmpty()) {
                    edtOtp.setError("Vui lòng nhập OTP");
                    return;
                }

                if (!userOtp.equals(otpCode)) {
                    edtOtp.setError("OTP không đúng");
                    Toast.makeText(requireContext(), "Mã OTP không đúng, vui lòng kiểm tra lại", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = viewModel().confirmTopUp();
                if (success) {
                    otpDialog.dismiss();
                    Toast.makeText(requireContext(), "Xác nhận thanh toán thành công", Toast.LENGTH_SHORT).show();
                    activityHost().openSuccess();
                } else {
                    Toast.makeText(requireContext(), "Không thể thanh toán. Vui lòng kiểm tra lại thông tin", Toast.LENGTH_SHORT).show();
                }
            });
        });

        otpDialog.show();
    }

    private String generateOtpCode() {
        int number = new Random().nextInt(9000) + 1000;
        return String.valueOf(number);
    }

    private String getSelectedPaymentMethodName(WalletUiState state) {
        for (PaymentMethod method : state.getPaymentMethods()) {
            if (method.getId().equals(state.getSelectedMethodId())) {
                return method.getName();
            }
        }
        return "phương thức đã chọn";
    }

    private void observeState() {
        viewModel().getUiState().observe(getViewLifecycleOwner(), state -> {
            String currentStr = binding.etAmount.getText().toString();
            String newStateStr = String.valueOf(state.getSelectedAmount());
            if (!currentStr.equals(newStateStr) && !(currentStr.isEmpty() && state.getSelectedAmount() == 0)) {
                binding.etAmount.setText(newStateStr);
                binding.etAmount.setSelection(newStateStr.length());
            }

            methodAdapter.submitList(state.getPaymentMethods(), state.getSelectedMethodId());

            boolean canConfirm = state.getSelectedAmount() > 0
                    && state.getSelectedMethodId() != null
                    && !state.getSelectedMethodId().trim().isEmpty();
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