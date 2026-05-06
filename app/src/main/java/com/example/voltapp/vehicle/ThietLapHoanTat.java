package com.example.voltapp.vehicle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;
import com.example.voltapp.home.ManHinhChinh;

public class ThietLapHoanTat extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thiet_lap_hoan_tat);

        // Đợi 2.5 giây (2500ms) để biểu diễn vòng xoay loading
        new Handler().postDelayed(() -> {
            // Chuyển sang màn hình chính
            Intent intent = new Intent(ThietLapHoanTat.this, ManHinhChinh.class);
            startActivity(intent);
            // Xóa sạch toàn bộ lịch sử các trang trước đó
            finish();
        }, 2500);
    }
}