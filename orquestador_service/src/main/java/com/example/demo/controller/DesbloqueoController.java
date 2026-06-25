package com.example.demo.controller;

import com.example.demo.client.CandadoFeignClient;
import com.example.demo.dto.DesbloqueoAprobadoResponse;
import com.example.demo.dto.DesbloqueoRechazadoResponse;
import com.example.demo.dto.SolicitudDesbloqueoRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/bici")
public class DesbloqueoController {

    private final RestTemplate restTemplate;
    private final CandadoFeignClient candadoFeignClient;

    public DesbloqueoController(RestTemplate restTemplate, CandadoFeignClient candadoFeignClient) {
        this.restTemplate = restTemplate;
        this.candadoFeignClient = candadoFeignClient;
    }

    @PostMapping("/solicitar-desbloqueo")
    public ResponseEntity<?> solicitarDesbloqueo(@RequestBody SolicitudDesbloqueoRequest request) {
        try {
            Boolean alumnoOk = restTemplate.getForObject(
                    "http://pucp-validador-service/validar/alumno/{codigo}",
                    Boolean.class,
                    request.getCodigo()
            );

            Boolean candadoOk = candadoFeignClient.validarCandado(request.getPin());

            if (Boolean.TRUE.equals(alumnoOk) && Boolean.TRUE.equals(candadoOk)) {
                String token = "PUCP-BIKE-" + UUID.randomUUID().toString().substring(0, 8);
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                DesbloqueoAprobadoResponse response = new DesbloqueoAprobadoResponse(
                        "APROBADO",
                        token,
                        120,
                        timestamp
                );

                return ResponseEntity.ok(response);
            }

            String mensaje;
            if (!Boolean.TRUE.equals(alumnoOk) && !Boolean.TRUE.equals(candadoOk)) {
                mensaje = "El código del alumno y el PIN son inválidos";
            } else if (!Boolean.TRUE.equals(alumnoOk)) {
                mensaje = "El código de alumno no existe en la base de datos";
            } else {
                mensaje = "El PIN del candado IoT no cumple el formato requerido";
            }

            return ResponseEntity.badRequest().body(new DesbloqueoRechazadoResponse(mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(503).body(
                    new DesbloqueoRechazadoResponse("Servicio de validación no disponible. Intente nuevamente.")
            );
        }
    }
}
