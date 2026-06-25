package com.example.bici_pucp.network.dto;

import com.google.gson.annotations.SerializedName;

public class DesbloqueoAprobadoDto {

    @SerializedName("status")
    public String status;

    @SerializedName("iot_auth_token")
    public String iotAuthToken;

    @SerializedName("desbloqueo_expira_en")
    public int desbloqueoExpiraEn;

    @SerializedName("timestamp_aprobacion")
    public String timestampAprobacion;
}
