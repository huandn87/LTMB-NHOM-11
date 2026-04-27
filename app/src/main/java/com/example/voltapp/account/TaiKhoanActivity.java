package com.example.voltapp.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.voltapp.R;
import com.example.voltapp.auth.DangNhap;
import com.example.voltapp.home.ManHinhChinh;
import com.example.voltapp.home.TramSacDaLuu;
import com.example.voltapp.profile.HoanThienHoSo;
import com.example.voltapp.vehicle.ChonXe;
import com.example.voltapp.wallet.WalletFeatureActivity;

public class TaiKhoanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tai_khoan);

        // Map user info (Giữ nguyên như cũ)
        // Map user info (Lấy từ SharedPreferences)
        android.content.SharedPreferences prefs = getSharedPreferences("evcharge_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Người dùng");
        String userPhone = prefs.getString("user_phone", "0779497860");

        TextView tvTen = findViewById(R.id.TV_TEN_TAI_KHOAN);
        TextView tvSdt = findViewById(R.id.TV_SO_DIEN_THOAI);
        if (tvTen != null) tvTen.setText(username);
        if (tvSdt != null) tvSdt.setText(userPhone);

        // Giao diện fix với màn hình (Setup Nav & Menu)
        setupBottomNav();
        setupMenuActions();
    }

    private void setupBottomNav() {
        View navView = findViewById(R.id.BOTTOM_NAV_CONTAINER);
        if (navView == null) return;

        // Highlight "Tài khoản" (Xanh Neon)
        TextView tvAccount = navView.findViewById(R.id.NAV_ACCOUNT_TEXT);
        ImageView ivAccount = navView.findViewById(R.id.NAV_ACCOUNT_ICON);
        int neonColor = ContextCompat.getColor(this, R.color.XANH_NEON);
        
        if (tvAccount != null) tvAccount.setTextColor(neonColor);
        if (ivAccount != null) ivAccount.setColorFilter(neonColor);

        // Chuyển Activity (Chức năng thao tác không đổi)
        navView.findViewById(R.id.NAV_HOME).setOnClickListener(v -> startActivity(new Intent(this, ManHinhChinh.class)));
        navView.findViewById(R.id.NAV_SAVED).setOnClickListener(v -> startActivity(new Intent(this, TramSacDaLuu.class)));
        navView.findViewById(R.id.NAV_WALLET).setOnClickListener(v -> startActivity(new Intent(this, WalletFeatureActivity.class)));
        navView.findViewById(R.id.NAV_ACCOUNT).setOnClickListener(v -> { /* Stay here */ });
        navView.findViewById(R.id.NAV_SCAN).setOnClickListener(v -> Toast.makeText(this, "Mở trình quét mã QR", Toast.LENGTH_SHORT).show());
    }

    private void setupMenuActions() {
        // Khôi phục logic thao tác gốc của bạn
        
        // 1. Xe của tôi -> MyCarsActivity
        View rowCars = findViewById(R.id.row_cars);
        if (rowCars != null) {
            rowCars.setOnClickListener(v -> startActivity(new Intent(this, MyCarsActivity.class)));
        }

        // 2. Phương thức thanh toán -> Wallet
        View rowPayment = findViewById(R.id.row_payment);
        if (rowPayment != null) {
            rowPayment.setOnClickListener(v -> startActivity(new Intent(this, WalletFeatureActivity.class)));
        }

        // 3. Thông tin cá nhân -> HoanThienHoSo
        View rowProfile = findViewById(R.id.row_profile);
        if (rowProfile != null) {
            rowProfile.setOnClickListener(v -> startActivity(new Intent(this, HoanThienHoSo.class)));
        }

        // 4. Đăng xuất -> DangNhap
        View rowLogout = findViewById(R.id.row_logout);
        if (rowLogout != null) {
            rowLogout.setOnClickListener(v -> {
                Intent intent = new Intent(this, DangNhap.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
