package com.example.bici_pucp.network.dto;

public class SolicitudDesbloqueoBody {
    private String codigo;
    private String pin;

    public SolicitudDesbloqueoBody(String codigo, String pin) {
        this.codigo = codigo;
        this.pin = pin;
    }
}
