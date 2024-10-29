package com.codigocreativo.mobile.features.marca

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Retrofit interface para la API
interface MarcaApiService {
    @GET("marca/listar")
    suspend fun listarMarcas(@Header("Authorization") token: String): Response<List<Marca>>

    @POST("marca/crear")
    suspend fun crearMarca(@Header("Authorization") token: String, @Body nuevaMarca: Marca): Response<Unit>

    @PUT("marca/modificar")
    suspend fun editarMarca(@Header("Authorization") token: String, @Body nuevaMarca: Marca): Response<Unit>

    @DELETE("marca/inactivar/{id}")
    suspend fun eliminarMarca(@Header("Authorization") token: String, @Path("id") id: Int) : Response<Unit>
}
