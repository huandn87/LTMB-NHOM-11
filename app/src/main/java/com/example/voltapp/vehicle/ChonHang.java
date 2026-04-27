package com.example.voltapp.vehicle;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;

public class ChonHang extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chon_hang);

        // 1. Xử lý nút Back
        ImageView btnBack = findViewById(R.id.BTN_BACK_HANG);
        if(btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 2. Ánh xạ ô tìm kiếm và danh sách các hãng xe
        EditText edtTimKiem = findViewById(R.id.EDT_TIM_KIEM);
        LinearLayout layoutDanhSach = findViewById(R.id.LAYOUT_DANH_SACH_HANG);

        // ==========================================
        // 3. XỬ LÝ SỰ KIỆN KHI BẤM VÀO TỪNG HÃNG XE (MỚI)
        // ==========================================
        for (int i = 0; i < layoutDanhSach.getChildCount(); i++) {
            View child = layoutDanhSach.getChildAt(i);

            if (child instanceof RelativeLayout) {
                RelativeLayout row = (RelativeLayout) child;

                // Gắn sự kiện click cho từng dòng
                row.setOnClickListener(v -> {
                    // Lấy tên hãng xe (Aiways, Audi, Tesla...) từ dòng vừa bấm
                    TextView txtTenHang = (TextView) row.getChildAt(0);
                    String tenHang = txtTenHang.getText().toString();

                    // Hiển thị một dòng thông báo nhỏ (Toast)
                    Toast.makeText(ChonHang.this, "Đang chọn hãng: " + tenHang, Toast.LENGTH_SHORT).show();

                    // Chuyển tiếp sang màn hình Chọn Xe
                    Intent intent = new Intent(ChonHang.this, ChonModel.class);
                    // (Tùy chọn) Gửi kèm tên hãng xe sang trang sau để xử lý tiếp
                    intent.putExtra("TEN_HANG_XE_DA_CHON", tenHang);
                    startActivity(intent);
                });
            }
        }

        // ==========================================
        // 4. LẮNG NGHE SỰ KIỆN TÌM KIẾM TỰ ĐỘNG (NHƯ CŨ)
        // ==========================================
        edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().toLowerCase().trim();

                for (int i = 0; i < layoutDanhSach.getChildCount(); i++) {
                    View child = layoutDanhSach.getChildAt(i);

                    if (child instanceof RelativeLayout) {
                        RelativeLayout row = (RelativeLayout) child;
                        TextView txtTenHang = (TextView) row.getChildAt(0);
                        String tenHang = txtTenHang.getText().toString().toLowerCase();

                        View divider = null;
                        if (i + 1 < layoutDanhSach.getChildCount() && layoutDanhSach.getChildAt(i + 1).getClass() == View.class) {
                            divider = layoutDanhSach.getChildAt(i + 1);
                        }

                        if (tenHang.contains(keyword)) {
                            row.setVisibility(View.VISIBLE);
                            if (divider != null) divider.setVisibility(View.VISIBLE);
                        } else {
                            row.setVisibility(View.GONE);
                            if (divider != null) divider.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}