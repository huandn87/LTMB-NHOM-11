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
import org.json.JSONArray;
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

        // Nhận dữ liệu truyền từ trang Chọn Model - Đánh dấu final để dùng trong lambda setOnClickListener
        final String tenHang = getIntent().getStringExtra("TEN_HANG");
        final String tenModel = getIntent().getStringExtra("TEN_MODEL");

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

    private void ensureCustomerExistsAndSave(final String brand, final String model) {
        Intent intent = getIntent();
        final int modelYear = intent.getIntExtra("MODEL_YEAR", 2023);
        String typeRaw = intent.getStringExtra("TYPE");
        final String type = (typeRaw == null) ? "car" : typeRaw;
        final double battery = intent.getDoubleExtra("BATTERY_CAPACITY", 50.0);
        final int chargeStandardId = intent.getIntExtra("CHARGE_STANDARD_ID", 1);

        SharedPreferences prefs = getSharedPreferences("evcharge_prefs", MODE_PRIVATE);
        final int accountId = prefs.getInt("account_id", 0);

        if (accountId == 0) {
            Toast.makeText(this, "Lỗi: Bạn chưa đăng nhập hoặc phiên làm việc hết hạn.", Toast.LENGTH_LONG).show();
            return;
        }

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang kiểm tra thông tin khách hàng...");
        pd.setCancelable(false);
        pd.show();

        // Bước 1: Kiểm tra xem đã có bản ghi khachhang chưa
        api.get("khachhang?account_id=eq." + accountId, new SupabaseService.ApiCallback() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONArray arr = new JSONArray(json);
                    if (arr.length() > 0) {
                        // Đã có, lấy customer_id và lưu xe
                        int customerId = arr.getJSONObject(0).getInt("customer_id");
                        runOnUiThread(() -> {
                            pd.setMessage("Đang lưu thông tin xe...");
                            saveVehicleToSupabase(brand, model, modelYear, type, battery, chargeStandardId, customerId, pd);
                        });
                    } else {
                        // Chưa có, tạo mới
                        runOnUiThread(() -> {
                            pd.setMessage("Đang khởi tạo hồ sơ khách hàng...");
                            createNewCustomerAndSave(brand, model, modelYear, type, battery, chargeStandardId, accountId, pd);
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        pd.dismiss();
                        Toast.makeText(XacNhanXe.this, "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    pd.dismiss();
                    Toast.makeText(XacNhanXe.this, "Lỗi kết nối máy chủ: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void createNewCustomerAndSave(final String brand, final String model, final int modelYear, final String type, final double battery, final int chargeStandardId, final int accountId, final ProgressDialog pd) {
        SharedPreferences prefs = getSharedPreferences("evcharge_prefs", MODE_PRIVATE);
        JSONObject customerJson = new JSONObject();
        try {
            customerJson.put("account_id", accountId);
            customerJson.put("name", prefs.getString("username", "Người dùng " + accountId));
            customerJson.put("phone", prefs.getString("user_phone", ""));
        } catch (Exception e) { e.printStackTrace(); }

        api.post("khachhang", customerJson.toString(), new SupabaseService.ApiCallback() {
            @Override
            public void onSuccess(String json) {
                try {
                    // Supabase POST với Prefer: return=representation trả về mảng
                    JSONArray arr = new JSONArray(json);
                    if (arr.length() > 0) {
                        int customerId = arr.getJSONObject(0).getInt("customer_id");
                        runOnUiThread(() -> {
                            pd.setMessage("Đang lưu thông tin xe...");
                            saveVehicleToSupabase(brand, model, modelYear, type, battery, chargeStandardId, customerId, pd);
                        });
                    } else {
                        // Nếu không trả về dữ liệu, thử query lại để lấy customer_id
                        retryGetCustomerAndSave(brand, model, modelYear, type, battery, chargeStandardId, accountId, pd);
                    }
                } catch (Exception e) {
                    retryGetCustomerAndSave(brand, model, modelYear, type, battery, chargeStandardId, accountId, pd);
                }
            }

            @Override
            public void onError(String message) {
                if (message != null && message.contains("409")) {
                    String phone = prefs.getString("user_phone", "");
                    if (!phone.isEmpty()) {
                        String queryPhone = phone;
                        try { queryPhone = java.net.URLEncoder.encode(phone, "UTF-8"); } catch (Exception ignored) {}
                        
                        api.get("khachhang?phone=eq." + queryPhone, new SupabaseService.ApiCallback() {
                            @Override
                            public void onSuccess(String json2) {
                                try {
                                    JSONArray arr = new JSONArray(json2);
                                    if (arr.length() > 0) {
                                        int existingCustomerId = arr.getJSONObject(0).getInt("customer_id");
                                        runOnUiThread(() -> saveVehicleToSupabase(brand, model, modelYear, type, battery, chargeStandardId, existingCustomerId, pd));
                                    } else {
                                        runOnUiThread(() -> {
                                            pd.dismiss();
                                            Toast.makeText(XacNhanXe.this, "Lỗi: Không tìm thấy hồ sơ cũ (409)", Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                } catch (Exception e) {
                                    runOnUiThread(() -> { pd.dismiss(); Toast.makeText(XacNhanXe.this, "Lỗi phân tích: " + e.getMessage(), Toast.LENGTH_SHORT).show(); });
                                }
                            }
                            @Override
                            public void onError(String msg) {
                                runOnUiThread(() -> { pd.dismiss(); Toast.makeText(XacNhanXe.this, "Lỗi tải hồ sơ cũ: " + msg, Toast.LENGTH_SHORT).show(); });
                            }
                        });
                        return;
                    }
                }

                runOnUiThread(() -> {
                    pd.dismiss();
                    android.util.Log.e("XacNhanXe", "Lỗi tạo hồ sơ khách hàng: " + message);
                    Toast.makeText(XacNhanXe.this, "Không thể tạo hồ sơ khách hàng: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void retryGetCustomerAndSave(final String brand, final String model, final int modelYear, final String type, final double battery, final int chargeStandardId, final int accountId, final ProgressDialog pd) {
        api.get("khachhang?account_id=eq." + accountId, new SupabaseService.ApiCallback() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONArray arr = new JSONArray(json);
                    if (arr.length() > 0) {
                        int customerId = arr.getJSONObject(0).getInt("customer_id");
                        runOnUiThread(() -> saveVehicleToSupabase(brand, model, modelYear, type, battery, chargeStandardId, customerId, pd));
                    } else {
                        runOnUiThread(() -> {
                            pd.dismiss();
                            Toast.makeText(XacNhanXe.this, "Lỗi: Không tìm thấy hồ sơ khách hàng sau khi tạo.", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        pd.dismiss();
                        Toast.makeText(XacNhanXe.this, "Lỗi phân tích dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    pd.dismiss();
                    Toast.makeText(XacNhanXe.this, "Lỗi truy vấn hồ sơ: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveVehicleToSupabase(final String brand, final String model, final int modelYear, final String type, final double battery, final int chargeStandardId, final int customerId, final ProgressDialog pd) {
        // Workaround cho lỗi "duplicate key value violates unique constraint phuongtien_pkey"
        // Lấy vehicle_id lớn nhất hiện có và cộng thêm 1
        api.get("phuongtien?order=vehicle_id.desc&limit=1", new SupabaseService.ApiCallback() {
            @Override
            public void onSuccess(String json) {
                int nextId = 1;
                try {
                    JSONArray arr = new JSONArray(json);
                    if (arr.length() > 0) {
                        nextId = arr.getJSONObject(0).getInt("vehicle_id") + 1;
                    }
                } catch (Exception e) {
                    // Nếu lỗi, thử dùng timestamp làm ID tạm thời (phải đảm bảo không quá lớn cho kiểu INT)
                    nextId = (int) (System.currentTimeMillis() % 1000000000L);
                }
                
                final int finalVehicleId = nextId;
                runOnUiThread(() -> performActualSave(brand, model, modelYear, type, battery, chargeStandardId, customerId, finalVehicleId, pd));
            }

            @Override
            public void onError(String message) {
                // Nếu không lấy được max ID, thử lưu không ID (có thể vẫn lỗi nếu sequence hỏng)
                runOnUiThread(() -> performActualSave(brand, model, modelYear, type, battery, chargeStandardId, customerId, -1, pd));
            }
        });
    }

    private void performActualSave(String brand, String model, int modelYear, String type, double battery, int chargeStandardId, int customerId, int vehicleId, ProgressDialog pd) {
        try {
            JSONObject json = new JSONObject();
            if (vehicleId != -1) {
                json.put("vehicle_id", vehicleId);
            }
            json.put("customer_id", customerId);
            json.put("manufacturer", brand);
            json.put("name", model);
            json.put("model_year", modelYear);
            json.put("charge_standard_id", chargeStandardId);
            json.put("type", type);
            json.put("battery_capacity", battery);

            api.post("phuongtien", json.toString(), new SupabaseService.ApiCallback() {
                @Override
                public void onSuccess(String jsonResponse) {
                    runOnUiThread(() -> {
                        if (pd != null && pd.isShowing()) pd.dismiss();
                        Toast.makeText(XacNhanXe.this, "Thêm xe thành công!", Toast.LENGTH_SHORT).show();
                        
                        boolean isFromRegister = getIntent().getBooleanExtra("IS_FROM_REGISTER", false);
                        Intent intent;
                        if (isFromRegister) {
                            intent = new Intent(XacNhanXe.this, com.example.voltapp.home.ManHinhChinh.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        } else {
                            intent = new Intent(XacNhanXe.this, com.example.voltapp.account.TaiKhoanActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        }
                        
                        startActivity(intent);
                        finish();
                    });
                }

                @Override
                public void onError(final String message) {
                    runOnUiThread(() -> {
                        if (pd != null && pd.isShowing()) pd.dismiss();
                        android.util.Log.e("XacNhanXe", "Lỗi lưu xe: " + message);
                        Toast.makeText(XacNhanXe.this, "Lỗi lưu xe: " + message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } catch (Exception e) {
            if (pd != null && pd.isShowing()) pd.dismiss();
            Toast.makeText(this, "Lỗi tạo dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
