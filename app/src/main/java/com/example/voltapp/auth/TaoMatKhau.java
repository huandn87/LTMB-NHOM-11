package com.example.voltapp.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;
import com.example.voltapp.profile.HoanThienHoSo;
import com.example.voltapp.home.ManHinhChinh;
import com.example.voltapp.vehicle.ChonHang;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class TaoMatKhau extends AppCompatActivity {

    private static final String TAG = "VoltApp_Register";
    private static final String SUPABASE_ANON_KEY = "sb_publishable_Ga562F_Z8kOEFmvkpbPYAw_gYus54p7";
    private static final String REST_URL = "https://xyntcrsfhacvqsuyvbkd.supabase.co/rest/v1/taikhoan";

    private String userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_mat_khau);

        // Nhận số điện thoại từ XacThucOtp
        userPhone = getIntent().getStringExtra("USER_PHONE");
        if (userPhone == null || userPhone.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có thông tin tài khoản", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hiển thị số điện thoại (read-only)
        EditText edtSoDienThoai = findViewById(R.id.EDT_TEN_TAI_KHOAN);
        if (edtSoDienThoai != null) {
            edtSoDienThoai.setText(userPhone);
        }

        TextInputEditText edtUsername = findViewById(R.id.EDT_USERNAME);
        TextInputEditText edtMatKhau = findViewById(R.id.EDT_MAT_KHAU);

        findViewById(R.id.BTN_HOAN_THANH).setOnClickListener(v -> {
            String username = edtUsername.getText() != null ? edtUsername.getText().toString().trim() : "";
            String password = edtMatKhau.getText() != null ? edtMatKhau.getText().toString().trim() : "";

            // Validate tên đăng nhập
            if (TextUtils.isEmpty(username)) {
                edtUsername.setError("Vui lòng nhập tên đăng nhập");
                edtUsername.requestFocus();
                return;
            }
            if (username.length() < 4) {
                edtUsername.setError("Tên đăng nhập phải có ít nhất 4 ký tự");
                edtUsername.requestFocus();
                return;
            }
            if (!username.matches("[a-zA-Z0-9_]+")) {
                edtUsername.setError("Chỉ dùng chữ cái, số và dấu gạch dưới");
                edtUsername.requestFocus();
                return;
            }

            // Validate mật khẩu
            if (TextUtils.isEmpty(password)) {
                edtMatKhau.setError("Vui lòng nhập mật khẩu");
                edtMatKhau.requestFocus();
                return;
            }
            if (password.length() < 6) {
                edtMatKhau.setError("Mật khẩu phải có ít nhất 6 ký tự");
                edtMatKhau.requestFocus();
                return;
            }

            // Kiểm tra username đã tồn tại chưa rồi mới tạo
            kiemTraRoiTao(username, password);
        });
    }

    /**
     * Bước 1: Kiểm tra username đã tồn tại trong DB chưa
     */
    private void kiemTraRoiTao(String username, String password) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(REST_URL + "?username=eq." + username + "&select=username")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TaoMatKhau.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                try {
                    JSONArray arr = new JSONArray(body);
                    if (arr.length() > 0) {
                        // Username đã tồn tại
                        runOnUiThread(() -> {
                            TextInputEditText edtU = findViewById(R.id.EDT_USERNAME);
                            if (edtU != null) edtU.setError("Tên đăng nhập đã được sử dụng");
                            Toast.makeText(TaoMatKhau.this,
                                    "Tên đăng nhập \"" + username + "\" đã tồn tại, vui lòng chọn tên khác",
                                    Toast.LENGTH_LONG).show();
                        });
                    } else {
                        // Username còn trống → tạo tài khoản mới
                        taoTaiKhoanMoi(username, password);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(TaoMatKhau.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    /**
     * Bước 2: INSERT tài khoản mới vào bảng taikhoan
     * Columns: username, password_hash, role='customer', status='active'
     */
    private void taoTaiKhoanMoi(String username, String password) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("password_hash", password);
            json.put("role", "customer");   // Đúng theo DB
            json.put("status", "active");   // Đúng theo DB
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "INSERT payload: " + json.toString());

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(REST_URL)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TaoMatKhau.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "INSERT response code: " + response.code() + " body: " + responseBody);

                if (response.isSuccessful() || response.code() == 201) {
                    // Lấy account_id vừa tạo
                    int newAccountId = 0;
                    try {
                        JSONArray arr = new JSONArray(responseBody);
                        if (arr.length() > 0) {
                            newAccountId = arr.getJSONObject(0).optInt("account_id", 0);
                        }
                    } catch (Exception ignored) {}

                    final int finalId = newAccountId;

                    // Lưu session
                    SharedPreferences prefs = getSharedPreferences("evcharge_prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("is_logged_in", true)
                            .putString("username", username)
                            .putString("user_phone", userPhone)
                            .putString("user_role", "customer")
                            .putInt("account_id", finalId)
                            .apply();

                    // Tạo bản ghi khachhang để hỗ trợ thêm xe sau này
                    createCustomerRecord(finalId, username);

                    runOnUiThread(() -> {
                        Toast.makeText(TaoMatKhau.this, "Tạo tài khoản thành công! 🎉", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(TaoMatKhau.this, ChonHang.class));
                        finish();
                    });
                } else {
                    // Xử lý lỗi cụ thể từ Supabase
                    String errorMsg = "Tạo tài khoản thất bại";
                    try {
                        JSONObject err = new JSONObject(responseBody);
                        String hint = err.optString("hint", "");
                        String message = err.optString("message", "");
                        if (!hint.isEmpty()) errorMsg = hint;
                        else if (!message.isEmpty()) errorMsg = message;
                    } catch (Exception ignored) {}

                    final String finalError = errorMsg;
                    runOnUiThread(() -> Toast.makeText(TaoMatKhau.this, finalError, Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void createCustomerRecord(int accountId, String username) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("customer_id", accountId);
            json.put("account_id", accountId);
            json.put("name", username);
            json.put("phone", userPhone);
        } catch (Exception e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://xyntcrsfhacvqsuyvbkd.supabase.co/rest/v1/khachhang")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                response.close();
            }
        });
    }
}