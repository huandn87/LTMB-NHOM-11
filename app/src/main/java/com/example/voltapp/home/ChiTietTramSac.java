package com.example.voltapp.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;
import com.example.voltapp.map.Station;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class ChiTietTramSac extends AppCompatActivity {

    private double lat = 0.0;
    private double lng = 0.0;
    private String tenTramHienTai = "Trạm sạc";
    private String statusHienTai = "Sẵn sàng";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_tram_sac);

        TextView txtTenTram = findViewById(R.id.TXT_TEN_TRAM_CHI_TIET);
        TextView txtTrangThai = findViewById(R.id.TXT_TRANG_THAI_CHI_TIET);
        TextView txtKhoangCach = findViewById(R.id.TXT_KHOANG_CACH_CHI_TIET);

        Intent intent = getIntent();
        if (intent != null) {
            tenTramHienTai = intent.getStringExtra("TEN_TRAM");
            statusHienTai = intent.getStringExtra("TRANG_THAI");
            String khoangCach = intent.getStringExtra("KHOANG_CACH");
            lat = intent.getDoubleExtra("LAT", 0.0);
            lng = intent.getDoubleExtra("LNG", 0.0);

            if (tenTramHienTai != null && txtTenTram != null) txtTenTram.setText(tenTramHienTai);
            if (khoangCach != null && txtKhoangCach != null) txtKhoangCach.setText(khoangCach);
            if (statusHienTai != null && txtTrangThai != null) {
                txtTrangThai.setText(statusHienTai);
                if (statusHienTai.contains("Đầy")) {
                    txtTrangThai.setBackgroundColor(Color.parseColor("#FF4D4D"));
                } else {
                    txtTrangThai.setBackgroundColor(Color.parseColor("#01B763"));
                }
            }
        }

        // Nút Chỉ dẫn
        MaterialButton btnChiDan = findViewById(R.id.BTN_CHI_DAN_CHI_TIET);
        if (btnChiDan != null) {
            btnChiDan.setOnClickListener(v -> moGoogleMaps(lat, lng, tenTramHienTai));
        }

        // Nút Lưu trạm sạc thực tế
        findViewById(R.id.BTN_LUU_TRAM).setOnClickListener(v -> {
            Station s = new Station();
            s.name = tenTramHienTai;
            s.address = "Địa chỉ trạm sạc"; // Hoặc truyền thêm từ intent nếu có
            s.latitude = lat;
            s.longitude = lng;
            s.status = statusHienTai;
            
            toggleLuuTram(s);
        });

        findViewById(R.id.BTN_BACK_CHI_TIET).setOnClickListener(v -> finish());
        setupTabs();
    }

    private void toggleLuuTram(Station s) {
        SharedPreferences pref = getSharedPreferences("SAVED_STATIONS", Context.MODE_PRIVATE);
        String json = pref.getString("STATIONS_LIST", "[]");
        Gson gson = new Gson();
        java.lang.reflect.Type listType = new TypeToken<ArrayList<Station>>(){}.getType();
        ArrayList<Station> savedList = gson.fromJson(json, listType);

        boolean isAlreadySaved = false;
        int indexToRemove = -1;
        for (int i = 0; i < savedList.size(); i++) {
            if (savedList.get(i).getName().equals(s.getName())) {
                isAlreadySaved = true;
                indexToRemove = i;
                break;
            }
        }

        if (isAlreadySaved) {
            savedList.remove(indexToRemove);
            Toast.makeText(this, "Đã bỏ lưu trạm sạc", Toast.LENGTH_SHORT).show();
        } else {
            savedList.add(s);
            Toast.makeText(this, "Đã lưu trạm sạc thành công!", Toast.LENGTH_SHORT).show();
        }

        pref.edit().putString("STATIONS_LIST", gson.toJson(savedList)).apply();
    }

    private void moGoogleMaps(double lat, double lng, String label) {
        String query = lat + "," + lng + "(" + label + ")";
        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + Uri.encode(query));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Uri browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng);
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }

    private void setupTabs() {
        TextView tabThongTin = findViewById(R.id.TAB_THONG_TIN);
        TextView tabChuanSac = findViewById(R.id.TAB_CHUAN_SAC);
        TextView tabDanhGia = findViewById(R.id.TAB_DANH_GIA);
        LinearLayout layoutThongTin = findViewById(R.id.LAYOUT_THONG_TIN);
        LinearLayout layoutChuanSac = findViewById(R.id.LAYOUT_CHUAN_SAC);
        LinearLayout layoutDanhGia = findViewById(R.id.LAYOUT_DANH_GIA);
        int colorNeon = Color.parseColor("#01B763");
        int colorGray = Color.parseColor("#808080");

        if(tabThongTin != null) tabThongTin.setOnClickListener(v -> {
            layoutThongTin.setVisibility(View.VISIBLE);
            layoutChuanSac.setVisibility(View.GONE);
            layoutDanhGia.setVisibility(View.GONE);
            tabThongTin.setTextColor(colorNeon); tabChuanSac.setTextColor(colorGray); tabDanhGia.setTextColor(colorGray);
        });

        if(tabChuanSac != null) tabChuanSac.setOnClickListener(v -> {
            layoutThongTin.setVisibility(View.GONE);
            layoutChuanSac.setVisibility(View.VISIBLE);
            layoutDanhGia.setVisibility(View.GONE);
            tabThongTin.setTextColor(colorGray); tabChuanSac.setTextColor(colorNeon); tabDanhGia.setTextColor(colorGray);
        });

        if(tabDanhGia != null) tabDanhGia.setOnClickListener(v -> {
            layoutThongTin.setVisibility(View.GONE);
            layoutChuanSac.setVisibility(View.GONE);
            layoutDanhGia.setVisibility(View.VISIBLE);
            tabThongTin.setTextColor(colorGray); tabChuanSac.setTextColor(colorGray); tabDanhGia.setTextColor(colorNeon);
        });
    }
}