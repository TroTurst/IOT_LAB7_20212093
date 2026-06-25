package com.example.bici_pucp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bici_pucp.MainActivity;
import com.example.bici_pucp.databinding.ActivityRegistroBinding;
import com.example.bici_pucp.network.dto.DesbloqueoAprobadoDto;
import com.google.android.material.snackbar.Snackbar;

public class RegistroActivity extends AppCompatActivity {

    private ActivityRegistroBinding binding;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authService = new AuthService();

        binding.btnRegistrar.setOnClickListener(v -> {
            String correo = binding.etCorreo.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String codigo = binding.etCodigo.getText().toString().trim();
            String pin = binding.etPin.getText().toString().trim();

            if (correo.isEmpty() || password.isEmpty() || codigo.isEmpty() || pin.isEmpty()) {
                Snackbar.make(binding.getRoot(), "Complete todos los campos", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (codigo.length() != 8) {
                Snackbar.make(binding.getRoot(), "El código PUCP debe tener 8 dígitos", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (pin.length() != 4) {
                Snackbar.make(binding.getRoot(), "El PIN del candado debe tener 4 dígitos", Snackbar.LENGTH_LONG).show();
                return;
            }

            binding.btnRegistrar.setEnabled(false);
            binding.pbValidando.setVisibility(View.VISIBLE);

            authService.registrar(correo, password, codigo, pin, new AuthService.AuthCallback<DesbloqueoAprobadoDto>() {
                @Override
                public void onSuccess(DesbloqueoAprobadoDto data) {
                    binding.pbValidando.setVisibility(View.GONE);
                    binding.btnRegistrar.setEnabled(true);
                    startActivity(new Intent(RegistroActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onError(String mensaje) {
                    binding.pbValidando.setVisibility(View.GONE);
                    binding.btnRegistrar.setEnabled(true);
                    Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }
}
