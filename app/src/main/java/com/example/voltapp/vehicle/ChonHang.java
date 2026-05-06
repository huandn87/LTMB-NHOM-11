package com.example.voltapp.vehicle;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;
import com.example.voltapp.account.api.SupabaseService;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

        ProgressBar progressBar = findViewById(R.id.PROGRESS_BAR_HANG);

        // ==========================================
        // 3. TẢI DANH SÁCH HÃNG TỪ SUPABASE
        // ==========================================
        progressBar.setVisibility(View.VISIBLE);
        SupabaseService api = new SupabaseService();
        api.get("phuongtien?customer_id=is.null", new SupabaseService.ApiCallback() {
            @Override
            public void onSuccess(String json) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONArray array = new JSONArray(json);
                        HashSet<String> uniqueBrands = new HashSet<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String brand = obj.optString("manufacturer", "").trim();
                            if (!brand.isEmpty()) {
                                uniqueBrands.add(brand);
                            }
                        }

                        // Xóa danh sách cũ nếu có
                        layoutDanhSach.removeAllViews();

                        // Thêm từng hãng vào UI
                        List<String> brandList = new ArrayList<>(uniqueBrands);
                        java.util.Collections.sort(brandList); // Sắp xếp theo A-Z

                        for (int i = 0; i < brandList.size(); i++) {
                            String tenHang = brandList.get(i);
                            addBrandRow(layoutDanhSach, tenHang);

                            // Thêm đường gạch ngang (divider)
                            if (i < brandList.size() - 1) {
                                View divider = new View(ChonHang.this);
                                divider.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                                divider.setBackgroundColor(android.graphics.Color.parseColor("#2A2D39"));
                                layoutDanhSach.addView(divider);
                            }
                        }

                        setupSearchFilter(edtTimKiem, layoutDanhSach);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ChonHang.this, "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ChonHang.this, "Lỗi tải hãng xe: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void addBrandRow(LinearLayout layoutDanhSach, String tenHang) {
        RelativeLayout row = new RelativeLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(60)));
        row.setClickable(true);
        row.setFocusable(true);

        TextView txtTenHang = new TextView(this);
        txtTenHang.setText(tenHang);
        txtTenHang.setTextColor(android.graphics.Color.WHITE);
        txtTenHang.setTextSize(16);
        RelativeLayout.LayoutParams paramName = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramName.addRule(RelativeLayout.CENTER_VERTICAL);
        txtTenHang.setLayoutParams(paramName);

        TextView txtArrow = new TextView(this);
        txtArrow.setText(">");
        txtArrow.setTextColor(android.graphics.Color.parseColor("#808080"));
        txtArrow.setTextSize(20);
        RelativeLayout.LayoutParams paramArrow = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramArrow.addRule(RelativeLayout.ALIGN_PARENT_END);
        paramArrow.addRule(RelativeLayout.CENTER_VERTICAL);
        txtArrow.setLayoutParams(paramArrow);

        row.addView(txtTenHang);
        row.addView(txtArrow);

        row.setOnClickListener(v -> {
            Toast.makeText(ChonHang.this, "Đang chọn hãng: " + tenHang, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChonHang.this, ChonModel.class);
            intent.putExtra("TEN_HANG_XE_DA_CHON", tenHang);
            startActivity(intent);
        });

        layoutDanhSach.addView(row);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void setupSearchFilter(EditText edtTimKiem, LinearLayout layoutDanhSach) {
        edtTimKiem.addTextChangedListener(new android.text.TextWatcher() {
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
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
}