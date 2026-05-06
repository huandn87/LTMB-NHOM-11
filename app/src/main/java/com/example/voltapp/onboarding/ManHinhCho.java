package com.example.voltapp.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;
import com.example.voltapp.auth.DangNhap;
import com.example.voltapp.home.ManHinhChinh;

public class ManHinhCho extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_man_hinh_cho);

        new Handler().postDelayed(() -> {
            // Luôn bắt đầu từ màn hình Đăng nhập theo yêu cầu của người dùng
            startActivity(new Intent(ManHinhCho.this, DangNhap.class));
            finish();
        }, 2000);
    }
}