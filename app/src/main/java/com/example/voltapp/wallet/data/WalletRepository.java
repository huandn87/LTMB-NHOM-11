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
    private static final List<WalletTransaction> transactions = new ArrayList<>();
    private static long balance = 100_000L;
    private static boolean defaultDataInitialized = false;

    public WalletRepository() {
        initDefaultPaymentMethodsIfNeeded();
        initDefaultTransactionsIfNeeded();
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

    private void initDefaultTransactionsIfNeeded() {
        if (defaultDataInitialized) {
            return;
        }

        transactions.add(new WalletTransaction(
                idGenerator.incrementAndGet(),
                "Trạm sạc số 1",
                "Thanh toán phiên sạc",
                125_000L,
                TransactionType.DEBIT,
                "05/12/2025 - 10:00"
        ));
        transactions.add(new WalletTransaction(
                idGenerator.incrementAndGet(),
                "Nạp ví EVCharger",
                "Nạp tiền thành công",
                125_000L,
                TransactionType.CREDIT,
                "05/12/2025 - 09:50"
        ));
        transactions.add(new WalletTransaction(
                idGenerator.incrementAndGet(),
                "Trạm sạc số 1",
                "Thanh toán phiên sạc",
                125_000L,
                TransactionType.DEBIT,
                "05/12/2025 - 10:00"
        ));

        defaultDataInitialized = true;
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

        HttpURLConnection connection = null;
        try {
            URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("apikey", SupabaseClientProvider.SUPABASE_KEY);
            connection.setRequestProperty("Authorization", "Bearer " + SupabaseClientProvider.SUPABASE_KEY);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

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
            List<Pttt> rows = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                Pttt pttt = Pttt.fromJson(item);
                rows.add(pttt);
                methods.add(mapToPaymentMethod(pttt));
            }

            Log.d("PTTT_TEST", "Data from Supabase: " + rows);
            Log.d("PTTT_TEST", "Methods size: " + methods.size());

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

    public List<WalletTransaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public TopUpReceipt topUp(long amount, String methodId) {
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

        balance += amount;
        transactions.add(0, new WalletTransaction(
                idGenerator.incrementAndGet(),
                "Nạp ví EVCharger",
                "Nạp qua " + method.getName(),
                amount,
                TransactionType.CREDIT,
                now()
        ));
        return new TopUpReceipt(amount, method.getName());
    }

    private String now() {
        return new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault()).format(new Date());
    }

    private String readStream(InputStream inputStream) throws IOException {
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