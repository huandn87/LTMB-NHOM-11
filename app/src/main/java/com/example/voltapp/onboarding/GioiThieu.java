package com.example.voltapp.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;
import com.example.voltapp.auth.DangNhap;

public class GioiThieu extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gioi_thieu);

        Button btnTiepTuc = findViewById(R.id.BTN_TIEP_TUC);
        btnTiepTuc.setOnClickListener(v -> {
            startActivity(new Intent(GioiThieu.this, DangNhap.class));
        });
    }
}