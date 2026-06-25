package com.example.bici_pucp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bici_pucp.auth.AuthService;
import com.example.bici_pucp.auth.LoginActivity;
import com.example.bici_pucp.databinding.ActivityMainBinding;
import com.example.bici_pucp.main.CarneActivity;
import com.example.bici_pucp.network.dto.DesbloqueoAprobadoDto;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AuthService authService;
    private CountDownTimer timer;
    private String iotAuthToken;
    private String codigoAlumno;
    private static final long VIDA_COMANDO_MS = 120_000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        authService = new AuthService();

        if (authService.usuarioActual() == null) {
            startActivity(new Intent(this, com.example.bici_pucp.auth.LoginActivity.class));
            finish();
            return;
        }

        binding.btnReSolicitar.setOnClickListener(v -> mostrarDialogoReSolicitar());

        cargarEstadoDesdeFirebase();
    }

    private void cargarEstadoDesdeFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.exists()) {
                        String ts = snap.getString("timestamp_aprobacion");
                        iotAuthToken = snap.getString("iot_auth_token");
                        codigoAlumno = snap.getString("codigo");

                        if (ts != null) {
                            try {
                                LocalDateTime aprobacion = LocalDateTime.parse(ts, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                long aprobMs = aprobacion.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                                long expiraEn = aprobMs + VIDA_COMANDO_MS - System.currentTimeMillis();

                                if (expiraEn > 0) {
                                    iniciarEstadoActivo(expiraEn);
                                } else {
                                    entrarEstadoExpirado();
                                }
                            } catch (Exception e) {
                                entrarEstadoExpirado();
                            }
                        } else {
                            entrarEstadoExpirado();
                        }
                    }
                });
    }

    private void iniciarEstadoActivo(long msRestantes) {
        binding.tvEstado.setTextColor(getColor(android.R.color.holo_green_dark));
        binding.tvEstado.setText("Estado Activo");
        binding.tvMensaje.setText("Candado IoT energizado - Retire la unidad");
        binding.btnReSolicitar.setVisibility(View.GONE);

        if (timer != null) timer.cancel();
        timer = new CountDownTimer(msRestantes, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long segundos = millisUntilFinished / 1000;
                binding.tvCountdown.setText(segundos + "s");
            }

            @Override
            public void onFinish() {
                entrarEstadoExpirado();
            }
        }.start();
    }

    private void entrarEstadoExpirado() {
        binding.tvEstado.setTextColor(getColor(android.R.color.holo_red_dark));
        binding.tvEstado.setText("Estado Expirado");
        binding.tvCountdown.setText("0s");
        binding.tvMensaje.setText("Tiempo de gracia expirado - Candado trabado por seguridad");
        binding.btnReSolicitar.setVisibility(View.VISIBLE);
    }

    private void mostrarDialogoReSolicitar() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reingrese su PIN");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("PIN de 4 dígitos");
        input.setMaxLines(1);
        builder.setView(input);

        builder.setPositiveButton("SOLICITAR", (dialog, which) -> {
            String pin = input.getText().toString().trim();
            if (pin.length() != 4) {
                Snackbar.make(binding.getRoot(), "El PIN debe tener 4 dígitos", Snackbar.LENGTH_LONG).show();
                return;
            }
            reSolicitarDesbloqueo(pin);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void reSolicitarDesbloqueo(String pin) {
        if (codigoAlumno == null) {
            Snackbar.make(binding.getRoot(), "Error: código de alumno no disponible", Snackbar.LENGTH_LONG).show();
            return;
        }

        binding.btnReSolicitar.setEnabled(false);

        authService.reSolicitarDesbloqueo(codigoAlumno, pin, new AuthService.AuthCallback<DesbloqueoAprobadoDto>() {
            @Override
            public void onSuccess(DesbloqueoAprobadoDto data) {
                binding.btnReSolicitar.setEnabled(true);
                iniciarEstadoActivo(VIDA_COMANDO_MS);
            }

            @Override
            public void onError(String mensaje) {
                binding.btnReSolicitar.setEnabled(true);
                Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_carne) {
            startActivity(new Intent(this, CarneActivity.class));
            return true;
        }
        if (id == R.id.action_logout) {
            confirmarLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmarLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseas cerrar sesión? Se cancelará la cuenta regresiva activa.")
                .setPositiveButton("Sí, cerrar", (d, w) -> hacerLogout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void hacerLogout() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        authService.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
