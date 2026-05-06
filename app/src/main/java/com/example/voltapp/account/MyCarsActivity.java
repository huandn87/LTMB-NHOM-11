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
import androidx.annotation.NonNull;
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

        // Thêm tính năng vuốt để xóa (Swipe to Delete)
        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback swipeCallback = 
            new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    Vehicle vehicle = vehicles.get(position);
                    
                    // Hiển thị dialog xác nhận xóa
                    new androidx.appcompat.app.AlertDialog.Builder(MyCarsActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa xe " + vehicle.manufacturer + " " + vehicle.model + " không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            api.deleteVehicle(vehicle.vehicleId, new SupabaseService.ApiCallback() {
                                @Override 
                                public void onSuccess(String json) { 
                                    runOnUiThread(() -> { 
                                        vehicles.remove(position); 
                                        adapter.notifyItemRemoved(position);
                                        Toast.makeText(MyCarsActivity.this, "Đã xóa xe thành công", Toast.LENGTH_SHORT).show();
                                    }); 
                                }
                                @Override 
                                public void onError(String message) { 
                                    runOnUiThread(() -> {
                                        adapter.notifyItemChanged(position);
                                        Toast.makeText(MyCarsActivity.this, "Lỗi xóa: " + message, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> {
                            adapter.notifyItemChanged(position); // Khôi phục lại item
                        })
                        .setCancelable(false)
                        .show();
                }
            };
        new androidx.recyclerview.widget.ItemTouchHelper(swipeCallback).attachToRecyclerView(rv);
        
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
        
        if (accountId == 0) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bước 1: Lấy actual customer_id từ bảng khachhang dựa trên account_id
        api.get("khachhang?account_id=eq." + accountId, new SupabaseService.ApiCallback() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONArray arr = new JSONArray(json);
                    if (arr.length() > 0) {
                        int actualCustomerId = arr.getJSONObject(0).getInt("customer_id");
                        fetchVehiclesByCustomerId(actualCustomerId);
                    } else {
                        // Thử tìm theo số điện thoại (trường hợp account_id bị thay đổi do test)
                        String phone = prefs.getString("user_phone", "");
                        if (!phone.isEmpty()) {
                            String queryPhone = phone;
                            try { queryPhone = java.net.URLEncoder.encode(phone, "UTF-8"); } catch (Exception ignored) {}
                            
                            api.get("khachhang?phone=eq." + queryPhone, new SupabaseService.ApiCallback() {
                                @Override
                                public void onSuccess(String json2) {
                                    try {
                                        JSONArray arr2 = new JSONArray(json2);
                                        if (arr2.length() > 0) {
                                            int actualCustomerId = arr2.getJSONObject(0).getInt("customer_id");
                                            fetchVehiclesByCustomerId(actualCustomerId);
                                        } else {
                                            showEmptyState();
                                        }
                                    } catch (Exception e) {
                                        showEmptyState();
                                    }
                                }
                                @Override
                                public void onError(String msg) {
                                    showEmptyState();
                                }
                            });
                        } else {
                            showEmptyState();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showEmptyState();
                }
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(MyCarsActivity.this, "Lỗi lấy thông tin khách hàng: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showEmptyState() {
        runOnUiThread(() -> {
            vehicles.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(MyCarsActivity.this, "Bạn chưa có xe nào. Hãy thêm xe mới!", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchVehiclesByCustomerId(int customerId) {
        // Bước 2: Lọc xe theo customer_id thực tế
        api.get("phuongtien?customer_id=eq." + customerId, new SupabaseService.ApiCallback() {
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
