package com.codigocreativo.mobile.features.paises

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface PaisApiService {
    @GET("paises/listar")
    suspend fun listarPaises(@Header("Authorization") token: String): Response<List<Pais>>
}