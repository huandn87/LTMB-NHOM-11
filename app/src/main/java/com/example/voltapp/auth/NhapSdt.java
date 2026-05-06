package com.example.voltapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;
import com.google.android.material.button.MaterialButton;

public class NhapSdt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhap_sdt);

        // Mode luôn là "register" (login không đi qua đây nữa)
        String mode = getIntent().getStringExtra("MODE");
        if (mode == null) mode = "register";
        final String finalMode = mode;

        EditText edtSdt = findViewById(R.id.EDT_SDT);
        CheckBox checkBox = null;
        try {
            checkBox = findViewById(R.id.CHK_DONG_Y);
        } catch (Exception ignored) {}
        final CheckBox finalCheckBox = checkBox;

        MaterialButton btnTiepTuc = findViewById(R.id.BTN_TIEP_TUC_SDT);

        btnTiepTuc.setOnClickListener(v -> {
            String sdt = edtSdt.getText().toString().trim();

            // Validate SĐT
            if (TextUtils.isEmpty(sdt)) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }
            // Chấp nhận 9-10 chữ số (bỏ số 0 đầu nếu cần)
            String digits = sdt.replaceAll("[^0-9]", "");
            if (digits.startsWith("0")) digits = digits.substring(1);
            if (digits.length() < 9 || digits.length() > 10) {
                Toast.makeText(this, "Số điện thoại không hợp lệ (9-10 chữ số)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra CheckBox điều khoản nếu có
            if (finalCheckBox != null && !finalCheckBox.isChecked()) {
                Toast.makeText(this, "Vui lòng đồng ý điều khoản sử dụng", Toast.LENGTH_SHORT).show();
                return;
            }

            // Chuẩn hoá thành +84xxxxxxxxx
            String phone = "+84" + digits;

            // Chuyển sang màn hình XacThucOtp
            Intent intent = new Intent(NhapSdt.this, XacThucOtp.class);
            intent.putExtra("USER_PHONE", phone);
            intent.putExtra("MODE", finalMode);
            startActivity(intent);
        });
    }
}