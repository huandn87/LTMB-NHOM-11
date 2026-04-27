package com.example.voltapp.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voltapp.R;
import com.example.voltapp.vehicle.ChonXe;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// ... các phần import giữ nguyên

public class HoanThienHoSo extends AppCompatActivity {

    private static final String TAG = "VoltApp_Profile";
    private static final String SUPABASE_URL = "https://xyntcrsfhacvqsuyvbkd.supabase.co/rest/v1/taikhoan";
    private static final String SUPABASE_ANON_KEY = "sb_publishable_Ga562F_Z8kOEFmvkpbPYAw_gYus54p7";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoan_thien_ho_so);

        EditText edtHoTen = findViewById(R.id.EDT_HO_TEN);
        EditText edtEmail = findViewById(R.id.EDT_EMAIL);
        Spinner spnGioiTinh = findViewById(R.id.SPN_GIOI_TINH);
        MaterialButton btnTiepTuc = findViewById(R.id.BTN_TIEP_TUC_HOSO);

        String[] options = {"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGioiTinh.setAdapter(adapter);

        // 1. XỬ LÝ SỐ ĐIỆN THOẠI: Xóa hết khoảng trắng ngay từ đầu
        String rawPhone = getIntent().getStringExtra("USER_PHONE");
        if (rawPhone == null) rawPhone = "0779497860";

        // Xóa dấu cách để khớp với Database (0779 497 860 -> 0779497860)
        final String sanitizedPhone = rawPhone.trim().replace(" ", "");

        btnTiepTuc.setOnClickListener(v -> {
            String name = edtHoTen.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String gender = spnGioiTinh.getSelectedItem().toString();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            saveProfileToSupabase(sanitizedPhone, name, email, gender);
        });
    }

    private void saveProfileToSupabase(String phone, String name, String email, String gender) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONObject json = new JSONObject();
        try {
            json.put("full_name", name);
            json.put("email", email);
            json.put("gender", gender);
        } catch (Exception e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(json.toString(), JSON);

        // 2. LOG URL ĐỂ KIỂM TRA: Xem trong Logcat xem URL có bị sai không
        String finalUrl = SUPABASE_URL + "?username=eq." + phone;
        Log.d(TAG, "Đang gọi URL: " + finalUrl);

        Request request = new Request.Builder()
                .url(finalUrl)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .patch(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(HoanThienHoSo.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 3. ĐỌC PHẢN HỒI CHI TIẾT: Rất quan trọng để biết tại sao thất bại
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(HoanThienHoSo.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(HoanThienHoSo.this, com.example.voltapp.account.MyCarsActivity.class));
                        finish();
                    });
                } else {
                    // Nếu lỗi, log ra nội dung lỗi từ Supabase (ví dụ: lỗi RLS, lỗi cột...)
                    Log.e(TAG, "Lỗi Supabase " + response.code() + ": " + responseBody);
                    runOnUiThread(() -> Toast.makeText(HoanThienHoSo.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}