package com.example.voltapp.vehicle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;

public class ChonModel extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chon_model);

        // Nhận tên Hãng xe từ màn hình trước (VD: Tesla)
        String tenHangXe = getIntent().getStringExtra("TEN_HANG_XE_DA_CHON");

        // Nút Back
        ImageView btnBack = findViewById(R.id.BTN_BACK_MODEL);
        btnBack.setOnClickListener(v -> finish());

        // Lắng nghe sự kiện bấm vào từng Model xe
        LinearLayout layoutDanhSach = findViewById(R.id.LAYOUT_DANH_SACH_MODEL);
        for (int i = 0; i < layoutDanhSach.getChildCount(); i++) {
            View child = layoutDanhSach.getChildAt(i);
            if (child instanceof RelativeLayout) {
                RelativeLayout row = (RelativeLayout) child;
                row.setOnClickListener(v -> {
                    TextView txtTenModel = (TextView) row.getChildAt(0);
                    String tenModel = txtTenModel.getText().toString();

                    Toast.makeText(ChonModel.this, "Bạn đã chọn: " + tenHangXe + " - " + tenModel, Toast.LENGTH_SHORT).show();

                    // Lát nữa mình sẽ tạo màn hình XacNhanXe.class ở đây
                     Intent intent = new Intent(ChonModel.this, XacNhanXe.class);
                    intent.putExtra("TEN_HANG", tenHangXe);
                    intent.putExtra("TEN_MODEL", tenModel);
                    startActivity(intent);
                });
            }
        }
    }
}