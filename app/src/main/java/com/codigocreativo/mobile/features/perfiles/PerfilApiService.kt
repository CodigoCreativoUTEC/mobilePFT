package com.codigocreativo.mobile.features.perfiles

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface PerfilApiService {


    @GET("perfiles/listar")
    suspend fun listarPerfiles(
        @Header("Authorization") token: String
    ): Response<List<Perfil>>

    @PUT("perfiles/modificar")
    suspend fun actualizar(@Header("Authorization") authHeader: String, @Body perfil: Perfil): Response<Unit>

    @POST("perfiles/crear")
    suspend fun crearPerfil(@Header("Authorization") authHeader: String, @Body perfil: Perfil) : Response<Unit>

    @DELETE("perfiles/inactivar")
    suspend fun eliminarPerfil(@Header("Authorization") authHeader: String, @Query("id") id: Int) : Response<Unit>
}