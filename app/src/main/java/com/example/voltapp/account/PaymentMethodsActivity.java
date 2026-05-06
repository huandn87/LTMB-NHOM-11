package com.example.voltapp.account;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voltapp.R;
import com.example.voltapp.account.model.PaymentMethod;
import java.util.ArrayList;

import com.example.voltapp.account.api.SupabaseService;
import org.json.JSONArray;
import org.json.JSONObject;

public class PaymentMethodsActivity extends AppCompatActivity {
    private final ArrayList<PaymentMethod> paymentMethods = new ArrayList<>();
    private PaymentMethodAdapter adapter;
    private final SupabaseService api = new SupabaseService();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_payment_methods_account);
        getWindow().setStatusBarColor(Color.parseColor("#191B21"));
        findViewById(R.id.btn_back_payment).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_payment).setOnClickListener(v -> Toast.makeText(this, "Thêm phương thức mới", Toast.LENGTH_SHORT).show());
        
        RecyclerView rv = findViewById(R.id.rv_payment_methods);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentMethodAdapter(paymentMethods);
        rv.setAdapter(adapter);

        loadPaymentMethods();
    }

    private void loadPaymentMethods() {
        api.get("pttt", new SupabaseService.ApiCallback() {
            @Override
            public void onSuccess(String json) {
                runOnUiThread(() -> {
                    try {
                        paymentMethods.clear();
                        JSONArray arr = new JSONArray(json);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            String name = o.optString("name", "N/A");
                            String status = o.optString("status", "inactive");
                            int methodId = o.optInt("method_id", 0);
                            
                            int logoRes = getLogoForMethod(methodId, name);
                            paymentMethods.add(new PaymentMethod(name, status.equalsIgnoreCase("active") ? "Đã liên kết" : "Chưa kích hoạt", logoRes));
                        }
                        if (paymentMethods.isEmpty()) addFallback();
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        addFallback();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    addFallback();
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    private int getLogoForMethod(int methodId, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("zalo")) return R.drawable.ic_zalopay;
        if (lowerName.contains("visa")) return R.drawable.ic_visa;
        if (lowerName.contains("viettel")) return R.drawable.ic_viettel_money;
        if (lowerName.contains("momo")) return R.drawable.ic_momo;
        if (lowerName.contains("ví")) return R.drawable.ic_wallet_volt;
        
        // Fallback mapping based on ID
        switch (methodId % 5) {
            case 1: return R.drawable.ic_zalopay;
            case 2: return R.drawable.ic_visa;
            case 3: return R.drawable.ic_viettel_money;
            case 4: return R.drawable.ic_momo;
            default: return R.drawable.ic_wallet_volt;
        }
    }

    private void addFallback() {
        paymentMethods.clear();
        paymentMethods.add(new PaymentMethod("Ví EVCharge", "Đã kích hoạt", R.drawable.ic_wallet_volt));
        paymentMethods.add(new PaymentMethod("ZaloPay", "Đã liên kết", R.drawable.ic_zalopay));
        paymentMethods.add(new PaymentMethod("Thẻ Visa", "Đã liên kết", R.drawable.ic_visa));
        paymentMethods.add(new PaymentMethod("Viettel Money", "Đã liên kết", R.drawable.ic_viettel_money));
        paymentMethods.add(new PaymentMethod("MoMo", "Đã liên kết", R.drawable.ic_momo));
    }
}
