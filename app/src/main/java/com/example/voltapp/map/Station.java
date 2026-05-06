package com.example.voltapp.map;

import com.example.voltapp.R;
import java.io.Serializable;


public class Station implements Serializable {
    public String name;
    public String address;
    public double latitude;
    public double longitude;
    public String status;

    public Station() {} // Bắt buộc phải có

    // Các hàm Getter để hết lỗi đỏ ở file khác
    public String getName() { return name != null ? name : "Trạm sạc"; }
    public String getAddress() { return address != null ? address : "Đang cập nhật"; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isAvailable() { return "available".equalsIgnoreCase(status); }

    // Dữ liệu tạm thời để sắp xếp, không lưu vào DB
    public float distanceValue = -1;

    public String getFormattedDistance() {
        if (distanceValue < 0) return "---";
        if (distanceValue < 1000) return String.format("%.0f m", distanceValue);
        return String.format("%.1f km", distanceValue / 1000.0);
    }
}
