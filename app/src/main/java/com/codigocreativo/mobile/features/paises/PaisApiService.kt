package com.codigocreativo.mobile.features.paises

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface PaisApiService {
    @GET("paises/listar")
    suspend fun listarPaises(@Header("Authorization") token: String): Response<List<Pais>>

    @GET("paises/listar")
    suspend fun listarPaisesFiltradas(
        @Header("Authorization") token: String, 
        @Query("nombre") nombre: String?, 
        @Query("estado") estado: String?
    ): Response<List<Pais>>

    @POST("paises/crear")
    suspend fun crearPais(@Header("Authorization") token: String, @Body pais: Pais): Response<Unit>

    @PUT("paises/inactivar")
    suspend fun inactivarPais(@Header("Authorization") token: String, @Query("id") id: Int): Response<Unit>

    @PUT("paises/modificar")
    suspend fun modificarPais(@Header("Authorization") token: String, @Body pais: Pais): Response<Unit>
}