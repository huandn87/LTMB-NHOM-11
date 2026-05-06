package com.example.voltapp.vehicle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import java.util.HashSet;

public class ChonModel extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chon_model);

        // Nhận tên Hãng xe từ màn hình trước (VD: Tesla)
        String rawHangXe = getIntent().getStringExtra("TEN_HANG_XE_DA_CHON");
        if (rawHangXe == null) rawHangXe = "VinFast";
        final String tenHangXe = rawHangXe;

        // Cập nhật tiêu đề nếu cần
        TextView txtTitle = findViewById(R.id.TXT_TITLE_MODEL); // Giả sử có ID này, nếu không thì bỏ qua
        if (txtTitle != null) txtTitle.setText("Chọn Model " + tenHangXe);

        // Nút Back
        ImageView btnBack = findViewById(R.id.BTN_BACK_MODEL);
        btnBack.setOnClickListener(v -> finish());

        LinearLayout layoutDanhSach = findViewById(R.id.LAYOUT_DANH_SACH_MODEL);
        ProgressBar progressBar = findViewById(R.id.PROGRESS_BAR_MODEL);

        if (tenHangXe == null || tenHangXe.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy hãng xe", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tải danh sách các Model của hãng này từ Supabase
        progressBar.setVisibility(View.VISIBLE);
        SupabaseService api = new SupabaseService();
        String encodedHangXe = tenHangXe;
        try {
            encodedHangXe = java.net.URLEncoder.encode(tenHangXe, "UTF-8");
        } catch (Exception e) {}

        String query = "phuongtien?manufacturer=eq." + encodedHangXe;
        api.get(query, new SupabaseService.ApiCallback() {
            @Override
            public void onSuccess(String json) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONArray array = new JSONArray(json);
                        layoutDanhSach.removeAllViews();

                        HashSet<String> addedModels = new HashSet<>();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String tenModel = obj.optString("name", "").trim();
                            int modelYear = obj.optInt("model_year", 2023);
                            String type = obj.optString("type", "car");
                            double battery = obj.optDouble("battery_capacity", 50.0);
                            int chargeStandardId = obj.optInt("charge_standard_id", 1);

                            if (!tenModel.isEmpty() && !addedModels.contains(tenModel)) {
                                addedModels.add(tenModel);
                                addModelRow(layoutDanhSach, tenHangXe, tenModel, modelYear, type, battery, chargeStandardId);
                            }
                        }

                        // Nếu không lấy được model nào từ mảng, dùng fallback data
                        if (addedModels.isEmpty()) {
                            String[] models;
                            if ("VinFast".equalsIgnoreCase(tenHangXe)) {
                                models = new String[]{"VF 5", "VF 6", "VF 7", "VF 8", "VF 9", "VF e34"};
                            } else if ("Tesla".equalsIgnoreCase(tenHangXe)) {
                                models = new String[]{"Model 3", "Model Y", "Model S", "Model X"};
                            } else if ("Porsche".equalsIgnoreCase(tenHangXe)) {
                                models = new String[]{"Taycan"};
                            } else if ("Audi".equalsIgnoreCase(tenHangXe)) {
                                models = new String[]{"e-tron GT", "Q4 e-tron", "Q8 e-tron"};
                            } else if ("Hyundai".equalsIgnoreCase(tenHangXe)) {
                                models = new String[]{"Ioniq 5", "Ioniq 6"};
                            } else if ("Kia".equalsIgnoreCase(tenHangXe)) {
                                models = new String[]{"EV6", "EV9"};
                            } else {
                                models = new String[]{"Mẫu xe tiêu chuẩn"};
                            }
                            
                            for (int i = 0; i < models.length; i++) {
                                addModelRow(layoutDanhSach, tenHangXe, models[i], 2023, "car", 60.0, 1);
                            }
                        }

                        // Vẽ đường gạch ngang (divider) cho các dòng
                        int childCount = layoutDanhSach.getChildCount();
                        for (int i = 0; i < childCount - 1; i++) {
                            View divider = new View(ChonModel.this);
                            divider.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
                            divider.setBackgroundColor(android.graphics.Color.parseColor("#2A2D39"));
                            layoutDanhSach.addView(divider, i * 2 + 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ChonModel.this, "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ChonModel.this, "Lỗi tải Model: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void addModelRow(LinearLayout layoutDanhSach, String tenHangXe, String tenModel, 
                             int modelYear, String type, double battery, int chargeStandardId) {
        RelativeLayout row = new RelativeLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(60)));
        row.setClickable(true);
        row.setFocusable(true);

        TextView txtTenModel = new TextView(this);
        txtTenModel.setText(tenModel);
        txtTenModel.setTextColor(android.graphics.Color.WHITE);
        txtTenModel.setTextSize(16);
        RelativeLayout.LayoutParams paramName = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramName.addRule(RelativeLayout.CENTER_VERTICAL);
        txtTenModel.setLayoutParams(paramName);

        TextView txtArrow = new TextView(this);
        txtArrow.setText(">");
        txtArrow.setTextColor(android.graphics.Color.parseColor("#808080"));
        txtArrow.setTextSize(20);
        RelativeLayout.LayoutParams paramArrow = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramArrow.addRule(RelativeLayout.ALIGN_PARENT_END);
        paramArrow.addRule(RelativeLayout.CENTER_VERTICAL);
        txtArrow.setLayoutParams(paramArrow);

        row.addView(txtTenModel);
        row.addView(txtArrow);

        row.setOnClickListener(v -> {
            Toast.makeText(ChonModel.this, "Đang chọn: " + tenHangXe + " - " + tenModel, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChonModel.this, XacNhanXe.class);
            intent.putExtra("TEN_HANG", tenHangXe);
            intent.putExtra("TEN_MODEL", tenModel);
            intent.putExtra("MODEL_YEAR", modelYear);
            intent.putExtra("TYPE", type);
            intent.putExtra("BATTERY_CAPACITY", battery);
            intent.putExtra("CHARGE_STANDARD_ID", chargeStandardId);
            intent.putExtra("IS_FROM_REGISTER", getIntent().getBooleanExtra("IS_FROM_REGISTER", false));
            startActivity(intent);
        });

        layoutDanhSach.addView(row);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
