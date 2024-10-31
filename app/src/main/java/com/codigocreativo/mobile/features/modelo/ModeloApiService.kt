package com.codigocreativo.mobile.features.modelo

import com.codigocreativo.mobile.features.marca.Marca
import com.codigocreativo.mobile.features.proveedores.Proveedor
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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

    @GET("modelo/buscar")
    suspend fun buscarModelo(
        @Header("Authorization") token: String,
        @Query("nombre") nombre: String? = null,
        @Query("estado") estado: String? = null
    ): Response<List<Modelo>>

    @PUT("modelo/modificar")
    suspend fun actualizar(@Header("Authorization") authHeader: String, @Body modelo: Modelo): Response<Unit>

    @POST("modelo/crear")
    suspend fun crearModelo(@Header("Authorization") authHeader: String, @Body modelo: Modelo): Response<Unit>

    @DELETE("modelo/inactivar")
    suspend fun eliminarModelo(@Header("Authorization") authHeader: String, @Query("id") id: Int): Response<Unit>
}
