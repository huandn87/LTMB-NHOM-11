package com.example.voltapp.vehicle;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.voltapp.R;
import com.example.voltapp.account.api.SupabaseService;
import com.google.android.material.button.MaterialButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ChiTietXeActivity extends AppCompatActivity {

    private EditText edtTenXe, edtHangXe, edtNamSx, edtDungLuongPin;
    private Spinner spnLoaiXe, spnChuanSac;
    private ProgressBar progressBar;
    private String vehicleId;
    private SupabaseService api;

    // Danh sách lưu trữ id chuẩn sạc để mapping với Spinner
    private List<Integer> chargeStandardIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_xe);

        api = new SupabaseService();
        vehicleId = getIntent().getStringExtra("VEHICLE_ID");

        if (vehicleId == null) {
            Toast.makeText(this, "Không nhận được mã xe!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadChuanSacData();

        ImageView btnBack = findViewById(R.id.BTN_BACK_CHI_TIET);
        btnBack.setOnClickListener(v -> finish());

        MaterialButton btnLuu = findViewById(R.id.BTN_LUU_CHI_TIET);
        btnLuu.setOnClickListener(v -> saveVehicleDetails());
    }

    private void initViews() {
        edtTenXe = findViewById(R.id.EDT_TEN_XE);
        edtHangXe = findViewById(R.id.EDT_HANG_XE);
        edtNamSx = findViewById(R.id.EDT_NAM_SX);
        edtDungLuongPin = findViewById(R.id.EDT_DUNG_LUONG_PIN);
        spnLoaiXe = findViewById(R.id.SPN_LOAI_XE);
        spnChuanSac = findViewById(R.id.SPN_CHUAN_SAC);
        progressBar = findViewById(R.id.PROGRESS_BAR_CHI_TIET);

        // Khởi tạo danh sách loại phương tiện
        String[] loaiXeArray = {"car", "motorbike", "bicycle", "other"};
        ArrayAdapter<String> loaiXeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, loaiXeArray);
        spnLoaiXe.setAdapter(loaiXeAdapter);
    }

    private void loadChuanSacData() {
        progressBar.setVisibility(View.VISIBLE);
        api.get("chuansac", new SupabaseService.ApiCallback() {
            @Override
            public void onSuccess(String json) {
                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(json);
                        List<String> chargeStandardNames = new ArrayList<>();
                        chargeStandardIds.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int id = obj.optInt("charge_standard_id", -1);
                            String name = obj.optString("name", "Unknown");
                            if (id != -1) {
                                chargeStandardIds.add(id);
                                chargeStandardNames.add(name);
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ChiTietXeActivity.this,
                                android.R.layout.simple_spinner_dropdown_item, chargeStandardNames);
                        spnChuanSac.setAdapter(adapter);

                        // Sau khi tải xong chuẩn sạc thì tải thông tin xe
                        loadVehicleDetails();
                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                        e.printStackTrace();
                        Toast.makeText(ChiTietXeActivity.this, "Lỗi phân tích dữ liệu chuẩn sạc", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ChiTietXeActivity.this, "Lỗi tải chuẩn sạc: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadVehicleDetails() {
        String query = "phuongtien?vehicle_id=eq." + vehicleId;
        api.get(query, new SupabaseService.ApiCallback() {
            @Override
            public void onSuccess(String json) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONArray array = new JSONArray(json);
                        if (array.length() > 0) {
                            JSONObject obj = array.getJSONObject(0);

                            edtTenXe.setText(obj.optString("name", ""));
                            edtHangXe.setText(obj.optString("manufacturer", ""));
                            
                            int year = obj.optInt("model_year", 0);
                            if (year > 0) edtNamSx.setText(String.valueOf(year));

                            double battery = obj.optDouble("battery_capacity", 0);
                            if (battery > 0) edtDungLuongPin.setText(String.valueOf(battery));

                            // Chọn loại xe trên Spinner
                            String type = obj.optString("type", "car");
                            for (int i = 0; i < spnLoaiXe.getCount(); i++) {
                                if (spnLoaiXe.getItemAtPosition(i).toString().equalsIgnoreCase(type)) {
                                    spnLoaiXe.setSelection(i);
                                    break;
                                }
                            }

                            // Chọn chuẩn sạc trên Spinner
                            int chargeId = obj.optInt("charge_standard_id", -1);
                            for (int i = 0; i < chargeStandardIds.size(); i++) {
                                if (chargeStandardIds.get(i) == chargeId) {
                                    spnChuanSac.setSelection(i);
                                    break;
                                }
                            }
                        } else {
                            Toast.makeText(ChiTietXeActivity.this, "Không tìm thấy dữ liệu xe", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ChiTietXeActivity.this, "Lỗi phân tích dữ liệu xe", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ChiTietXeActivity.this, "Lỗi tải thông tin xe: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveVehicleDetails() {
        String tenXe = edtTenXe.getText().toString().trim();
        String hangXe = edtHangXe.getText().toString().trim();
        String namSxStr = edtNamSx.getText().toString().trim();
        String pinStr = edtDungLuongPin.getText().toString().trim();

        if (tenXe.isEmpty() || hangXe.isEmpty() || namSxStr.isEmpty() || pinStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int year;
        double battery;
        try {
            year = Integer.parseInt(namSxStr);
            battery = Double.parseDouble(pinStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Định dạng số không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String loaiXe = spnLoaiXe.getSelectedItem() != null ? spnLoaiXe.getSelectedItem().toString() : "car";
        
        int chargePos = spnChuanSac.getSelectedItemPosition();
        if (chargePos < 0 || chargePos >= chargeStandardIds.size()) {
            Toast.makeText(this, "Chưa chọn chuẩn sạc hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        int chargeId = chargeStandardIds.get(chargePos);

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang lưu...");
        pd.show();

        try {
            JSONObject json = new JSONObject();
            json.put("name", tenXe);
            json.put("manufacturer", hangXe);
            json.put("model_year", year);
            json.put("type", loaiXe);
            json.put("battery_capacity", battery);
            json.put("charge_standard_id", chargeId);

            String query = "phuongtien?vehicle_id=eq." + vehicleId;
            api.patch(query, json.toString(), new SupabaseService.ApiCallback() {
                @Override
                public void onSuccess(String jsonResponse) {
                    runOnUiThread(() -> {
                        pd.dismiss();
                        Toast.makeText(ChiTietXeActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng form, MyCarsActivity sẽ load lại
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        pd.dismiss();
                        Toast.makeText(ChiTietXeActivity.this, "Lỗi cập nhật: " + message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } catch (Exception e) {
            pd.dismiss();
            Toast.makeText(this, "Lỗi tạo dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
