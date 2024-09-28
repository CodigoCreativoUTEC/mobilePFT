package com.codigocreativo.mobile.api

import com.codigocreativo.mobile.objetos.Marca
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

// Retrofit interface para la API
interface MarcaApiService {
    @GET("marca/listarTodas")
    suspend fun listarMarcas(@Header("Authorization") token: String): Response<List<Marca>>
}
