package com.example.voltapp.wallet.data;

import android.util.Log;

import com.example.voltapp.R;
import com.example.voltapp.wallet.model.PaymentMethod;
import com.example.voltapp.wallet.model.Pttt;
import com.example.voltapp.wallet.model.TopUpReceipt;
import com.example.voltapp.wallet.model.TransactionType;
import com.example.voltapp.wallet.model.WalletTransaction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class WalletRepository {

    // Dữ liệu ví cần được giữ nguyên trong suốt lượt chạy app.
    // Vì WalletFeatureActivity có thể được tạo lại khi chuyển tab/tài khoản,
    // nếu dùng biến thường thì số dư và lịch sử sẽ bị reset.
    // Dùng static để dữ liệu tồn tại trong RAM cho đến khi app bị tắt hoàn toàn.
    private static final AtomicLong idGenerator = new AtomicLong(System.currentTimeMillis());

    private static List<PaymentMethod> paymentMethods = new ArrayList<>();
    private int customerId = -1;
    private long balance = 0L;
    private final List<WalletTransaction> transactions = new ArrayList<>();
    private android.content.Context context;

    public WalletRepository(android.content.Context context) {
        this.context = context;
        initDefaultPaymentMethodsIfNeeded();
        loadBalanceFromCache();
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
        loadBalanceFromCache();
        loadTransactionsFromCache();
    }

    private void loadBalanceFromCache() {
        if (context != null) {
            android.content.SharedPreferences prefs = context.getSharedPreferences("wallet_prefs_" + customerId, android.content.Context.MODE_PRIVATE);
            this.balance = prefs.getLong("last_balance", 0L);
        }
    }

    private void saveBalanceToCache(long balance) {
        if (context != null) {
            android.content.SharedPreferences prefs = context.getSharedPreferences("wallet_prefs_" + customerId, android.content.Context.MODE_PRIVATE);
            prefs.edit().putLong("last_balance", balance).apply();
        }
    }

    private void loadTransactionsFromCache() {
        if (context == null || customerId == -1) return;
        android.content.SharedPreferences prefs = context.getSharedPreferences("wallet_prefs_" + customerId, android.content.Context.MODE_PRIVATE);
        String json = prefs.getString("tx_cache", "[]");
        try {
            JSONArray arr = new JSONArray(json);
            transactions.clear();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                transactions.add(new WalletTransaction(
                    o.optLong("id"),
                    o.optString("title"),
                    o.optString("subtitle"),
                    o.optLong("amount"),
                    TransactionType.valueOf(o.optString("type", "CREDIT")),
                    o.optString("created_at")
                ));
            }
        } catch (Exception e) {
            Log.e("WalletRepo", "Lỗi tải cache giao dịch: " + e.getMessage());
        }
    }

    private void saveTransactionsToCache() {
        if (context == null || customerId == -1) return;
        android.content.SharedPreferences prefs = context.getSharedPreferences("wallet_prefs_" + customerId, android.content.Context.MODE_PRIVATE);
        try {
            JSONArray arr = new JSONArray();
            for (WalletTransaction tx : transactions) {
                if (tx.getId() == 0) continue; 
                JSONObject o = new JSONObject();
                o.put("id", tx.getId());
                o.put("title", tx.getTitle());
                o.put("subtitle", tx.getSubtitle());
                o.put("amount", tx.getAmount());
                o.put("type", tx.getType().name());
                o.put("created_at", tx.getCreatedAt());
                arr.put(o);
            }
            prefs.edit().putString("tx_cache", arr.toString()).apply();
        } catch (Exception e) {
            Log.e("WalletRepo", "Lỗi lưu cache giao dịch: " + e.getMessage());
        }
    }

    private void initDefaultPaymentMethodsIfNeeded() {
        if (!paymentMethods.isEmpty()) {
            return;
        }

        paymentMethods.add(new PaymentMethod("zalopay", "ZaloPay", "Ví liên kết nhanh", "ZP", R.color.badge_zalopay, R.drawable.ic_zalopay));
        paymentMethods.add(new PaymentMethod("visa", "Thẻ Visa", "Thẻ tín dụng / ghi nợ", "VS", R.color.badge_visa, R.drawable.ic_visa));
        paymentMethods.add(new PaymentMethod("viettel_money", "Viettel Money", "Nạp nhanh từ tài khoản viễn thông", "VT", R.color.badge_viettel, R.drawable.ic_viettel_money));
        paymentMethods.add(new PaymentMethod("momo", "MoMo", "Ví điện tử phổ biến", "MM", R.color.badge_momo, R.drawable.ic_momo));
    }

    public long getBalanceFromSupabase() throws Exception {
        if (customerId == -1) return 0L;
        
        String endpoint = SupabaseClientProvider.SUPABASE_URL + "/rest/v1/khachhang?customer_id=eq." + customerId + "&select=balance";
        HttpURLConnection conn = createConnection(endpoint, "GET");
        
        int code = conn.getResponseCode();
        if (code == 200) {
            String body = readStream(conn.getInputStream());
            JSONArray arr = new JSONArray(body);
            if (arr.length() > 0) {
                this.balance = arr.getJSONObject(0).optLong("balance", 0L);
                saveBalanceToCache(this.balance);
            }
        }
        return this.balance;
    }

    public long getBalance() {
        return balance;
    }

    public List<PaymentMethod> getPaymentMethods() {
        return new ArrayList<>(paymentMethods);
    }

    public List<PaymentMethod> getPaymentMethodsFromSupabase() throws Exception {
        String endpoint = SupabaseClientProvider.SUPABASE_URL
                + "/rest/v1/pttt?select=method_id,name,description,status&order=method_id.asc";

        HttpURLConnection connection = createConnection(endpoint, "GET");
        try {
            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readStream(stream);

            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("Supabase error " + responseCode + ": " + body);
            }

            JSONArray array = new JSONArray(body);
            List<PaymentMethod> methods = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                Pttt pttt = Pttt.fromJson(item);
                methods.add(mapToPaymentMethod(pttt));
            }

            if (!methods.isEmpty()) {
                paymentMethods = methods;
            }
            return getPaymentMethods();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public HttpURLConnection createConnection(String endpoint, String method) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("apikey", SupabaseClientProvider.SUPABASE_KEY);
        connection.setRequestProperty("Authorization", "Bearer " + SupabaseClientProvider.SUPABASE_KEY);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        return connection;
    }

    private PaymentMethod mapToPaymentMethod(Pttt item) {
        String name = item.getName() == null ? "" : item.getName();
        String badgeText = name.length() >= 2 ? name.substring(0, 2).toUpperCase(Locale.ROOT) : name.toUpperCase(Locale.ROOT);
        if (badgeText.trim().isEmpty()) {
            badgeText = "PT";
        }

        int colorRes;
        int logoRes;
        switch (item.getMethodId() % 4) {
            case 1:
                colorRes = R.color.badge_zalopay;
                logoRes = R.drawable.ic_zalopay;
                break;
            case 2:
                colorRes = R.color.badge_visa;
                logoRes = R.drawable.ic_visa;
                break;
            case 3:
                colorRes = R.color.badge_viettel;
                logoRes = R.drawable.ic_viettel_money;
                break;
            default:
                colorRes = R.color.badge_momo;
                logoRes = R.drawable.ic_momo;
                break;
        }

        boolean enabled = "active".equalsIgnoreCase(item.getStatus());
        return new PaymentMethod(
                String.valueOf(item.getMethodId()),
                name,
                item.getDescription() == null ? "" : item.getDescription(),
                badgeText,
                colorRes,
                logoRes,
                enabled
        );
    }

    public List<WalletTransaction> getTransactionsFromSupabase() throws Exception {
        if (customerId == -1) return transactions;

        String endpoint = SupabaseClientProvider.SUPABASE_URL + "/rest/v1/giao_dich?customer_id=eq." + customerId + "&order=created_at.desc";
        HttpURLConnection conn = createConnection(endpoint, "GET");
        
        int code = conn.getResponseCode();
        if (code == 200) {
            String body = readStream(conn.getInputStream());
            JSONArray arr = new JSONArray(body);
            transactions.clear();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                
                String typeStr = o.optString("type", "CREDIT").toUpperCase(Locale.ROOT);
                TransactionType type = TransactionType.CREDIT;
                if (typeStr.contains("DEBIT")) type = TransactionType.DEBIT;

                transactions.add(new WalletTransaction(
                    o.optLong("id", i + 1),
                    o.optString("title", "Giao dịch"),
                    o.optString("subtitle", ""),
                    o.optLong("amount", 0),
                    type,
                    o.optString("created_at", "")
                ));
            }
            saveTransactionsToCache();
        } else {
            String errorBody = readStream(conn.getErrorStream());
            Log.e("WalletRepo", "Lỗi tải giao dịch (" + code + "): " + errorBody);
        }
        
        if (transactions.isEmpty()) {
            transactions.add(new WalletTransaction(0, "Chào mừng", "Bắt đầu sử dụng ví", 0, TransactionType.CREDIT, now()));
        }
        
        return transactions;
    }

    public List<WalletTransaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public TopUpReceipt topUp(long amount, String methodId) {
        if (customerId == -1) throw new IllegalStateException("User not logged in");
        
        PaymentMethod method = null;
        for (PaymentMethod item : paymentMethods) {
            if (item.getId().equals(methodId)) {
                method = item;
                break;
            }
        }
        if (method == null) {
            throw new IllegalArgumentException("Payment method not found: " + methodId);
        }

        // 1. Cập nhật số dư trong Supabase
        saveBalanceToCache(balance + amount);
        updateBalanceInSupabase(balance + amount);
        
        // 2. Lưu giao dịch vào Supabase
        saveTransactionToSupabase("Nạp ví EVCharger", "Nạp qua " + method.getName(), amount, TransactionType.CREDIT);

        // 3. Cập nhật local list để hiển thị ngay lập tức
        transactions.add(0, new WalletTransaction(
                System.currentTimeMillis(),
                "Nạp ví EVCharger",
                "Nạp qua " + method.getName(),
                amount,
                TransactionType.CREDIT,
                now()
        ));

        balance += amount;
        return new TopUpReceipt(amount, method.getName());
    }

    private void updateBalanceInSupabase(long newBalance) {
        new Thread(() -> {
            try {
                String endpoint = SupabaseClientProvider.SUPABASE_URL + "/rest/v1/khachhang?customer_id=eq." + customerId;
                HttpURLConnection conn = createConnection(endpoint, "PATCH");
                conn.setDoOutput(true);
                JSONObject json = new JSONObject();
                json.put("balance", newBalance);
                conn.getOutputStream().write(json.toString().getBytes(StandardCharsets.UTF_8));
                int code = conn.getResponseCode();
                if (code >= 400) {
                    Log.e("WalletRepo", "Lỗi cập nhật số dư: " + code);
                }
            } catch (Exception e) {
                Log.e("WalletRepo", "Exception update balance", e);
            }
        }).start();
    }

    private void saveTransactionToSupabase(String title, String subtitle, long amount, TransactionType type) {
        new Thread(() -> {
            try {
                String endpoint = SupabaseClientProvider.SUPABASE_URL + "/rest/v1/giao_dich";
                HttpURLConnection conn = createConnection(endpoint, "POST");
                conn.setDoOutput(true);
                JSONObject json = new JSONObject();
                json.put("customer_id", customerId);
                json.put("title", title);
                json.put("subtitle", subtitle);
                json.put("amount", amount);
                json.put("type", type.name());
                json.put("created_at", now());
                conn.getOutputStream().write(json.toString().getBytes(StandardCharsets.UTF_8));
                int code = conn.getResponseCode();
                if (code >= 400) {
                    Log.e("WalletRepo", "Lỗi lưu giao dịch: " + code);
                }
            } catch (Exception e) {
                Log.e("WalletRepo", "Exception save transaction", e);
            }
        }).start();
    }

    private String now() {
        // Sử dụng định dạng ISO để Postgres có thể nhận diện tốt hơn
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }
}