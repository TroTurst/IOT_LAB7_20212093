package com.example.bici_pucp.main;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bici_pucp.R;
import com.example.bici_pucp.databinding.ActivityCarneBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;

public class CarneActivity extends AppCompatActivity {

    private ActivityCarneBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ExecutorService executor;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) procesarYSubir(uri);
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCarneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle("Mi Carné IoT");

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        executor = Executors.newSingleThreadExecutor();

        binding.btnSubirFoto.setOnClickListener(v -> {
            pickImage.launch("image/*");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.exists()) {
                        String codigo = snap.getString("codigo");
                        String nombre = snap.getString("correo");
                        String fotoUrl = snap.getString("foto_url");

                        binding.tvNombre.setText(nombre != null ? nombre : "Alumno PUCP");
                        binding.tvCodigo.setText(codigo != null ? codigo : "------");

                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            binding.tvFotoUrl.setText(fotoUrl);
                            Glide.with(this)
                                    .load(fotoUrl)
                                    .circleCrop()
                                    .into(binding.ivFoto);
                        }
                    }
                });
    }

    private void procesarYSubir(Uri uri) {
        executor.execute(() -> {
            try {
                Bitmap bitmap;
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    android.graphics.ImageDecoder.Source source =
                            android.graphics.ImageDecoder.createSource(getContentResolver(), uri);
                    bitmap = android.graphics.ImageDecoder.decodeBitmap(source);
                } else {
                    bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                }

                int maxSize = 1024;
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                float ratio = (float) width / height;

                if (width > height) {
                    if (width > maxSize) {
                        width = maxSize;
                        height = (int) (width / ratio);
                    }
                } else {
                    if (height > maxSize) {
                        height = maxSize;
                        width = (int) (height * ratio);
                    }
                }

                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] data = baos.toByteArray();

                String uid = auth.getCurrentUser().getUid();
                StorageReference ref = FirebaseStorage.getInstance()
                        .getReference("credenciales_bicipucp/" + uid + ".jpg");

                ref.putBytes(data)
                        .continueWithTask(task -> ref.getDownloadUrl())
                        .addOnSuccessListener(downloadUri -> {
                            String url = downloadUri.toString();
                            runOnUiThread(() -> {
                                Toast.makeText(CarneActivity.this,
                                        "Foto subida: " + url, Toast.LENGTH_LONG).show();
                                binding.tvFotoUrl.setText(url);
                            });

                            db.collection("usuarios").document(uid)
                                    .update("foto_url", url);

                            runOnUiThread(() -> {
                                Glide.with(CarneActivity.this)
                                        .load(url)
                                        .circleCrop()
                                        .into(binding.ivFoto);
                            });
                        })
                        .addOnFailureListener(e -> {
                            runOnUiThread(() -> {
                                Toast.makeText(CarneActivity.this,
                                        "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(CarneActivity.this,
                            "Error al procesar imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
