package com.example.proyectoappsmovilesdavinci.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoappsmovilesdavinci.R;
import com.example.proyectoappsmovilesdavinci.dtos.FinancialEntityHomeDto;
import com.example.proyectoappsmovilesdavinci.ui.DashboardActivity;
import com.example.proyectoappsmovilesdavinci.ui.adapters.dashboard.FinancialEntityListAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

public class FinancialEntitiesFragment extends Fragment {

    private DashboardActivity main;
    private FinancialEntityListAdapter listAdapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        main = (DashboardActivity) requireActivity();
        return inflater.inflate(R.layout.fragment_financial_entities, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvFinancialEntities);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        listAdapter = new FinancialEntityListAdapter();
        rv.setAdapter(listAdapter);


        listAdapter.setOnDeleteEntityListener(entity -> eliminarEntidad(entity));

        refreshList();
    }

    private void refreshList() {
        listAdapter.setEntities(main.getEntidades());
    }

    // funcion PARA ELIMINAR ENTIDAD
    private void eliminarEntidad(FinancialEntityHomeDto entity) {

        // 1️⃣ Eliminar en Firestore
        db.collection("financial_entities")
                .whereEqualTo("id", entity.getId())
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.isEmpty()) {
                        snapshot.getDocuments()
                                .get(0)
                                .getReference()
                                .delete();
                    }

                    // 2️⃣ Eliminar de la lista en DashboardActivity
                    main.getEntidades().remove(entity);

                    // 3️⃣ Actualizar pantalla
                    refreshList();

                    Toast.makeText(requireContext(),
                            "Entidad eliminada: " + entity.getName(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}
