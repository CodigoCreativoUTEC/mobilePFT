package com.codigocreativo.mobile.features.marca

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

// Retrofit interface para la API
interface MarcaApiService {
    @GET("marca/listar")
    suspend fun listarMarcas(@Header("Authorization") token: String): Response<List<Marca>>

    @PUT("marca/actualizar")
    suspend fun actualizarMarca(@Header("Authorization") token: String, @Body marca: Marca): Response<Marca>
}
