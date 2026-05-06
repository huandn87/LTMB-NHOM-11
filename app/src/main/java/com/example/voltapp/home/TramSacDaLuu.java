package com.example.voltapp.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.voltapp.R;
import com.example.voltapp.account.TaiKhoanActivity;
import com.example.voltapp.map.Station;
import com.example.voltapp.wallet.WalletFeatureActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class TramSacDaLuu extends AppCompatActivity {

    private LinearLayout containerSavedStations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tram_sac_da_luu);

        containerSavedStations = findViewById(R.id.CONTAINER_SAVED_STATIONS);
        findViewById(R.id.BTN_BACK_DA_LUU).setOnClickListener(v -> finish());

        setupBottomNav();
        loadSavedStations();
    }

    private void setupBottomNav() {
        View navView = findViewById(R.id.BOTTOM_NAV_CONTAINER);
        if (navView == null) return;

        // Highlight "Đã lưu"
        TextView tvSaved = navView.findViewById(R.id.NAV_SAVED_TEXT);
        ImageView ivSaved = navView.findViewById(R.id.NAV_SAVED_ICON);
        int neonColor = ContextCompat.getColor(this, R.color.XANH_NEON);
        
        if (tvSaved != null) tvSaved.setTextColor(neonColor);
        if (ivSaved != null) ivSaved.setColorFilter(neonColor);

        navView.findViewById(R.id.NAV_HOME).setOnClickListener(v -> startActivity(new Intent(this, ManHinhChinh.class)));
        navView.findViewById(R.id.NAV_SAVED).setOnClickListener(v -> { /* Đang ở Saved */ });
        navView.findViewById(R.id.NAV_WALLET).setOnClickListener(v -> startActivity(new Intent(this, WalletFeatureActivity.class)));
        navView.findViewById(R.id.NAV_ACCOUNT).setOnClickListener(v -> startActivity(new Intent(this, TaiKhoanActivity.class)));
        navView.findViewById(R.id.NAV_SCAN).setOnClickListener(v -> Toast.makeText(this, "Mở trình quét mã QR", Toast.LENGTH_SHORT).show());
    }

    private void loadSavedStations() {
        // Lấy username để phân tách trạm sạc đã lưu theo tài khoản
        SharedPreferences userPrefs = getSharedPreferences("evcharge_prefs", Context.MODE_PRIVATE);
        String loggedUsername = userPrefs.getString("username", "Khách");

        // Sử dụng tên file SharedPreferences riêng cho từng user
        SharedPreferences pref = getSharedPreferences("SAVED_STATIONS_" + loggedUsername, Context.MODE_PRIVATE);
        String json = pref.getString("STATIONS_LIST", "[]");
        Gson gson = new Gson();
        java.lang.reflect.Type listType = new TypeToken<ArrayList<Station>>(){}.getType();
        ArrayList<Station> savedList = gson.fromJson(json, listType);

        containerSavedStations.removeAllViews();
        if (savedList.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có trạm sạc nào được lưu");
            tvEmpty.setTextColor(Color.GRAY);
            tvEmpty.setGravity(android.view.Gravity.CENTER);
            tvEmpty.setPadding(0, 100, 0, 0);
            containerSavedStations.addView(tvEmpty);
            return;
        }

        for (Station s : savedList) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_station, containerSavedStations, false);
            TextView name = itemView.findViewById(R.id.tvStationName);
            TextView addr = itemView.findViewById(R.id.tvStationAddress);
            TextView status = itemView.findViewById(R.id.tvStatus);

            if (name != null) name.setText(s.getName());
            if (addr != null) addr.setText(s.getAddress());
            if (status != null) {
                status.setText(s.isAvailable() ? "● Sẵn sàng" : "● Đang bận");
                status.setTextColor(s.isAvailable() ? Color.parseColor("#01B763") : Color.parseColor("#FFC107"));
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChiTietTramSac.class);
                intent.putExtra("TEN_TRAM", s.getName());
                intent.putExtra("TRANG_THAI", s.status);
                intent.putExtra("KHOANG_CACH", "1.2 Km");
                intent.putExtra("LAT", s.latitude);
                intent.putExtra("LNG", s.longitude);
                startActivity(intent);
            });
            containerSavedStations.addView(itemView);
        }
    }
}