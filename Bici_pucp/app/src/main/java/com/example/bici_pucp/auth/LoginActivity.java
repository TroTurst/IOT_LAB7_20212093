package com.example.bici_pucp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bici_pucp.MainActivity;
import com.example.bici_pucp.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authService = new AuthService();

        if (authService.usuarioActual() != null) {
            irAMainActivity();
            return;
        }

        binding.btnLogin.setOnClickListener(v -> {
            String correo = binding.etCorreo.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnLogin.setEnabled(false);
            binding.pbLogin.setVisibility(android.view.View.VISIBLE);

            authService.login(correo, password, new AuthService.AuthCallback<FirebaseUser>() {
                @Override
                public void onSuccess(FirebaseUser data) {
                    binding.pbLogin.setVisibility(android.view.View.GONE);
                    binding.btnLogin.setEnabled(true);
                    irAMainActivity();
                }

                @Override
                public void onError(String mensaje) {
                    binding.pbLogin.setVisibility(android.view.View.GONE);
                    binding.btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, mensaje, Toast.LENGTH_LONG).show();
                }
            });
        });

        binding.tvRegistrar.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroActivity.class));
        });
    }

    private void irAMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
