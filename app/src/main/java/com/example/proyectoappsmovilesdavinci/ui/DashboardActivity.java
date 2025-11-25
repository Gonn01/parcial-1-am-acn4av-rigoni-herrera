package com.example.proyectoappsmovilesdavinci.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.proyectoappsmovilesdavinci.R;
import com.example.proyectoappsmovilesdavinci.dtos.FinancialEntityHomeDto;
import com.example.proyectoappsmovilesdavinci.dtos.PurchaseHomeDto;
import com.example.proyectoappsmovilesdavinci.dtos.User;
import com.example.proyectoappsmovilesdavinci.ui.fragments.FinancialEntitiesFragment;
import com.example.proyectoappsmovilesdavinci.ui.fragments.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;


public class DashboardActivity extends AppCompatActivity {
    private final List<FinancialEntityHomeDto> entidades = new ArrayList<>();
    private final List<PurchaseHomeDto> compras = new ArrayList<>();
    private int nextEntityId = 1;
    private int nextPurchaseId = 1;
    private BottomNavigationView bottomNav;
    private User user;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = (User) getIntent().getSerializableExtra("user");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            swap(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f;
            if (item.getItemId() == R.id.nav_home) {
                f = new HomeFragment();
            } else {
                f = new FinancialEntitiesFragment();
            }
            swap(f);
            return true;
        });
    }

    private void swap(@NonNull Fragment fragment) {
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void removeCompraById(int id) {
        Iterator<PurchaseHomeDto> it = compras.iterator();
        while (it.hasNext()) {
            if (it.next().getId() == id) {
                it.remove();
                break;
            }
        }
    }
    public List<FinancialEntityHomeDto> getEntidades() {
        return entidades;
    }
    public void addEntidad(FinancialEntityHomeDto entidad) {
        entidades.add(entidad);
    }
    public List<PurchaseHomeDto> getCompras() {
        return compras;
    }

    public void addCompra(PurchaseHomeDto compra) {
        compras.add(compra);
    }

    public int nextEntityId() {
        return nextEntityId++;
    }

    public int nextPurchaseId() {
        return nextPurchaseId++;
    }
}