package com.example.proyectoappsmovilesdavinci.ui;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        user = (User) getIntent().getSerializableExtra("user");
        db = FirebaseFirestore.getInstance();

        if (savedInstanceState == null) {
            cargarDatosDesdeFirestore();
        } else {
            setContentView(R.layout.activity_dashboard);
            bottomNav = findViewById(R.id.bottom_navigation);
        }
    }

    private void cargarDatosDesdeFirestore() {

        db.collection("users")
                .document(user.getId())
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        String name = doc.getString("name");
                        user = new User(user.getId(), name, user.getEmail());
                    }

                    cargarEntidades();  // 🔥 continuar recién cuando termine
                });
    }

    private void cargarEntidades() {

        db.collection("financial_entities")
                .whereEqualTo("userId", user.getId())
                .get()
                .addOnSuccessListener(snapshot -> {

                    entidades.clear();
                    int maxId = 0;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        int id = doc.getLong("id").intValue();
                        if (id > maxId) maxId = id;

                        entidades.add(new FinancialEntityHomeDto(
                                id,
                                doc.getString("name")
                        ));
                    }

                    cargarCompras(); // 🔥 continuar cuando termine
                });
    }

    private void cargarCompras() {

        db.collection("purchases")
                .whereEqualTo("userId", user.getId())
                .get()
                .addOnSuccessListener(snapshot -> {

                    compras.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        compras.add(new PurchaseHomeDto(
                                doc.getLong("id").intValue(),
                                doc.getDouble("amount"),
                                doc.getString("name"),
                                doc.getLong("financialEntityId").intValue()
                        ));
                    }

                    // 🔥 AHORA SÍ: ya tengo TODO → recién acá muestro UI
                    iniciarUI();
                });
    }

    private void iniciarUI() {
        setContentView(R.layout.activity_dashboard);
        bottomNav = findViewById(R.id.bottom_navigation);

        swap(new HomeFragment()); // ahora sí, con user cargado

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f = (item.getItemId() == R.id.nav_home)
                    ? new HomeFragment()
                    : new FinancialEntitiesFragment();

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
    public void refrescarFragmentActual() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (f instanceof HomeFragment) {
            ((HomeFragment) f).refreshList();
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