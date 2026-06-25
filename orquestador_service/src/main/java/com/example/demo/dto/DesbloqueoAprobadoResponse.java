package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DesbloqueoAprobadoResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("iot_auth_token")
    private String iotAuthToken;

    @JsonProperty("desbloqueo_expira_en")
    private int desbloqueoExpiraEn;

    @JsonProperty("timestamp_aprobacion")
    private String timestampAprobacion;

    public DesbloqueoAprobadoResponse() {}

    public DesbloqueoAprobadoResponse(String status, String iotAuthToken, int desbloqueoExpiraEn, String timestampAprobacion) {
        this.status = status;
        this.iotAuthToken = iotAuthToken;
        this.desbloqueoExpiraEn = desbloqueoExpiraEn;
        this.timestampAprobacion = timestampAprobacion;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIotAuthToken() { return iotAuthToken; }
    public void setIotAuthToken(String iotAuthToken) { this.iotAuthToken = iotAuthToken; }
    public int getDesbloqueoExpiraEn() { return desbloqueoExpiraEn; }
    public void setDesbloqueoExpiraEn(int desbloqueoExpiraEn) { this.desbloqueoExpiraEn = desbloqueoExpiraEn; }
    public String getTimestampAprobacion() { return timestampAprobacion; }
    public void setTimestampAprobacion(String timestampAprobacion) { this.timestampAprobacion = timestampAprobacion; }
}
