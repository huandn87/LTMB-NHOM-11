package com.example.voltapp.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.voltapp.R;
import com.example.voltapp.auth.DangNhap;
import com.example.voltapp.home.ManHinhChinh;
import com.example.voltapp.home.TramSacDaLuu;
import com.example.voltapp.profile.HoanThienHoSo;
import com.example.voltapp.wallet.WalletFeatureActivity;

public class TaiKhoanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tai_khoan);

        // Lấy thông tin user từ SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("evcharge_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Người dùng");
        String userPhone = prefs.getString("user_phone", "0779497860");

        TextView tvTen = findViewById(R.id.TV_TEN_TAI_KHOAN);
        TextView tvSdt = findViewById(R.id.TV_SO_DIEN_THOAI);
        if (tvTen != null) tvTen.setText(username);
        if (tvSdt != null) tvSdt.setText(userPhone);

        setupBottomNav();
        setupMenuActions();
    }

    private void setupBottomNav() {
        View navView = findViewById(R.id.BOTTOM_NAV_CONTAINER);
        if (navView == null) return;

        TextView tvAccount = navView.findViewById(R.id.NAV_ACCOUNT_TEXT);
        ImageView ivAccount = navView.findViewById(R.id.NAV_ACCOUNT_ICON);
        int neonColor = ContextCompat.getColor(this, R.color.XANH_NEON);

        if (tvAccount != null) tvAccount.setTextColor(neonColor);
        if (ivAccount != null) ivAccount.setColorFilter(neonColor);

        navView.findViewById(R.id.NAV_HOME).setOnClickListener(v -> startActivity(new Intent(this, ManHinhChinh.class)));
        navView.findViewById(R.id.NAV_SAVED).setOnClickListener(v -> startActivity(new Intent(this, TramSacDaLuu.class)));
        navView.findViewById(R.id.NAV_WALLET).setOnClickListener(v -> startActivity(new Intent(this, WalletFeatureActivity.class)));
        navView.findViewById(R.id.NAV_ACCOUNT).setOnClickListener(v -> { /* Đang ở màn hình này rồi */ });
        navView.findViewById(R.id.NAV_SCAN).setOnClickListener(v -> Toast.makeText(this, "Mở trình quét mã QR", Toast.LENGTH_SHORT).show());
    }

    private void setupMenuActions() {
        // 1. Xe của tôi
        View rowCars = findViewById(R.id.row_cars);
        if (rowCars != null) rowCars.setOnClickListener(v -> startActivity(new Intent(this, MyCarsActivity.class)));

        // 2. Phương thức thanh toán
        View rowPayment = findViewById(R.id.row_payment);
        if (rowPayment != null) rowPayment.setOnClickListener(v -> startActivity(new Intent(this, WalletFeatureActivity.class)));

        // 3. Thông tin cá nhân
        View rowProfile = findViewById(R.id.row_profile);
        if (rowProfile != null) rowProfile.setOnClickListener(v -> startActivity(new Intent(this, HoanThienHoSo.class)));

        // 4. Bảo mật
        View rowSecurity = findViewById(R.id.row_security);
        if (rowSecurity != null) rowSecurity.setOnClickListener(v -> startActivity(new Intent(this, SecurityActivity.class)));

        // 5. Ngôn ngữ (MỚI BỔ SUNG ĐỂ ĐỒNG BỘ VỚI XML)
        View rowLanguage = findViewById(R.id.row_language);
        if (rowLanguage != null) rowLanguage.setOnClickListener(v -> startActivity(new Intent(this, LanguageActivity.class)));

        // 6. Chế độ tối (Bắt sự kiện khi gạt Switch)
        Switch switchDark = findViewById(R.id.switch_dark);
        if (switchDark != null) {
            switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked) {
                    Toast.makeText(this, "Đã bật Chế độ tối", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Đã tắt Chế độ tối", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 7. Trung tâm trợ giúp
        View rowHelpCenter = findViewById(R.id.row_help_center);
        if (rowHelpCenter != null) rowHelpCenter.setOnClickListener(v -> startActivity(new Intent(this, HelpCenterActivity.class)));

        // 8. Điều khoản
        View rowTerms = findViewById(R.id.row_terms);
        if (rowTerms != null) rowTerms.setOnClickListener(v -> startActivity(new Intent(this, TermsActivity.class)));

        // 9. Đăng xuất
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