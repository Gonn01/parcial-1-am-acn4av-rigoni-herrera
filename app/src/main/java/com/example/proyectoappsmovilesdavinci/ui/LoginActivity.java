package com.example.proyectoappsmovilesdavinci.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.proyectoappsmovilesdavinci.R;
import com.example.proyectoappsmovilesdavinci.dtos.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etPassword;
    private Button btnLogin;
    private MaterialCardView btnGoogleLogin;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    private static final int RC_GOOGLE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();

        // 🔥 AUTLOGIN → si ya hay sesión, ir directo al Dashboard
        if (mAuth.getCurrentUser() != null) {
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            cargarYEnviarUsuario(firebaseUser);
            return; // NO seguir cargando la UI del login
        }

        // Si NO hay sesión activa → mostrar login
        setContentView(R.layout.activity_login);

        inicializarComponentes();
        inicializarGoogleLogin();
        configurarEventos();
    }

    private void inicializarComponentes() {
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        ImageView googleLogo = findViewById(R.id.imgGoogleLogo);
        Glide.with(this)
                .load(getString(R.string.url_logo_google))
                .into(googleLogo);
    }

    private void inicializarGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void configurarEventos() {

        // Ir a registro
        findViewById(R.id.txtGoRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        // Login con email y contraseña
        btnLogin.setOnClickListener(v -> {
            String email = etUsuario.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        cargarYEnviarUsuario(firebaseUser);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // Login con Google
        btnGoogleLogin.setOnClickListener(v -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, RC_GOOGLE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    cargarYEnviarUsuario(firebaseUser);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error autenticando con Google", Toast.LENGTH_SHORT).show()
                );
    }

    // ⭐ Obtiene el nombre REAL desde Firestore o Google según corresponda
    private void cargarYEnviarUsuario(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    String name;

                    if (doc.exists() && doc.getString("name") != null) {
                        name = doc.getString("name"); // nombre del registro
                    } else {
                        name = firebaseUser.getDisplayName() != null
                                ? firebaseUser.getDisplayName()
                                : "Usuario";
                    }

                    User myUser = new User(uid, name, email);

                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    intent.putExtra("user", myUser);
                    startActivity(intent);
                    finish();
                });
    }
}
