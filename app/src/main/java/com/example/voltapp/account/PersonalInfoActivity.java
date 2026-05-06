package com.example.voltapp.account;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;

public class PersonalInfoActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_personal_info_account);
        getWindow().setStatusBarColor(Color.parseColor("#191B21"));
        findViewById(R.id.btn_back_personal).setOnClickListener(v -> finish());
    }
}
