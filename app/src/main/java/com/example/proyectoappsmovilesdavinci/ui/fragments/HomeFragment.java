package com.example.proyectoappsmovilesdavinci.ui.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoappsmovilesdavinci.R;
import com.example.proyectoappsmovilesdavinci.dtos.FinancialEntityHomeDto;
import com.example.proyectoappsmovilesdavinci.dtos.PurchaseHomeDto;
import com.example.proyectoappsmovilesdavinci.dtos.User;
import com.example.proyectoappsmovilesdavinci.ui.DashboardActivity;
import com.example.proyectoappsmovilesdavinci.ui.LoginActivity;
import com.example.proyectoappsmovilesdavinci.ui.adapters.dashboard.HomeListAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeListAdapter listAdapter;
    private DashboardActivity main;
    private User user;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private PurchaseHomeDto compraEnEdicionParaImagen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        main = (DashboardActivity) requireActivity();
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable("user");
        }

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView welcomeTxt = view.findViewById(R.id.welcome);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", user.getName());

        welcomeTxt.setText(getString(R.string.welcome) + " " + username + "!");

        configurarLogout(view);
        configurarPickerImagen();
        configurarRecycler(view);

        refreshList();
    }

    private void configurarLogout(View view) {
        MaterialButton logoutBtn = view.findViewById(R.id.btnLogout);

        logoutBtn.setText(getString(R.string.logout));

        logoutBtn.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            GoogleSignIn.getClient(
                    requireContext(),
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            ).signOut();

            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });
    }

    private void configurarPickerImagen() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && compraEnEdicionParaImagen != null) {
                        compraEnEdicionParaImagen.setImageUri(uri.toString());
                        Toast.makeText(requireContext(),
                                getString(R.string.purchase_image_add),
                                Toast.LENGTH_SHORT).show();
                        refreshList();
                    }
                    compraEnEdicionParaImagen = null;
                });
    }

    private void configurarRecycler(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rvHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        listAdapter = new HomeListAdapter(this::mostrarDialogoEditarCompra);
        recyclerView.setAdapter(listAdapter);

        MaterialButton crearEntidad = view.findViewById(R.id.crear_entidad_financiera);
        MaterialButton crearCompra = view.findViewById(R.id.crear_compra);

        crearEntidad.setText(getString(R.string.create_financial_entity));
        crearCompra.setText(getString(R.string.create_purchase));

        crearEntidad.setOnClickListener(v -> dialogCrearEntidad());
        crearCompra.setOnClickListener(v -> dialogCrearCompra());
    }

    private void dialogCrearEntidad() {
        EditText input = new EditText(requireContext());
        input.setHint(getString(R.string.entity_default_name));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.create_financial_entity))
                .setView(input)
                .setPositiveButton(getString(R.string.create_financial_entity), (d, w) -> {

                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(),
                                getString(R.string.entity_default_name),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FinancialEntityHomeDto fe =
                            new FinancialEntityHomeDto(main.nextEntityId(), name);

                    Map<String, Object> data = new HashMap<>();
                    data.put("id", fe.getId());
                    data.put("name", fe.getName());
                    data.put("userId", user.getId());

                    FirebaseFirestore.getInstance()
                            .collection("financial_entities")
                            .document(String.valueOf(fe.getId()))
                            .set(data);

                    main.addEntidad(fe);
                })
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show();
    }

    private void dialogCrearCompra() {
        if (main.getEntidades().isEmpty()) {
            Toast.makeText(requireContext(),
                    "Primero creá una entidad financiera",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout layout = crearLayoutDialogo();

        Spinner spEntidad = crearSpinnerEntidades(layout);
        EditText txtNombre = crearCampoTexto(layout, getString(R.string.purchase_name), getString(R.string.purchase_example_name));
        EditText txtMonto = crearCampoNumero(layout, getString(R.string.purchase_amount), getString(R.string.purchase_example_amount));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.create_purchase))
                .setView(layout)
                .setPositiveButton(getString(R.string.create_purchase), (d, w) -> {

                    FinancialEntityHomeDto fe = (FinancialEntityHomeDto) spEntidad.getSelectedItem();

                    String nombre = txtNombre.getText().toString().trim();
                    String montoStr = txtMonto.getText().toString().trim();

                    if (nombre.isEmpty() || montoStr.isEmpty()) {
                        Toast.makeText(requireContext(),
                                getString(R.string.login_error_empty),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double monto = Double.parseDouble(montoStr);

                    PurchaseHomeDto p = new PurchaseHomeDto(
                            main.nextPurchaseId(),
                            monto,
                            nombre,
                            fe.getId()
                    );

                    Map<String, Object> data = new HashMap<>();
                    data.put("id", p.getId());
                    data.put("name", p.getName());
                    data.put("amount", p.getAmount());
                    data.put("financialEntityId", p.getFinancialEntityId());
                    data.put("userId", user.getId());

                    FirebaseFirestore.getInstance()
                            .collection("purchases")
                            .document(String.valueOf(p.getId()))
                            .set(data);

                    main.addCompra(p);
                    refreshList();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void mostrarDialogoEditarCompra(int purchaseId) {

        PurchaseHomeDto compra = buscarCompraPorId(purchaseId);
        if (compra == null) return;

        LinearLayout layout = crearLayoutDialogo();

        TextView lblEntidad = new TextView(requireContext());
        lblEntidad.setText(getString(R.string.entity));
        layout.addView(lblEntidad);

        Spinner spEntidad = crearSpinnerEntidades(layout);

        // Seleccionar entidad actual
        int index = 0;
        for (int i = 0; i < main.getEntidades().size(); i++) {
            if (main.getEntidades().get(i).getId() == compra.getFinancialEntityId()) {
                index = i;
                break;
            }
        }
        spEntidad.setSelection(index);

        EditText txtNombre = crearCampoTexto(layout, getString(R.string.purchase_name), "");
        txtNombre.setText(compra.getName());

        EditText txtMonto = crearCampoNumero(layout, getString(R.string.purchase_amount), "");
        txtMonto.setText(String.valueOf(compra.getAmount()));

        MaterialButton btnImg = new MaterialButton(requireContext());
        btnImg.setText(compra.getImageUri() == null
                ? getString(R.string.purchase_image_add)
                : getString(R.string.purchase_image_change));

        layout.addView(btnImg);

        btnImg.setOnClickListener(v -> {
            compraEnEdicionParaImagen = compra;
            imagePickerLauncher.launch("image/*");
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.purchase_name))
                .setView(layout)
                .setPositiveButton(getString(R.string.purchase_update), (d, w) -> {

                    String nuevoNombre = txtNombre.getText().toString().trim();
                    String montoStr = txtMonto.getText().toString().trim();

                    if (nuevoNombre.isEmpty() || montoStr.isEmpty()) {
                        Toast.makeText(requireContext(),
                                getString(R.string.login_error_empty),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double nuevoMonto;
                    try {
                        nuevoMonto = Double.parseDouble(montoStr);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(),
                                getString(R.string.purchase_invalid_amount),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FinancialEntityHomeDto entidad =
                            (FinancialEntityHomeDto) spEntidad.getSelectedItem();

                    compra.setName(nuevoNombre);
                    compra.setAmount(nuevoMonto);
                    compra.setFinancialEntityId(entidad.getId());

                    FirebaseFirestore.getInstance()
                            .collection("purchases")
                            .document(String.valueOf(compra.getId()))
                            .update(
                                    "name", nuevoNombre,
                                    "amount", nuevoMonto,
                                    "financialEntityId", entidad.getId()
                            );

                    refreshList();
                    Toast.makeText(requireContext(),
                            getString(R.string.purchase_update),
                            Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton(getString(R.string.purchase_delete), (d, w) -> {
                    main.removeCompraById(compra.getId());
                    FirebaseFirestore.getInstance()
                            .collection("purchases")
                            .document(String.valueOf(compra.getId()))
                            .delete();
                    refreshList();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private PurchaseHomeDto buscarCompraPorId(int id) {
        for (PurchaseHomeDto p : main.getCompras()) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    private LinearLayout crearLayoutDialogo() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);
        return layout;
    }

    private Spinner crearSpinnerEntidades(LinearLayout layout) {

        Spinner sp = new Spinner(requireContext());
        ArrayAdapter<FinancialEntityHomeDto> adp = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                main.getEntidades()
        );
        sp.setAdapter(adp);

        layout.addView(sp);
        return sp;
    }

    private EditText crearCampoTexto(LinearLayout layout, String label, String hint) {
        TextView lbl = new TextView(requireContext());
        lbl.setText(label);
        layout.addView(lbl);

        EditText txt = new EditText(requireContext());
        txt.setHint(hint);
        layout.addView(txt);

        return txt;
    }

    private EditText crearCampoNumero(LinearLayout layout, String label, String hint) {
        EditText txt = crearCampoTexto(layout, label, hint);
        txt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return txt;
    }

    public void refreshList() {

        Map<Integer, List<PurchaseHomeDto>> byEntity = new HashMap<>();

        for (PurchaseHomeDto p : main.getCompras()) {
            byEntity.computeIfAbsent(p.getFinancialEntityId(), k -> new ArrayList<>()).add(p);
        }

        List<HomeListAdapter.Row> rows = new ArrayList<>();

        for (FinancialEntityHomeDto fe : main.getEntidades()) {
            List<PurchaseHomeDto> list = byEntity.get(fe.getId());
            if (list == null || list.isEmpty()) continue;

            rows.add(new HomeListAdapter.EntityHeader(fe.getId(), fe.getName()));

            for (PurchaseHomeDto p : list) {
                rows.add(new HomeListAdapter.PurchaseRow(
                        p.getId(),
                        p.getName(),
                        p.getAmount()
                ));
            }
        }

        listAdapter.setRows(rows);
    }
}
