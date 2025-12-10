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
import com.example.proyectoappsmovilesdavinci.api.ApiService;
import com.example.proyectoappsmovilesdavinci.api.RetrofitClient;
import com.example.proyectoappsmovilesdavinci.dtos.FinancialEntityHomeDto;
import com.example.proyectoappsmovilesdavinci.dtos.FinancialEntityListDtoResponse;
import com.example.proyectoappsmovilesdavinci.ui.DashboardActivity;
import com.example.proyectoappsmovilesdavinci.ui.adapters.dashboard.FinancialEntityListAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinancialEntitiesFragment extends Fragment {

    private DashboardActivity main;
    private FinancialEntityListAdapter listAdapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
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

        listAdapter.setOnDeleteEntityListener(this::confirmarEliminarEntidad);

        refreshList();
    }

    private void refreshList() {

        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIxOCIsImVtYWlsIjoiZ29uemFsbzEyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzY1MzA2OTA5LCJleHAiOjE3NjU5MTE3MDl9.grAYaNWY8Dxd_awKjPQDAwhsmpHoRfONghhGs8-zbc4";

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);

        api.getDashboard(token).enqueue(new Callback<FinancialEntityListDtoResponse>() {
            @Override
            public void onResponse(Call<FinancialEntityListDtoResponse> call,
                                   Response<FinancialEntityListDtoResponse> res) {

                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(requireContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show();
                    return;
                }

                listAdapter.setEntities(res.body().data);
            }

            @Override
            public void onFailure(Call<FinancialEntityListDtoResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarEliminarEntidad(FinancialEntityHomeDto entity) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_entity))
                .setMessage(getString(R.string.entity_delete_confirm))
                .setPositiveButton(
                        getString(R.string.entity_delete_button),
                        (dialog, which) -> eliminarEntidad(entity)
                )
                .setNegativeButton(getString(R.string.entity_delete_cancel), null)
                .show();
    }

    private void eliminarEntidad(FinancialEntityHomeDto entity) {

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

                    main.getEntidades().remove(entity);

                    refreshList();

                    Toast.makeText(
                            requireContext(),
                            getString(R.string.icon_delete) + ": " + entity.getName(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}
