package com.example.voltapp.account;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;

public class SecurityActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_security_account);
        getWindow().setStatusBarColor(Color.parseColor("#191B21"));
        findViewById(R.id.btn_back_security).setOnClickListener(v -> finish());
        SharedPreferences prefs = getSharedPreferences("evcharge_prefs", MODE_PRIVATE);
        int[] ids = {R.id.sw_save_login, R.id.sw_bio, R.id.sw_face, R.id.sw_google, R.id.sw_sms};
        for (int id: ids) {
            Switch s = findViewById(id);
            s.setChecked(prefs.getBoolean("sec_" + id, id == R.id.sw_save_login));
            s.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("sec_" + id, isChecked).apply());
        }
        findViewById(R.id.btn_change_password).setOnClickListener(v -> Toast.makeText(this, "Đổi mật khẩu", Toast.LENGTH_SHORT).show());
    }
}
