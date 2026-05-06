package com.example.voltapp.vehicle;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;
import com.google.android.material.button.MaterialButton;

public class ChonXe extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chon_xe);

        MaterialButton btnThem = findViewById(R.id.BTN_THEM_XE);
        btnThem.setOnClickListener(v -> {
            startActivity(new Intent(ChonXe.this, ChonHang.class));
        });
    }
}