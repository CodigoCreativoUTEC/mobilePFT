package com.codigocreativo.mobile.features.ubicacion

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

// Retrofit interface para la API
interface UbicacionesApiService {
    @GET("ubicaciones/listar")
    suspend fun listar(
        @Header("Authorization") token: String
    ): Response<List<Ubicacion>>

}
