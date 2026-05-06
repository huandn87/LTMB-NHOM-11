package com.example.voltapp.home;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.voltapp.R;
import com.google.android.material.button.MaterialButton;

public class ChiDanDuong extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_dan_duong);

        // Nút Back
        findViewById(R.id.BTN_BACK_NAV).setOnClickListener(v -> finish());

        // Ánh xạ View
        LinearLayout panelChiDan = findViewById(R.id.PANEL_CHI_DAN);
        CardView avatarNav = findViewById(R.id.AVATAR_NAV);
        ImageView carNav = findViewById(R.id.CAR_NAV);
        MaterialButton btnBatDau = findViewById(R.id.BTN_BAT_DAU);

        // Sự kiện nút Bắt đầu
        if (btnBatDau != null) {
            btnBatDau.setOnClickListener(v -> {
                // 1. Ẩn Bảng thông tin và Avatar
                panelChiDan.setVisibility(View.GONE);
                avatarNav.setVisibility(View.GONE);

                // 2. Hiện Ô tô màu đỏ lên
                carNav.setVisibility(View.VISIBLE);

                // 3. HIỆU ỨNG: Cho ô tô di chuyển dần lên phía trạm sạc (siêu ngầu)
                carNav.animate()
                        .translationYBy(-350f) // Xe tiến lên trên 350 pixel
                        .translationXBy(50f)   // Xe nhích sang phải 50 pixel (cho hơi chéo)
                        .setDuration(4000)     // Hiệu ứng chạy từ từ trong 4 giây
                        .start();
            });
        }
    }
}