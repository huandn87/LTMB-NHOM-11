package com.example.voltapp.map;

import com.example.voltapp.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {
    List<Station> list;
    Context context;

    public StationAdapter(List<Station> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_station, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Station s = list.get(position);
        holder.name.setText(s.getName());
        holder.address.setText(s.getAddress());
        
        if (holder.statusText != null) {
            holder.statusText.setText(s.isAvailable() ? "● Sẵn sàng" : "● Đang bận");
            holder.statusText.setTextColor(s.isAvailable() ? Color.parseColor("#01B763") : Color.parseColor("#FFC107"));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, DetailActivity.class);
            i.putExtra("station_data", s);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address, statusText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvStationName);
            address = itemView.findViewById(R.id.tvStationAddress);
            statusText = itemView.findViewById(R.id.tvStatus);
        }
    }
}
