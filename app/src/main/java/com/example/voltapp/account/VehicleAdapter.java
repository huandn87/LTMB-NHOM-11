package com.example.voltapp.account;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voltapp.R;
import com.example.voltapp.account.model.Vehicle;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleVH> {
    public interface OnDeleteClick { void onDelete(int position, Vehicle vehicle); }
    public interface OnItemClick { void onItemClick(Vehicle vehicle); }
    
    private final List<Vehicle> vehicles;
    private final OnDeleteClick onDeleteClick;
    private final OnItemClick onItemClick;
    private int openedPosition = -1;

    public VehicleAdapter(List<Vehicle> vehicles, OnDeleteClick onDeleteClick, OnItemClick onItemClick) {
        this.vehicles = vehicles;
        this.onDeleteClick = onDeleteClick;
        this.onItemClick = onItemClick;
    }

    @NonNull @Override public VehicleVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle_account, parent, false);
        return new VehicleVH(v);
    }

    @Override public void onBindViewHolder(@NonNull VehicleVH h, int position) {
        Vehicle item = vehicles.get(position);
        h.txtName.setText(item.manufacturer);
        h.txtModel.setText(item.model);
        h.foreground.setTranslationX(0); // Reset translation if it was swiped
        
        // Khi bấm vào mũi tên (hoặc cả item), mở màn hình Chi Tiết Xe
        h.btnArrow.setOnClickListener(v -> onItemClick.onItemClick(item));
        h.foreground.setOnClickListener(v -> onItemClick.onItemClick(item));
        
        h.btnDelete.setOnClickListener(v -> onDeleteClick.onDelete(h.getAdapterPosition(), item));
    }

    @Override public int getItemCount() { return vehicles.size(); }

    static class VehicleVH extends RecyclerView.ViewHolder {
        View foreground;
        TextView txtName, txtModel;
        ImageView btnArrow, btnDelete;
        VehicleVH(@NonNull View v) {
            super(v);
            foreground = v.findViewById(R.id.vehicle_foreground);
            txtName = v.findViewById(R.id.txt_vehicle_name);
            txtModel = v.findViewById(R.id.txt_vehicle_model);
            btnArrow = v.findViewById(R.id.btn_vehicle_arrow);
            btnDelete = v.findViewById(R.id.btn_vehicle_delete);
        }
    }
}
