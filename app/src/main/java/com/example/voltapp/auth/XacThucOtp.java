package com.example.voltapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;

public class XacThucOtp extends AppCompatActivity {

    private EditText otp1, otp2, otp3, otp4;
    private String userPhone;
    private String mode; // "login" hoặc "register"
    private String generatedOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xac_thuc_otp);

        userPhone = getIntent().getStringExtra("USER_PHONE");
        mode = getIntent().getStringExtra("MODE");
        if (userPhone == null) userPhone = "";
        if (mode == null) mode = "register";

        otp1 = findViewById(R.id.OTP1);
        otp2 = findViewById(R.id.OTP2);
        otp3 = findViewById(R.id.OTP3);
        otp4 = findViewById(R.id.OTP4);

        setupOtpForwarding();
        setupOtpBackspacing();
        generateAndLogOtp();
    }

    private void generateAndLogOtp() {
        // Tạo mã OTP ngẫu nhiên 4 chữ số
        generatedOtp = String.valueOf((int)(Math.random() * 9000) + 1000);
        android.util.Log.d("VoltApp_OTP", "========================================");
        android.util.Log.d("VoltApp_OTP", "MÃ OTP CỦA BẠN LÀ: " + generatedOtp);
        android.util.Log.d("VoltApp_OTP", "========================================");
        
        Toast.makeText(this, "Mã OTP đã được gửi! (Kiểm tra Logcat/Terminal)", Toast.LENGTH_LONG).show();
    }

    private void setupOtpForwarding() {
        otp1.addTextChangedListener(createOtpWatcher(otp2));
        otp2.addTextChangedListener(createOtpWatcher(otp3));
        otp3.addTextChangedListener(createOtpWatcher(otp4));

        otp4.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    // Ghép 4 chữ số OTP
                    String otpCode = otp1.getText().toString()
                            + otp2.getText().toString()
                            + otp3.getText().toString()
                            + s.toString();
                    xuLySauKhiNhapOtp(otpCode);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Xử lý sau khi nhập xong 4 số OTP:
     * - mode "register" → chuyển TaoMatKhau (tạo username + password)
     * - mode "login"    → không dùng nữa (login đã dùng username+password trực tiếp)
     *   Nhưng nếu vẫn gọi, redirect về DangNhap
     */
    private void xuLySauKhiNhapOtp(String otpCode) {
        // Kiểm tra OTP (nếu cần nghiêm ngặt hơn có thể dùng generatedOtp)
        if (otpCode.equals(generatedOtp)) {
            Toast.makeText(this, "Xác thực OTP thành công ✅", Toast.LENGTH_SHORT).show();

            if ("register".equals(mode)) {
                // Đăng ký: → Tạo username + mật khẩu
                Intent intent = new Intent(XacThucOtp.this, TaoMatKhau.class);
                intent.putExtra("USER_PHONE", userPhone);
                startActivity(intent);
                finish();
            } else {
                // Login mode không nên qua đây nữa, nhưng phòng trường hợp:
                // Quay về màn đăng nhập để nhập username + password
                Toast.makeText(this, "Vui lòng đăng nhập bằng tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(XacThucOtp.this, DangNhap.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(this, "Mã OTP không đúng, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            // Reset các ô nhập
            otp1.setText(""); otp2.setText(""); otp3.setText(""); otp4.setText("");
            otp1.requestFocus();
        }
    }

    private TextWatcher createOtpWatcher(EditText nextField) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) nextField.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private void setupOtpBackspacing() {
        setBackKeyListener(otp2, otp1);
        setBackKeyListener(otp3, otp2);
        setBackKeyListener(otp4, otp3);
    }

    private void setBackKeyListener(EditText current, EditText previous) {
        current.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (current.getText().toString().isEmpty()) {
                    previous.requestFocus();
                }
            }
            return false;
        });
    }
}