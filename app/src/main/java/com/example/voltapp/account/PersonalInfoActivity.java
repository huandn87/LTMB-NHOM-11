package com.example.voltapp.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voltapp.R;
import com.example.voltapp.profile.HoanThienHoSo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PersonalInfoActivity extends AppCompatActivity {

    private static final String TAG = "VoltApp_PersonalInfo";
    private static final String SUPABASE_URL = "https://xyntcrsfhacvqsuyvbkd.supabase.co/rest/v1/khachhang";
    private static final String SUPABASE_ANON_KEY = "sb_publishable_Ga562F_Z8kOEFmvkpbPYAw_gYus54p7";

    private TextView tvName, tvEmail, tvPhone, tvGender;
    private ProgressBar progressBar;
    private String userPhone;
    
    // Lưu lại dữ liệu để truyền sang màn Edit
    private String currentName = "";
    private String currentEmail = "";
    private String currentGender = "Khác";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info_account);
        getWindow().setStatusBarColor(Color.parseColor("#191B21"));
        
        tvName = findViewById(R.id.tv_name_personal);
        tvEmail = findViewById(R.id.tv_email_personal);
        tvPhone = findViewById(R.id.tv_phone_personal);
        tvGender = findViewById(R.id.tv_gender_personal);
        progressBar = findViewById(R.id.progress_bar_personal);

        SharedPreferences prefs = getSharedPreferences("evcharge_prefs", MODE_PRIVATE);
        userPhone = prefs.getString("user_phone", "0779497860");

        findViewById(R.id.btn_back_personal).setOnClickListener(v -> finish());
        
        findViewById(R.id.btn_edit_personal).setOnClickListener(v -> {
            Intent intent = new Intent(PersonalInfoActivity.this, HoanThienHoSo.class);
            intent.putExtra("IS_EDIT_MODE", true);
            intent.putExtra("USER_PHONE", userPhone);
            intent.putExtra("USER_NAME", currentName);
            intent.putExtra("USER_EMAIL", currentEmail);
            intent.putExtra("USER_GENDER", currentGender);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserProfile();
    }

    private void fetchUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        
        final String sanitizedPhone = userPhone.trim().replace(" ", "");
        String encodedPhone = sanitizedPhone;
        try {
            encodedPhone = java.net.URLEncoder.encode(sanitizedPhone, "UTF-8");
        } catch (Exception e) {}
        
        String finalUrl = SUPABASE_URL + "?phone=eq." + encodedPhone;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(finalUrl)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PersonalInfoActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        if (jsonArray.length() > 0) {
                            JSONObject userObj = jsonArray.getJSONObject(0);
                            
                            currentName = userObj.optString("name", "Chưa cập nhật");
                            currentEmail = userObj.optString("email", "Chưa cập nhật");
                            currentGender = userObj.optString("gender", "Khác");
                            
                            runOnUiThread(() -> {
                                tvName.setText(currentName);
                                tvEmail.setText(currentEmail);
                                tvPhone.setText(sanitizedPhone);
                                tvGender.setText(currentGender + "                                      ✓");
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Lỗi parse JSON: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Lỗi API: " + response.code() + " " + responseBody);
                }
            }
        });
    }
}
