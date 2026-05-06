package com.example.voltapp.account;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voltapp.R;
import com.example.voltapp.account.api.SupabaseService;
import com.example.voltapp.account.model.Vehicle;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class MyCarsActivity extends AppCompatActivity {
    private final ArrayList<Vehicle> vehicles = new ArrayList<>();
    private VehicleAdapter adapter;
    private final SupabaseService api = new SupabaseService();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_my_cars_account);
        getWindow().setStatusBarColor(Color.parseColor("#191B21"));
        findViewById(R.id.btn_back_cars).setOnClickListener(v -> finish());
        RecyclerView rv = findViewById(R.id.rv_cars);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new VehicleAdapter(vehicles, (position, vehicle) -> {
            api.deleteVehicle(vehicle.vehicleId, new SupabaseService.ApiCallback() {
                @Override public void onSuccess(String json) { runOnUiThread(() -> { vehicles.remove(position); adapter.notifyItemRemoved(position); }); }
                @Override public void onError(String message) { runOnUiThread(() -> Toast.makeText(MyCarsActivity.this, "Lỗi xóa: " + message, Toast.LENGTH_SHORT).show()); }
            });
        }, vehicle -> {
            // Khi click vào xe, mở trang ChiTietXeActivity
            Intent intent = new Intent(MyCarsActivity.this, com.example.voltapp.vehicle.ChiTietXeActivity.class);
            intent.putExtra("VEHICLE_ID", vehicle.vehicleId);
            startActivity(intent);
        });
        
        rv.setAdapter(adapter);
        
        // Nút thêm xe mới
        findViewById(R.id.btn_add_car).setOnClickListener(v -> {
            startActivity(new Intent(MyCarsActivity.this, com.example.voltapp.vehicle.ChonHang.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVehicles();
    }

    private void loadVehicles() {
        android.content.SharedPreferences prefs = getSharedPreferences("evcharge_prefs", MODE_PRIVATE);
        int accountId = prefs.getInt("account_id", 0);
        // Toast.makeText(this, "Đang tải xe cho ID: " + accountId, Toast.LENGTH_SHORT).show();
        
        // Lọc xe theo customer_id của người dùng hiện tại
        api.get("phuongtien?customer_id=eq." + accountId, new SupabaseService.ApiCallback() {
            @Override public void onSuccess(String json) {
                runOnUiThread(() -> {
                    try {
                        vehicles.clear();
                        JSONArray arr = new JSONArray(json);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            String maker = o.optString("manufacturer", "Xe");
                            String model = o.optString("name", "Mới");
                            vehicles.add(new Vehicle(o.optInt("vehicle_id", 0), maker, model));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    adapter.notifyDataSetChanged();
                });
            }
            @Override public void onError(String message) { 
                runOnUiThread(() -> Toast.makeText(MyCarsActivity.this, "Lỗi tải xe: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }
    private void addFallback() {
        vehicles.clear();
        vehicles.add(new Vehicle(1, "VinFast", "VF 8"));
        vehicles.add(new Vehicle(2, "Audi", "Model S , 40"));
        vehicles.add(new Vehicle(3, "VinFast", "VF3"));
    }
}
