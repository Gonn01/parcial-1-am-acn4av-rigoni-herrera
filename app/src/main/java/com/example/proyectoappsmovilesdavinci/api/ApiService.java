package com.example.proyectoappsmovilesdavinci.api;

import com.example.proyectoappsmovilesdavinci.dtos.FinancialEntityListDtoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ApiService {

    @GET("dashboard")
    Call<FinancialEntityListDtoResponse> getDashboard(
            @Header("Authorization") String token
    );
}
