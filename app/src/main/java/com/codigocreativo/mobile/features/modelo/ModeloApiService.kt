package com.codigocreativo.mobile.features.modelo

import com.codigocreativo.mobile.features.marca.Marca
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Query

// Retrofit interface para la API
interface ModeloApiService {
    @GET("modelo/listar")
    suspend fun listarModelos(
        @Header("Authorization") token: String,
        @Query("nombre") nombre: String? = null,
        @Query("estado") estado: String? = null
    ): Response<List<Modelo>>

    @PUT("modelo/modificar")
    suspend fun actualizar(@Header("Authorization") authHeader: String, @Body modelo: Modelo): Response<Modelo>
}
