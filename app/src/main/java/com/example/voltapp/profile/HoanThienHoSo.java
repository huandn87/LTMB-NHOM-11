package com.example.voltapp.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
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
    private static final String SUPABASE_URL = "https://xyntcrsfhacvqsuyvbkd.supabase.co/rest/v1/khachhang";
    private static final String SUPABASE_ANON_KEY = "sb_publishable_Ga562F_Z8kOEFmvkpbPYAw_gYus54p7";

    private boolean isEditMode = false;
    private ProgressBar progressBar;
    private MaterialButton btnTiepTuc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoan_thien_ho_so);

        TextView tvTitle = findViewById(R.id.TV_TITLE_HOSO);
        EditText edtHoTen = findViewById(R.id.EDT_HO_TEN);
        EditText edtEmail = findViewById(R.id.EDT_EMAIL);
        Spinner spnGioiTinh = findViewById(R.id.SPN_GIOI_TINH);
        btnTiepTuc = findViewById(R.id.BTN_TIEP_TUC_HOSO);
        progressBar = findViewById(R.id.progress_bar_hoso);

        String[] options = {"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGioiTinh.setAdapter(adapter);

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false);
        
        if (isEditMode) {
            tvTitle.setText("Chỉnh sửa hồ sơ ✎");
            btnTiepTuc.setText("Lưu cập nhật");
            
            String userName = intent.getStringExtra("USER_NAME");
            String userEmail = intent.getStringExtra("USER_EMAIL");
            String userGender = intent.getStringExtra("USER_GENDER");
            
            if (userName != null && !userName.equals("Chưa cập nhật")) edtHoTen.setText(userName);
            if (userEmail != null && !userEmail.equals("Chưa cập nhật")) edtEmail.setText(userEmail);
            
            if (userGender != null) {
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equalsIgnoreCase(userGender.trim())) {
                        spnGioiTinh.setSelection(i);
                        break;
                    }
                }
            }
        }

        String rawPhone = intent.getStringExtra("USER_PHONE");
        if (rawPhone == null) rawPhone = "0779497860";
        final String sanitizedPhone = rawPhone.trim().replace(" ", "");

        btnTiepTuc.setOnClickListener(v -> {
            String name = edtHoTen.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String gender = spnGioiTinh.getSelectedItem().toString();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ họ tên và email", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            saveProfileToSupabase(sanitizedPhone, name, email, gender);
        });
    }

    private void saveProfileToSupabase(String phone, String name, String email, String gender) {
        progressBar.setVisibility(View.VISIBLE);
        btnTiepTuc.setEnabled(false);
        
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONObject json = new JSONObject();
        try {
            json.put("name", name); // Cột trong bảng khachhang là 'name'
            json.put("email", email);
            json.put("gender", gender);
        } catch (Exception e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        String encodedPhone = phone;
        try {
            encodedPhone = java.net.URLEncoder.encode(phone, "UTF-8");
        } catch (Exception e) {}
        
        String finalUrl = SUPABASE_URL + "?phone=eq." + encodedPhone;

        Request request = new Request.Builder()
                .url(finalUrl)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .patch(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnTiepTuc.setEnabled(true);
                    Toast.makeText(HoanThienHoSo.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnTiepTuc.setEnabled(true);
                });

                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(HoanThienHoSo.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        if (isEditMode) {
                            finish(); // Trở về PersonalInfoActivity
                        } else {
                            // Chuyển qua màn hình Chọn Xe
                            startActivity(new Intent(HoanThienHoSo.this, com.example.voltapp.vehicle.ChonXe.class));
                            finish();
                        }
                    });
                } else {
                    Log.e(TAG, "Lỗi Supabase " + response.code() + ": " + responseBody);
                    String errorMsg = responseBody;
                    try {
                        JSONObject err = new JSONObject(responseBody);
                        errorMsg = err.optString("message", "") + " - " + err.optString("hint", "");
                    } catch (Exception e) {}
                    
                    final String finalError = errorMsg;
                    runOnUiThread(() -> Toast.makeText(HoanThienHoSo.this, "Lỗi: " + finalError, Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}