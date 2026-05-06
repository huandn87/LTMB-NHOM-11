package com.example.voltapp.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;

public class HelpCenterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);

        // Xử lý nút quay lại
        ImageView btnBack = findViewById(R.id.btn_back_help);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Xử lý nút gọi điện
        Button btnCall = findViewById(R.id.btn_call_support);
        if (btnCall != null) {
            btnCall.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:0795728658"));
                startActivity(intent);
            });
        }

        // Xử lý nút gửi Email
        Button btnEmail = findViewById(R.id.btn_email_support);
        if (btnEmail != null) {
            btnEmail.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:nhattamkudo@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Cần hỗ trợ từ VoltApp");
                startActivity(intent);
            });
        }
    }
}