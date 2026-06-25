package com.example.bici_pucp.network;

import com.example.bici_pucp.network.dto.DesbloqueoAprobadoDto;
import com.example.bici_pucp.network.dto.SolicitudDesbloqueoBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OrquestadorApi {

    @POST("/bici/solicitar-desbloqueo")
    Call<DesbloqueoAprobadoDto> solicitarDesbloqueo(@Body SolicitudDesbloqueoBody body);
}
