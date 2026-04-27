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
}
