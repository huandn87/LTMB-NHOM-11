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

    public void initUserWallet(int accountId) {
        executorService.execute(() -> {
            try {
                Log.d("WalletVM", "Khởi tạo ví cho account: " + accountId);
                // 1. Tìm customer_id từ account_id
                String url = com.example.voltapp.wallet.data.SupabaseClientProvider.SUPABASE_URL + "/rest/v1/khachhang?account_id=eq." + accountId + "&select=customer_id";
                java.net.HttpURLConnection conn = repository.createConnection(url, "GET");
                
                int code = conn.getResponseCode();
                if (code == 200) {
                    String body = repository.readStream(conn.getInputStream());
                    org.json.JSONArray arr = new org.json.JSONArray(body);
                    if (arr.length() > 0) {
                        int customerId = arr.getJSONObject(0).getInt("customer_id");
                        repository.setCustomerId(customerId);
                        
                        // 2. Tải số dư và giao dịch
                        long balance = repository.getBalanceFromSupabase();
                        List<com.example.voltapp.wallet.model.WalletTransaction> txs = repository.getTransactionsFromSupabase();
                        
                        Log.d("WalletVM", "Tải xong: Balance=" + balance + ", Txs=" + txs.size());
                        
                        mainHandler.post(() -> {
                            WalletUiState current = getCurrentState();
                            uiState.setValue(new WalletUiState(
                                    balance,
                                    current.getPaymentMethods(),
                                    txs,
                                    current.getSelectedAmount(),
                                    current.getSelectedMethodId(),
                                    current.getTopUpReceipt()
                            ));
                        });
                    } else {
                        Log.e("WalletVM", "Không tìm thấy khách hàng cho accountId: " + accountId);
                    }
                } else {
                    Log.e("WalletVM", "Lỗi API khachhang: " + code);
                }
            } catch (Exception e) {
                Log.e("WalletVM", "Lỗi khởi tạo ví người dùng", e);
            }
        });
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
