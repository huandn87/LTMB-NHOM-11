package com.example.voltapp.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.voltapp.R;
import com.example.voltapp.account.TaiKhoanActivity;
import com.example.voltapp.wallet.WalletFeatureActivity;
import com.example.voltapp.map.Station;
import com.example.voltapp.map.SupabaseConfig;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class ManHinhChinh extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean isListView = false;
    private LinearLayout panelThongTinTram, containerListTram;
    private ShimmerFrameLayout shimmerDanhSach;
    private TextView txtTenTram, txtDiaChi, txtTrangThai, txtKhoangCach;
    
    private Station selectedStation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_man_hinh_chinh);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        FloatingActionButton fabToggle = findViewById(R.id.FAB_TOGGLE_VIEW);
        ScrollView scrollViewDanhSach = findViewById(R.id.LAYOUT_DANH_SACH_TRAM);
        panelThongTinTram = findViewById(R.id.PANEL_THONG_TIN_TRAM);
        containerListTram = findViewById(R.id.CONTAINER_LIST_TRAM);
        shimmerDanhSach = findViewById(R.id.SHIMMER_DANH_SACH);

        txtTenTram = findViewById(R.id.TXT_TEN_TRAM_MAP);
        txtDiaChi = findViewById(R.id.TXT_DIA_CHI_MAP);
        txtTrangThai = findViewById(R.id.TXT_TRANG_THAI_MAP);
        txtKhoangCach = findViewById(R.id.TXT_KHOANG_CACH_MAP);

        setupBottomNav();

        ImageView btnChiDuong = findViewById(R.id.BTN_DIRECTIONS);
        if (btnChiDuong != null) {
            btnChiDuong.setOnClickListener(v -> {
                if (selectedStation != null) moGoogleMapsDirection(selectedStation.latitude, selectedStation.longitude);
            });
        }

        MaterialButton btnLuuTramMap = findViewById(R.id.BTN_LUU_TRAM_MAP);
        if (btnLuuTramMap != null) {
            btnLuuTramMap.setOnClickListener(v -> {
                if (selectedStation != null) toggleLuuTram(selectedStation);
            });
        }

        fabToggle.setOnClickListener(v -> {
            if (isListView) {
                scrollViewDanhSach.setVisibility(View.GONE);
                fabToggle.setImageResource(android.R.drawable.ic_menu_sort_by_size);
                isListView = false;
            } else {
                scrollViewDanhSach.setVisibility(View.VISIBLE);
                panelThongTinTram.setVisibility(View.GONE);
                fabToggle.setImageResource(android.R.drawable.ic_menu_mapmode);
                isListView = true;
            }
        });

        MaterialButton btnXemTram = findViewById(R.id.BTN_XEM_TRAM);
        if (btnXemTram != null) {
            btnXemTram.setOnClickListener(v -> {
                if (selectedStation != null) chuyenSangChiTiet(selectedStation);
            });
        }
    }

    private void setupBottomNav() {
        View navView = findViewById(R.id.BOTTOM_NAV_CONTAINER);
        if (navView == null) return;

        // Highlight Home
        TextView tvHome = navView.findViewById(R.id.NAV_HOME_TEXT);
        ImageView ivHome = navView.findViewById(R.id.NAV_HOME_ICON);
        int neonColor = ContextCompat.getColor(this, R.color.XANH_NEON);
        
        if (tvHome != null) tvHome.setTextColor(neonColor);
        if (ivHome != null) ivHome.setColorFilter(neonColor);

        navView.findViewById(R.id.NAV_SAVED).setOnClickListener(v -> startActivity(new Intent(this, TramSacDaLuu.class)));
        navView.findViewById(R.id.NAV_WALLET).setOnClickListener(v -> startActivity(new Intent(this, WalletFeatureActivity.class)));
        navView.findViewById(R.id.NAV_ACCOUNT).setOnClickListener(v -> startActivity(new Intent(this, TaiKhoanActivity.class)));
        navView.findViewById(R.id.NAV_SCAN).setOnClickListener(v -> Toast.makeText(this, "Mở trình quét mã QR", Toast.LENGTH_SHORT).show());
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

    private void moGoogleMapsDirection(double lat, double lng) {
        String query = lat + "," + lng + "(" + (selectedStation != null ? selectedStation.getName() : "Trạm sạc") + ")";
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

    private void chuyenSangChiTiet(Station s) {
        Intent intent = new Intent(ManHinhChinh.this, ChiTietTramSac.class);
        intent.putExtra("TEN_TRAM", s.getName());
        intent.putExtra("TRANG_THAI", s.status);
        intent.putExtra("KHOANG_CACH", "1.2 Km");
        intent.putExtra("LAT", s.latitude);
        intent.putExtra("LNG", s.longitude);
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(16.0544, 108.2022), 15));
        mMap.setOnMarkerClickListener(marker -> {
            Station s = (Station) marker.getTag();
            if (s != null) {
                selectedStation = s; 
                hienThiPanelThongTin(s);
            }
            return false; 
        });
        mMap.setOnMapClickListener(latLng -> {
            panelThongTinTram.setVisibility(View.GONE);
            selectedStation = null;
        });
        loadStationsFromSupabase();
    }

    private void hienThiPanelThongTin(Station s) {
        panelThongTinTram.setVisibility(View.VISIBLE);
        txtTenTram.setText(s.getName());
        txtDiaChi.setText(s.getAddress());
        txtTrangThai.setText(s.isAvailable() ? "● Sẵn sàng" : "● Đang bận");
        txtTrangThai.setTextColor(s.isAvailable() ? Color.parseColor("#01B763") : Color.parseColor("#FFC107"));
        txtKhoangCach.setText("📍 1.2 Km");
    }

    private void loadStationsFromSupabase() {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(SupabaseConfig.SUPABASE_URL + "/rest/v1/tramsac?select=*");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", SupabaseConfig.SUPABASE_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SupabaseConfig.SUPABASE_KEY);
                java.io.InputStream in = conn.getInputStream();
                java.util.Scanner scanner = new java.util.Scanner(in).useDelimiter("\\A");
                String json = scanner.hasNext() ? scanner.next() : "";
                Gson gson = new Gson();
                final List<Station> stations = gson.fromJson(json, new TypeToken<List<Station>>(){}.getType());

                runOnUiThread(() -> {
                    if (shimmerDanhSach != null) {
                        shimmerDanhSach.stopShimmer();
                        shimmerDanhSach.setVisibility(View.GONE);
                    }
                    containerListTram.setVisibility(View.VISIBLE);
                    containerListTram.removeAllViews();
                    if (stations != null) {
                        for (Station s : stations) {
                            LatLng pos = new LatLng(s.latitude, s.longitude);
                            int markerIcon = s.isAvailable() ? R.drawable.ic_marker_available : R.drawable.ic_marker_busy;
                            Marker m = mMap.addMarker(new MarkerOptions().position(pos).icon(bitmapDescriptorFromVector(markerIcon)));
                            if (m != null) m.setTag(s);

                            View itemView = LayoutInflater.from(this).inflate(R.layout.item_station, containerListTram, false);
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
                                selectedStation = s;
                                hienThiPanelThongTin(s);
                                chuyenSangChiTiet(s);
                            });
                            containerListTram.addView(itemView);
                        }
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        if (vectorDrawable == null) return BitmapDescriptorFactory.defaultMarker();
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
