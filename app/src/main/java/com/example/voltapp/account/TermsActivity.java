package com.example.voltapp.account;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;

public class TermsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        ImageView btnBack = findViewById(R.id.btn_back_terms);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}