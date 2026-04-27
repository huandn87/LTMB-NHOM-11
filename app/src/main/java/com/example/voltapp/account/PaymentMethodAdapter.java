package com.example.voltapp.account;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voltapp.R;
import com.example.voltapp.account.model.PaymentMethod;
import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentVH> {
    private final List<PaymentMethod> items;
    public PaymentMethodAdapter(List<PaymentMethod> items) { this.items = items; }
    @NonNull @Override public PaymentVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PaymentVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_method_account, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull PaymentVH h, int position) {
        PaymentMethod p = items.get(position);
        if (p.logoRes != 0) {
            h.logo.setVisibility(View.VISIBLE);
            h.logo.setImageResource(p.logoRes);
            h.icon.setVisibility(View.GONE);
        } else {
            h.logo.setVisibility(View.GONE);
            h.icon.setVisibility(View.VISIBLE);
            h.icon.setText(p.name.substring(0, 1).toUpperCase());
        }
        h.name.setText(p.name);
        h.status.setText(p.status);
    }
    @Override public int getItemCount() { return items.size(); }
    static class PaymentVH extends RecyclerView.ViewHolder {
        android.widget.ImageView logo;
        TextView icon, name, status;
        PaymentVH(@NonNull View v) {
            super(v);
            logo = v.findViewById(R.id.pm_logo);
            icon = v.findViewById(R.id.pm_icon);
            name = v.findViewById(R.id.pm_name);
            status = v.findViewById(R.id.pm_status);
        }
    }
}
