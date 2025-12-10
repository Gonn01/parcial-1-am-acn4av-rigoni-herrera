package com.example.proyectoappsmovilesdavinci.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etPassword;
    private Button btnLogin;
    private MaterialCardView btnGoogleLogin;

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleClient;

    private static final int RC_GOOGLE = 100;

    private String pendingEmail;
    private String pendingPassword;
    private AuthCredential pendingGoogleCredential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {

            FirebaseUser u = mAuth.getCurrentUser();

            if (!u.isEmailVerified() && u.getProviderData().size() == 2) {
                mostrarPantallaVerificacion(u);
                return;
            }

            cargarYEnviarUsuario(u);
            return;
        }

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

        googleClient = GoogleSignIn.getClient(this, gso);
    }

    private void configurarEventos() {

        findViewById(R.id.txtGoRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        btnLogin.setOnClickListener(v -> {

            pendingEmail = etUsuario.getText().toString().trim();
            pendingPassword = etPassword.getText().toString().trim();

            if (pendingEmail.isEmpty() || pendingPassword.isEmpty()) {
                Toast.makeText(this, getString(R.string.login_error_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(pendingEmail, pendingPassword)
                    .addOnSuccessListener(r -> {

                        FirebaseUser u = mAuth.getCurrentUser();

                        if (!u.isEmailVerified()) {
                            mostrarPantallaVerificacion(u);
                            return;
                        }

                        cargarYEnviarUsuario(u);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    getString(R.string.login_error_generic, e.getMessage()),
                                    Toast.LENGTH_SHORT).show());
        });

        btnGoogleLogin.setOnClickListener(v -> {
            Intent i = googleClient.getSignInIntent();
            startActivityForResult(i, RC_GOOGLE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount acc = task.getResult(ApiException.class);

                AuthCredential cred =
                        GoogleAuthProvider.getCredential(acc.getIdToken(), null);

                loginConGoogle(cred);

            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.firebase_google_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loginConGoogle(AuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(r -> {
                    cargarYEnviarUsuario(r.getUser());
                })
                .addOnFailureListener(e -> {

                    if (e instanceof FirebaseAuthUserCollisionException) {

                        FirebaseAuthUserCollisionException ex = (FirebaseAuthUserCollisionException) e;

                        pendingEmail = ex.getEmail();
                        pendingGoogleCredential = credential;

                        manejarCuentaDuplicada(pendingEmail);

                    } else {
                        Toast.makeText(this, getString(R.string.firebase_google_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void manejarCuentaDuplicada(String email) {

        mAuth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(result -> {

                    List<String> providers = result.getSignInMethods();

                    if (providers.contains("password")) {
                        pedirPasswordParaLinkear(email);
                    } else {
                        Toast.makeText(this, getString(R.string.firebase_google_link_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pedirPasswordParaLinkear(String email) {

        EditText input = new EditText(this);
        input.setHint(getString(R.string.login_password));

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.register_link_password))
                .setMessage(getString(R.string.register_existing_google))
                .setView(input)
                .setPositiveButton(getString(R.string.register_link_password), (d, w) -> {

                    String pass = input.getText().toString().trim();
                    AuthCredential passCred =
                            EmailAuthProvider.getCredential(email, pass);

                    mAuth.signInWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(result -> {

                                FirebaseUser u = mAuth.getCurrentUser();

                                u.linkWithCredential(pendingGoogleCredential)
                                        .addOnSuccessListener(r2 -> cargarYEnviarUsuario(r2.getUser()))
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, getString(R.string.firebase_google_link_error), Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, getString(R.string.login_error_generic, e.getMessage()), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void mostrarPantallaVerificacion(FirebaseUser user) {

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.firebase_verify_email_title))
                .setMessage(getString(R.string.firebase_verify_email_msg) + "\n\nEmail: " + user.getEmail())
                .setPositiveButton(getString(R.string.firebase_resend_email), (d, w) -> {

                    user.sendEmailVerification()
                            .addOnSuccessListener(a ->
                                    Toast.makeText(this, getString(R.string.firebase_resend_email), Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(a ->
                                    Toast.makeText(this, getString(R.string.login_error_generic, a.getMessage()), Toast.LENGTH_SHORT).show());

                })
                .setNegativeButton(getString(R.string.firebase_close_session), (d, w) -> mAuth.signOut())
                .show();
    }

    private void cargarYEnviarUsuario(FirebaseUser firebaseUser) {

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {

                    String name = doc.exists() && doc.getString("name") != null
                            ? doc.getString("name")
                            : (firebaseUser.getDisplayName() != null
                            ? firebaseUser.getDisplayName()
                            : getString(R.string.welcome));

                    User myUser = new User(
                            firebaseUser.getUid(),
                            name,
                            firebaseUser.getEmail()
                    );

                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", myUser.getName());
                    editor.apply();

                    Intent intent = new Intent(this, DashboardActivity.class);
                    intent.putExtra("user", myUser);
                    startActivity(intent);
                    finish();
                });
    }
}
