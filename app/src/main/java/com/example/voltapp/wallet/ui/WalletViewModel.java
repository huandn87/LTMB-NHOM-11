package com.example.voltapp.wallet.ui;

import com.example.voltapp.R;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.voltapp.wallet.data.WalletRepository;
import com.example.voltapp.wallet.model.PaymentMethod;
import com.example.voltapp.wallet.model.TopUpReceipt;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WalletViewModel extends ViewModel {
    private final WalletRepository repository;
    private final MutableLiveData<WalletUiState> uiState = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public WalletViewModel(WalletRepository repository) {
        this.repository = repository;
        uiState.setValue(new WalletUiState(
                repository.getBalance(),
                repository.getPaymentMethods(),
                repository.getTransactions(),
                0L,
                null,
                null
        ));
        loadPaymentMethodsFromSupabase();
    }

    public LiveData<WalletUiState> getUiState() {
        return uiState;
    }

    private void loadPaymentMethodsFromSupabase() {
        executorService.execute(() -> {
            try {
                List<PaymentMethod> methods = repository.getPaymentMethodsFromSupabase();
                Log.d("PTTT_TEST", "Methods data: " + methods);
                mainHandler.post(() -> {
                    WalletUiState current = getCurrentState();
                    String selectedId = methods.isEmpty() ? current.getSelectedMethodId() : methods.get(0).getId();
                    uiState.setValue(new WalletUiState(
                            current.getBalance(),
                            methods,
                            current.getTransactions(),
                            current.getSelectedAmount(),
                            selectedId,
                            current.getTopUpReceipt()
                    ));
                });
            } catch (Exception e) {
                Log.e("PTTT_TEST", "Lỗi lấy phương thức thanh toán", e);
            }
        });
    }

    public void setAmount(long amount) {
        WalletUiState current = getCurrentState();
        uiState.setValue(new WalletUiState(
                current.getBalance(),
                current.getPaymentMethods(),
                current.getTransactions(),
                amount,
                current.getSelectedMethodId(),
                current.getTopUpReceipt()
        ));
    }

    public void selectPaymentMethod(String methodId) {
        WalletUiState current = getCurrentState();
        uiState.setValue(new WalletUiState(
                current.getBalance(),
                current.getPaymentMethods(),
                current.getTransactions(),
                current.getSelectedAmount(),
                methodId,
                current.getTopUpReceipt()
        ));
    }

    public boolean confirmTopUp() {
        WalletUiState current = getCurrentState();
        long amount = current.getSelectedAmount();
        String methodId = current.getSelectedMethodId();
        if (amount <= 0 || methodId == null || methodId.trim().isEmpty()) {
            return false;
        }

        TopUpReceipt receipt = repository.topUp(amount, methodId);
        uiState.setValue(new WalletUiState(
                repository.getBalance(),
                current.getPaymentMethods(),
                repository.getTransactions(),
                current.getSelectedAmount(),
                current.getSelectedMethodId(),
                receipt
        ));
        return true;
    }

    public void clearTopUpForm() {
        WalletUiState current = getCurrentState();
        uiState.setValue(new WalletUiState(
                current.getBalance(),
                current.getPaymentMethods(),
                current.getTransactions(),
                0L,
                null,
                current.getTopUpReceipt()
        ));
    }

    public void consumeReceipt() {
        WalletUiState current = getCurrentState();
        uiState.setValue(new WalletUiState(
                current.getBalance(),
                current.getPaymentMethods(),
                current.getTransactions(),
                current.getSelectedAmount(),
                current.getSelectedMethodId(),
                null
        ));
    }

    private WalletUiState getCurrentState() {
        WalletUiState current = uiState.getValue();
        if (current != null) {
            return current;
        }
        return new WalletUiState(
                repository.getBalance(),
                repository.getPaymentMethods(),
                repository.getTransactions(),
                0L,
                null,
                null
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
