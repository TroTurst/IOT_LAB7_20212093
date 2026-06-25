package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DesbloqueoRechazadoResponse {

    @JsonProperty("mensaje")
    private String mensaje;

    public DesbloqueoRechazadoResponse() {}

    public DesbloqueoRechazadoResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
