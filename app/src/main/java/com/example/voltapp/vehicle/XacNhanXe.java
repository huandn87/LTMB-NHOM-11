package com.example.voltapp.vehicle;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;
import com.google.android.material.button.MaterialButton;
import com.example.voltapp.account.MyCarsActivity;
import com.example.voltapp.home.ManHinhChinh;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.example.voltapp.account.api.SupabaseService;
import org.json.JSONObject;

public class XacNhanXe extends AppCompatActivity {
    private final SupabaseService api = new SupabaseService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xac_nhan_xe);

        // Nút Back
        ImageView btnBack = findViewById(R.id.BTN_BACK_XAC_NHAN);
        btnBack.setOnClickListener(v -> finish());

        // Nhận dữ liệu truyền từ trang Chọn Model
        String tenHang = getIntent().getStringExtra("TEN_HANG");
        String tenModel = getIntent().getStringExtra("TEN_MODEL");

        if (tenHang != null) {
            ((TextView) findViewById(R.id.TXT_HANG_XE_XAC_NHAN)).setText(tenHang);
            ((TextView) findViewById(R.id.TXT_MODEL_XE_XAC_NHAN)).setText(tenModel);
        }

        // Bấm "Thêm mới" -> Lưu vào Supabase và chuyển sang màn hình Loading
        MaterialButton btnThemMoi = findViewById(R.id.BTN_THEM_MOI_XE);
        btnThemMoi.setOnClickListener(v -> {
            ensureCustomerExistsAndSave(tenHang, tenModel);
        });
    }

    private void ensureCustomerExistsAndSave(String brand, String model) {
        SharedPreferences prefs = getSharedPreferences("evcharge_prefs", MODE_PRIVATE);
        int accountId = prefs.getInt("account_id", 0);

        // Tạo bản ghi khachhang trước để thỏa mãn Foreign Key
        JSONObject customerJson = new JSONObject();
        try {
            customerJson.put("customer_id", accountId);
            customerJson.put("account_id", accountId);
            customerJson.put("name", prefs.getString("username", "Người dùng"));
            customerJson.put("phone", prefs.getString("user_phone", "0779497860"));
        } catch (Exception e) { e.printStackTrace(); }

        api.post("khachhang", customerJson.toString(), new SupabaseService.ApiCallback() {
            @Override
            public void onSuccess(String json) {
                // Đã tạo thành công hoặc đã tồn tại
                saveVehicleToSupabase(brand, model);
            }

            @Override
            public void onError(String message) {
                // Nếu lỗi do đã tồn tại (409) thì vẫn tiếp tục lưu xe
                if (message.contains("409")) {
                    saveVehicleToSupabase(brand, model);
                } else {
                    saveVehicleToSupabase(brand, model); // Thử lưu xe luôn, có thể DB đã có sẵn
                }
            }
        });
    }

    private void saveVehicleToSupabase(String brand, String model) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang lưu thông tin xe...");
        pd.show();

        SharedPreferences prefs = getSharedPreferences("evcharge_prefs", MODE_PRIVATE);
        int accountId = prefs.getInt("account_id", 0);

        try {
            JSONObject json = new JSONObject();
            json.put("customer_id", accountId);
            json.put("manufacturer", brand);
            json.put("name", model);
            json.put("charge_standard_id", 1); // Giá trị mặc định do UI chưa có
            json.put("type", "car"); // Giá trị mặc định
            json.put("battery_capacity", 50.0); // Giá trị mặc định

            api.post("phuongtien", json.toString(), new SupabaseService.ApiCallback() {
                @Override
                public void onSuccess(String jsonResponse) {
                    runOnUiThread(() -> {
                        pd.dismiss();
                        Intent intent = new Intent(XacNhanXe.this, MyCarsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        pd.dismiss();
                        android.util.Log.e("XacNhanXe", "Lỗi lưu xe: " + message);
                        Toast.makeText(XacNhanXe.this, "Lỗi lưu xe (ID=" + accountId + "): " + message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } catch (Exception e) {
            pd.dismiss();
            Toast.makeText(this, "Lỗi tạo dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}