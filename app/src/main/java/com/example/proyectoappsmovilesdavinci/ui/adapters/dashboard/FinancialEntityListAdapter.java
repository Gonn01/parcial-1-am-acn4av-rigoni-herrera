package com.example.proyectoappsmovilesdavinci.ui.adapters.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoappsmovilesdavinci.R;
import com.example.proyectoappsmovilesdavinci.dtos.FinancialEntityHomeDto;

import java.util.ArrayList;
import java.util.List;

public class FinancialEntityListAdapter extends RecyclerView.Adapter<FinancialEntityListAdapter.EntityVH> {

    public interface OnDeleteEntityListener {
        void onDelete(FinancialEntityHomeDto entity);
    }

    private OnDeleteEntityListener deleteListener;

    public void setOnDeleteEntityListener(OnDeleteEntityListener listener) {
        this.deleteListener = listener;
    }

    private final List<FinancialEntityHomeDto> entities = new ArrayList<>();

    public void setEntities(List<FinancialEntityHomeDto> newEntities) {
        entities.clear();
        entities.addAll(newEntities);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EntityVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entity_header_deletable, parent, false);
        return new EntityVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EntityVH holder, int position) {
        FinancialEntityHomeDto entity = entities.get(position);

        holder.txt.setText(entity.getName());

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(entity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }

    static class EntityVH extends RecyclerView.ViewHolder {

        final TextView txt;
        final ImageView btnDelete; 

        EntityVH(@NonNull View itemView) {
            super(itemView);

            txt = itemView.findViewById(R.id.txtEntityName);

            btnDelete = itemView.findViewById(R.id.btnDeleteEntity);
        }

    }
}
