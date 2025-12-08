package com.example.proyectoappsmovilesdavinci.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

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

    private ActivityResultLauncher<String> imagePickerLauncher;
    private PurchaseHomeDto compraEnEdicionParaImagen;
    private User user;

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
        welcomeTxt.setText("Bienvenido, " + user.getName() + "!");

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && compraEnEdicionParaImagen != null) {
                        compraEnEdicionParaImagen.setImageUri(uri.toString());
                        Toast.makeText(requireContext(),
                                "Imagen agregada", Toast.LENGTH_SHORT).show();
                        refreshList();
                    }
                    compraEnEdicionParaImagen = null;
                });
        MaterialButton logoutBtn = view.findViewById(R.id.btnLogout);
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            GoogleSignIn.getClient(
                    requireContext(),
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            ).signOut();

            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
        RecyclerView recyclerView = view.findViewById(R.id.rvHome);
        MaterialButton botonCrearEntidad = view.findViewById(R.id.crear_entidad_financiera);
        MaterialButton botonCrearCompra = view.findViewById(R.id.crear_compra);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        listAdapter = new HomeListAdapter(this::mostrarDialogoEditarCompra);
        recyclerView.setAdapter(listAdapter);

        botonCrearEntidad.setOnClickListener(v -> dialogCrearEntidad());
        botonCrearCompra.setOnClickListener(v -> dialogCrearCompra());

        refreshList();
    }

    private void dialogCrearEntidad() {
        final EditText input = new EditText(requireContext());
        input.setHint("Nombre de la entidad");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Nueva Entidad Financiera")
                .setView(input)
                .setPositiveButton("Crear", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FinancialEntityHomeDto fe = new FinancialEntityHomeDto(main.nextEntityId(), name);
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", fe.getId());
                    data.put("name", fe.getName());
                    data.put("userId", user.getId());
                    Log.d("FIRE_TEST", "Guardando entidad: " + data);
                    FirebaseFirestore.getInstance()
                            .collection("financial_entities")
                            .document(String.valueOf(fe.getId()))
                            .set(data);

                    main.addEntidad(fe);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void dialogCrearCompra() {
        if (main.getEntidades().isEmpty()) {
            Toast.makeText(requireContext(),
                    "Primero creá una entidad financiera", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout layout = crearLayoutDialogo();

        Spinner spEntidad = crearSpinnerEntidades(layout);
        EditText txtNombre = crearCampoTexto(layout, "Nombre de la compra", "Ej: Spotify");
        EditText txtMonto = crearCampoNumero(layout, "Monto", "1999.99");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Nueva Compra")
                .setView(layout)
                .setPositiveButton("Guardar", (d, w) -> {
                    FinancialEntityHomeDto fe = (FinancialEntityHomeDto) spEntidad.getSelectedItem();

                    String nombre = txtNombre.getText().toString().trim();
                    String montoStr = txtMonto.getText().toString().trim();

                    if (nombre.isEmpty() || montoStr.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Completá nombre y monto", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double monto = Double.parseDouble(montoStr);

                    PurchaseHomeDto p = new PurchaseHomeDto(
                            main.nextPurchaseId(),
                            monto,
                            nombre,
                            fe.getId());
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
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditarCompra(int purchaseId) {

        PurchaseHomeDto compra = buscarCompraPorId(purchaseId);
        if (compra == null)
            return;

        LinearLayout layout = crearLayoutDialogo();

        // === SPINNER DE ENTIDAD (PRE- SELECCIONADO) ===
        TextView lblEntidad = new TextView(requireContext());
        lblEntidad.setText("Entidad financiera");
        layout.addView(lblEntidad);

        Spinner spEntidad = new Spinner(requireContext());
        ArrayAdapter<FinancialEntityHomeDto> adpEntidad = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                main.getEntidades());
        spEntidad.setAdapter(adpEntidad);
        layout.addView(spEntidad);

        // Preseleccionar la entidad actual
        int index = 0;
        for (int i = 0; i < main.getEntidades().size(); i++) {
            if (main.getEntidades().get(i).getId() == compra.getFinancialEntityId()) {
                index = i;
                break;
            }
        }
        spEntidad.setSelection(index);

        // === CAMPOS PRE-LLENADOS ===
        EditText txtNombre = crearCampoTexto(layout, "Nombre", "");
        txtNombre.setText(compra.getName());

        EditText txtMonto = crearCampoNumero(layout, "Monto", "");
        txtMonto.setText(String.valueOf(compra.getAmount()));

        // === Imagen ===
        TextView lblImg = new TextView(requireContext());
        lblImg.setText("Imagen de factura");
        layout.addView(lblImg);

        MaterialButton btnImg = new MaterialButton(requireContext());
        btnImg.setText(compra.getImageUri() == null ? "Agregar imagen" : "Cambiar imagen");
        layout.addView(btnImg);

        btnImg.setOnClickListener(v -> {
            compraEnEdicionParaImagen = compra;
            imagePickerLauncher.launch("image/*");
        });

        // === Botón Ver imagen ===
        if (compra.getImageUri() != null) {
            MaterialButton btnVerImg = new MaterialButton(requireContext());
            btnVerImg.setText("Ver imagen");
            layout.addView(btnVerImg);

            btnVerImg.setOnClickListener(v -> mostrarDialogoVerImagen(compra.getImageUri()));
        }

        // === DIALOGO ===
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Editar Compra")
                .setView(layout)
                .setPositiveButton("Guardar", (d, w) -> {

                    String nuevoNombre = txtNombre.getText().toString().trim();
                    String nuevoMontoStr = txtMonto.getText().toString().trim();

                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (nuevoMontoStr.isEmpty()) {
                        Toast.makeText(requireContext(), "El monto no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double nuevoMonto;
                    try {
                        nuevoMonto = Double.parseDouble(nuevoMontoStr);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FinancialEntityHomeDto entidadSeleccionada = (FinancialEntityHomeDto) spEntidad.getSelectedItem();

                    compra.setName(nuevoNombre);
                    compra.setAmount(nuevoMonto);
                    compra.setFinancialEntityId(entidadSeleccionada.getId());
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("purchases")
                            .document(String.valueOf(compra.getId()))
                            .update(
                                    "name", compra.getName(),
                                    "amount", compra.getAmount(),
                                    "financialEntityId", compra.getFinancialEntityId()
                            );

                    refreshList();
                    Toast.makeText(requireContext(), "Compra actualizada", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Eliminar", (d, w) -> {
                    main.removeCompraById(compra.getId());
                    FirebaseFirestore.getInstance()
                            .collection("purchases")
                            .document(String.valueOf(compra.getId()))
                            .delete();
                    refreshList();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private PurchaseHomeDto buscarCompraPorId(int id) {
        for (PurchaseHomeDto p : main.getCompras()) {
            if (p.getId() == id)
                return p;
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

/*    private Spinner crearSpinnerEntidades(LinearLayout layout) {
        TextView lbl = new TextView(requireContext());
        lbl.setText("Entidad");
        layout.addView(lbl);

        Spinner sp = new Spinner(requireContext());
        ArrayAdapter<FinancialEntityHomeDto> adp = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                main.getEntidades());
        sp.setAdapter(adp);
        layout.addView(sp);

        return sp;
    }*/



    private Spinner crearSpinnerEntidades(LinearLayout layout) {

        TextView lbl = new TextView(requireContext());
        lbl.setText("Entidad");
        lbl.setTextColor(getResources().getColor(R.color.text_primary));
        lbl.setPadding(0, 10, 0, 10);
        layout.addView(lbl);

        Spinner sp = new Spinner(requireContext());
        sp.setBackgroundResource(R.drawable.bg_spinner);
        layout.addView(sp);

        ArrayAdapter<FinancialEntityHomeDto> adp = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner_dark,         // vista cerrada
                main.getEntidades()
        );

        adp.setDropDownViewResource(R.layout.item_spinner_dropdown_dark);

        sp.setAdapter(adp);

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

    private void mostrarDialogoVerImagen(String uri) {
        ImageView imageView = new ImageView(requireContext());
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(16, 16, 16, 16);

        // Carga la imagen desde la URI
        imageView.setImageURI(android.net.Uri.parse(uri));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Imagen adjunta")
                .setView(imageView)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    public  void refreshList() {
        Map<Integer, List<PurchaseHomeDto>> byEntity = new HashMap<>();
        for (PurchaseHomeDto p : main.getCompras()) {
            byEntity.computeIfAbsent(p.getFinancialEntityId(), k -> new ArrayList<>()).add(p);
        }

        List<HomeListAdapter.Row> rows = new ArrayList<>();
        for (FinancialEntityHomeDto fe : main.getEntidades()) {
            List<PurchaseHomeDto> list = byEntity.get(fe.getId());
            if (list == null || list.isEmpty())
                continue;

            rows.add(new HomeListAdapter.EntityHeader(fe.getId(), fe.getName()));
            for (PurchaseHomeDto p : list) {
                rows.add(new HomeListAdapter.PurchaseRow(p.getId(), p.getName(), p.getAmount()));
            }
        }

        listAdapter.setRows(rows);
    }
}
