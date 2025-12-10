package com.example.proyectoappsmovilesdavinci.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoappsmovilesdavinci.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private GoogleSignInClient googleClient;
    private static final int RC_LINK_GOOGLE = 2000;

    private String pendingEmail;
    private String pendingPassword;
    private AuthCredential pendingGoogleCredential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        etName = findViewById(R.id.etRegisterName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnRegister).setOnClickListener(v -> registerUser());
        findViewById(R.id.txtGoLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        pendingEmail = etEmail.getText().toString().trim();
        pendingPassword = etPassword.getText().toString().trim();

        if (name.isEmpty() || pendingEmail.isEmpty() || pendingPassword.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(pendingEmail, pendingPassword)
                .addOnSuccessListener(result -> {

                    FirebaseUser firebaseUser = result.getUser();

                    firebaseUser.sendEmailVerification()
                            .addOnSuccessListener(a ->
                                    Toast.makeText(this, "Te enviamos un correo para verificar tu cuenta.", Toast.LENGTH_LONG).show()
                            );

                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", firebaseUser.getUid());
                    data.put("name", name);
                    data.put("email", pendingEmail);

                    db.collection("users").document(firebaseUser.getUid()).set(data);

                    auth.signOut();

                    Toast.makeText(this, "Cuenta creada. Verificá tu email antes de ingresar.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {

                    if (e instanceof FirebaseAuthUserCollisionException) {
                        mostrarDialogoVincularGoogle();
                    } else {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarDialogoVincularGoogle() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Email ya registrado")
                .setMessage("Ese correo ya está registrado con Google. ¿Querés agregarle una contraseña?")
                .setPositiveButton("Vincular", (d, w) -> iniciarReAuthGoogle())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void iniciarReAuthGoogle() {
        Intent intent = googleClient.getSignInIntent();
        startActivityForResult(intent, RC_LINK_GOOGLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_LINK_GOOGLE) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                pendingGoogleCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                auth.signInWithCredential(pendingGoogleCredential)
                        .addOnSuccessListener(result -> linkPassword(result.getUser()))
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error autenticando Google", Toast.LENGTH_SHORT).show()
                        );

            } catch (Exception e) {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void linkPassword(FirebaseUser user) {

        AuthCredential passCred =
                EmailAuthProvider.getCredential(pendingEmail, pendingPassword);

        user.linkWithCredential(passCred)
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "Cuenta vinculada. Ahora verificá tu email.", Toast.LENGTH_LONG).show();

                    result.getUser().sendEmailVerification();

                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", result.getUser().getUid());
                    data.put("name", etName.getText().toString());
                    data.put("email", pendingEmail);

                    db.collection("users").document(result.getUser().getUid()).set(data);

                    auth.signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al vincular contraseña", Toast.LENGTH_SHORT).show()
                );
    }
}
