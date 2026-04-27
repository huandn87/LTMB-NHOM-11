package com.example.voltapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class QuenMatKhau extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quen_mat_khau);

        // Nút "Tiếp tục" → chuyển sang NhapSdt theo mode đặt lại mật khẩu (register = tạo lại mật khẩu)
        MaterialButton btnTiepTuc = findViewById(R.id.BTN_TIEP_TUC_QUEN);
        if (btnTiepTuc != null) {
            btnTiepTuc.setOnClickListener(v -> {
                Intent intent = new Intent(QuenMatKhau.this, NhapSdt.class);
                intent.putExtra("MODE", "register"); // Đặt lại mật khẩu = flow register
                startActivity(intent);
            });
        }
    }
}