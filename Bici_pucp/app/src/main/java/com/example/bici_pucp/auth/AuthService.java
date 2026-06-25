package com.example.bici_pucp.auth;

import android.os.Handler;
import android.os.Looper;

import com.example.bici_pucp.network.ApiClient;
import com.example.bici_pucp.network.OrquestadorApi;
import com.example.bici_pucp.network.dto.DesbloqueoAprobadoDto;
import com.example.bici_pucp.network.dto.DesbloqueoErrorDto;
import com.example.bici_pucp.network.dto.SolicitudDesbloqueoBody;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class AuthService {

    public interface AuthCallback<T> {
        void onSuccess(T data);
        void onError(String mensaje);
    }

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final OrquestadorApi api;
    private final ExecutorService executor;
    private final Handler uiHandler;

    public AuthService() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.api = ApiClient.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
        this.uiHandler = new Handler(Looper.getMainLooper());
    }

    public void registrar(String correo, String password, String codigo, String pin,
                          AuthCallback<DesbloqueoAprobadoDto> callback) {
        executor.execute(() -> {
            try {
                Response<DesbloqueoAprobadoDto> response = api.solicitarDesbloqueo(
                        new SolicitudDesbloqueoBody(codigo, pin)
                ).execute();

                if (!response.isSuccessful()) {
                    String mensaje = "Error de validación";
                    try {
                        if (response.errorBody() != null) {
                            DesbloqueoErrorDto error = new Gson().fromJson(
                                    response.errorBody().string(), DesbloqueoErrorDto.class);
                            if (error != null && error.mensaje != null) {
                                mensaje = error.mensaje;
                            }
                        }
                    } catch (IOException ignored) {}
                    final String errorMsg = mensaje;
                    uiHandler.post(() -> callback.onError(errorMsg));
                    return;
                }

                DesbloqueoAprobadoDto dto = response.body();
                if (dto == null) {
                    uiHandler.post(() -> callback.onError("Respuesta vacía del servidor"));
                    return;
                }

                try {
                    auth.createUserWithEmailAndPassword(correo, password)
                            .addOnSuccessListener(authResult -> {
                                FirebaseUser user = authResult.getUser();
                                if (user != null) {
                                    Map<String, Object> perfil = new HashMap<>();
                                    perfil.put("correo", correo);
                                    perfil.put("codigo", codigo);
                                    perfil.put("iot_auth_token", dto.iotAuthToken);
                                    perfil.put("timestamp_aprobacion", dto.timestampAprobacion);
                                    perfil.put("desbloqueo_expira_en", dto.desbloqueoExpiraEn);

                                    db.collection("usuarios").document(user.getUid())
                                            .set(perfil)
                                            .addOnSuccessListener(aVoid -> {
                                                uiHandler.post(() -> callback.onSuccess(dto));
                                            })
                                            .addOnFailureListener(e -> {
                                                uiHandler.post(() -> callback.onError("Error al guardar perfil: " + e.getMessage()));
                                            });
                                } else {
                                    uiHandler.post(() -> callback.onError("Error al crear usuario en Firebase"));
                                }
                            })
                            .addOnFailureListener(e -> {
                                uiHandler.post(() -> callback.onError("Error en Firebase: " + e.getMessage()));
                            });
                } catch (Exception e) {
                    uiHandler.post(() -> callback.onError("Error en Firebase: " + e.getMessage()));
                }

            } catch (IOException e) {
                uiHandler.post(() -> callback.onError("Error de conexión con el servidor"));
            }
        });
    }

    public void login(String correo, String password, AuthCallback<FirebaseUser> callback) {
        auth.signInWithEmailAndPassword(correo, password)
                .addOnSuccessListener(authResult -> {
                    callback.onSuccess(authResult.getUser());
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void logout() {
        auth.signOut();
    }

    public FirebaseUser usuarioActual() {
        return auth.getCurrentUser();
    }

    public void reSolicitarDesbloqueo(String codigo, String pin,
                                      AuthCallback<DesbloqueoAprobadoDto> callback) {
        executor.execute(() -> {
            try {
                Response<DesbloqueoAprobadoDto> response = api.solicitarDesbloqueo(
                        new SolicitudDesbloqueoBody(codigo, pin)
                ).execute();

                if (!response.isSuccessful()) {
                    String mensaje = "Error de validación";
                    try {
                        if (response.errorBody() != null) {
                            DesbloqueoErrorDto error = new Gson().fromJson(
                                    response.errorBody().string(), DesbloqueoErrorDto.class);
                            if (error != null && error.mensaje != null) {
                                mensaje = error.mensaje;
                            }
                        }
                    } catch (IOException ignored) {}
                    final String errorMsg = mensaje;
                    uiHandler.post(() -> callback.onError(errorMsg));
                    return;
                }

                DesbloqueoAprobadoDto dto = response.body();
                if (dto == null) {
                    uiHandler.post(() -> callback.onError("Respuesta vacía del servidor"));
                    return;
                }

                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("iot_auth_token", dto.iotAuthToken);
                    updates.put("timestamp_aprobacion", dto.timestampAprobacion);
                    updates.put("desbloqueo_expira_en", dto.desbloqueoExpiraEn);

                    db.collection("usuarios").document(user.getUid())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                uiHandler.post(() -> callback.onSuccess(dto));
                            })
                            .addOnFailureListener(e -> {
                                uiHandler.post(() -> callback.onError("Error al actualizar perfil: " + e.getMessage()));
                            });
                } else {
                    uiHandler.post(() -> callback.onError("Usuario no autenticado"));
                }

            } catch (IOException e) {
                uiHandler.post(() -> callback.onError("Error de conexión con el servidor"));
            }
        });
    }
}
