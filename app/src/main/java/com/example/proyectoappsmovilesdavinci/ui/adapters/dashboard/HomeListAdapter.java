package com.example.proyectoappsmovilesdavinci.ui.adapters.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoappsmovilesdavinci.R;

import java.util.ArrayList;
import java.util.List;

public final class HomeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Row {}

    public static final class EntityHeader implements Row {
        public final int entityId;
        public final String name;
        public EntityHeader(int entityId, String name) {
            this.entityId = entityId;
            this.name = name;
        }
    }

    public static final class PurchaseRow implements Row {
        public final int id;
        public final String name;
        public final double amount;
        public PurchaseRow(int id, String name, double amount) {
            this.id = id;
            this.name = name;
            this.amount = amount;
        }
    }

    public interface OnPurchaseClickListener {
        void onPurchaseClick(int purchaseId);
    }
    private final OnPurchaseClickListener listener;

    private static final int VT_ENTITY = 1;
    private static final int VT_PURCHASE = 2;

    private final List<Row> rows = new ArrayList<>();

    public HomeListAdapter(OnPurchaseClickListener listener) {
        this.listener = listener;
    }

    public void setRows(List<Row> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (rows.get(position) instanceof EntityHeader) ? VT_ENTITY : VT_PURCHASE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == VT_ENTITY) {
            View v = inf.inflate(R.layout.item_entity_header, parent, false);
            return new EntityVH(v);
        } else {
            View v = inf.inflate(R.layout.item_purchase, parent, false);
            return new PurchaseVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row r = rows.get(position);

        if (holder instanceof EntityVH) {
            ((EntityVH) holder).txt.setText(((EntityHeader) r).name);

        } else {
            PurchaseRow pr = (PurchaseRow) r;
            PurchaseVH vh = (PurchaseVH) holder;

            vh.name.setText(pr.name);
            vh.amount.setText(String.format("$%.2f", pr.amount));

            vh.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPurchaseClick(pr.id);
            });
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    public static class EntityVH extends RecyclerView.ViewHolder {
        final TextView txt;
        EntityVH(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.txtEntityName);
        }
    }

    public static class PurchaseVH extends RecyclerView.ViewHolder {
        final TextView name, amount;
        PurchaseVH(@NonNull View itemView) {
            super(itemView);
            name   = itemView.findViewById(R.id.txtPurchaseName);
            amount = itemView.findViewById(R.id.txtPurchaseAmount);
        }
    }
}
