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
        if (tenHangXe == null) tenHangXe = "VinFast";

        // Cập nhật tiêu đề nếu cần
        TextView txtTitle = findViewById(R.id.TXT_TITLE_MODEL); // Giả sử có ID này, nếu không thì bỏ qua
        if (txtTitle != null) txtTitle.setText("Chọn Model " + tenHangXe);

        // Nút Back
        ImageView btnBack = findViewById(R.id.BTN_BACK_MODEL);
        btnBack.setOnClickListener(v -> finish());

        // Lắng nghe sự kiện bấm vào từng Model xe
        LinearLayout layoutDanhSach = findViewById(R.id.LAYOUT_DANH_SACH_MODEL);
        layoutDanhSach.removeAllViews(); // Xoá các model cứng trong XML

        String[] models;
        if (tenHangXe.equalsIgnoreCase("VinFast")) {
            models = new String[]{"VF 5 Plus", "VF e34", "VF 6", "VF 7", "VF 8", "VF 9"};
        } else if (tenHangXe.equalsIgnoreCase("Tesla")) {
            models = new String[]{"Model 3", "Model S", "Model X", "Model Y", "Cybertruck"};
        } else if (tenHangXe.equalsIgnoreCase("Hyundai")) {
            models = new String[]{"Ioniq 5", "Ioniq 6", "Kona Electric"};
        } else if (tenHangXe.equalsIgnoreCase("Kia")) {
            models = new String[]{"EV6", "EV9"};
        } else if (tenHangXe.equalsIgnoreCase("Audi")) {
            models = new String[]{"e-tron GT", "Q4 e-tron", "Q8 e-tron"};
        } else {
            models = new String[]{"Standard Model", "Long Range", "Performance"};
        }

        for (String modelName : models) {
            RelativeLayout row = new RelativeLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.match_parent, 
                    (int) (60 * getResources().getDisplayMetrics().density)));
            row.setPadding((int) (16 * getResources().getDisplayMetrics().density), 0, 0, 0);

            TextView txtName = new TextView(this);
            txtName.setText(modelName);
            txtName.setTextColor(android.graphics.Color.WHITE);
            txtName.setTextSize(16);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, 
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_VERTICAL);
            row.addView(txtName, lp);

            TextView txtArrow = new TextView(this);
            txtArrow.setText(">");
            txtArrow.setTextColor(android.graphics.Color.parseColor("#808080"));
            txtArrow.setTextSize(20);
            RelativeLayout.LayoutParams lpArrow = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, 
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lpArrow.addRule(RelativeLayout.ALIGN_PARENT_END);
            lpArrow.addRule(RelativeLayout.CENTER_VERTICAL);
            lpArrow.setMarginEnd((int) (16 * getResources().getDisplayMetrics().density));
            row.addView(txtArrow, lpArrow);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.match_parent, 1));
            divider.setBackgroundColor(android.graphics.Color.parseColor("#2A2D39"));

            row.setOnClickListener(v -> {
                Intent intent = new Intent(ChonModel.this, XacNhanXe.class);
                intent.putExtra("TEN_HANG", getIntent().getStringExtra("TEN_HANG_XE_DA_CHON"));
                intent.putExtra("TEN_MODEL", modelName);
                startActivity(intent);
            });

            layoutDanhSach.addView(row);
            layoutDanhSach.addView(divider);
        }
    }
}