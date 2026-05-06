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
    private final List<Vehicle> vehicles;
    private final OnDeleteClick onDeleteClick;
    private int openedPosition = -1;

    public VehicleAdapter(List<Vehicle> vehicles, OnDeleteClick onDeleteClick) {
        this.vehicles = vehicles;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull @Override public VehicleVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle_account, parent, false);
        return new VehicleVH(v);
    }

    @Override public void onBindViewHolder(@NonNull VehicleVH h, int position) {
        Vehicle item = vehicles.get(position);
        h.txtName.setText(item.manufacturer);
        h.txtModel.setText(item.model);
        float openedX = -86 * h.itemView.getResources().getDisplayMetrics().density;
        h.foreground.setTranslationX(position == openedPosition ? openedX : 0);
        h.btnArrow.setOnClickListener(v -> {
            int old = openedPosition;
            int current = h.getAdapterPosition();
            openedPosition = (openedPosition == current) ? -1 : current;
            if (old >= 0) notifyItemChanged(old);
            if (openedPosition >= 0) notifyItemChanged(openedPosition);
        });
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
