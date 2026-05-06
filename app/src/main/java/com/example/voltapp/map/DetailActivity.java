package com.example.voltapp.map;

import com.example.voltapp.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Ánh xạ View (Đảm bảo ID trong XML khớp với ở đây)
        TextView txtName = findViewById(R.id.txtNameDetail);
        TextView txtAddr = findViewById(R.id.txtAddrDetail);
        Button btnNavigate = findViewById(R.id.btnNavigate);

        // Nhận dữ liệu trạm sạc từ Intent
        Station station = (Station) getIntent().getSerializableExtra("station_data");

        if (station != null) {
            txtName.setText(station.getName());
            txtAddr.setText(station.getAddress()); // Giả sử model Station có field Address

            // Xử lý nút Chỉ đường qua Google Maps
            btnNavigate.setOnClickListener(v -> {
                // Tạo Uri với tọa độ của trạm sạc
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + station.getLatitude() + "," + station.getLongitude());

                // Tạo Intent để mở Google Maps chuyên về dẫn đường (navigation)
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                // Kiểm tra xem máy có cài Google Maps không trước khi mở
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            });
        }
    }
}
