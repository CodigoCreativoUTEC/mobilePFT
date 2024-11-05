package com.codigocreativo.mobile.features.equipos

import com.codigocreativo.mobile.features.equipos.Equipo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

// Retrofit interface para la API
interface EquiposApiService {
    @GET("equipos/buscar")
    suspend fun buscar(
        @Header("Authorization") token: String,
        @Query("nombre") nombre: String? = null,
        @Query("estado") estado: String? = null
    ): Response<List<Equipo>>

    @GET("equipos/listar")
    suspend fun listar(
        @Header("Authorization") token: String
    ): Response<List<Equipo>>

    @PUT("equipos/modificar")
    suspend fun actualizar(@Header("Authorization") authHeader: String, @Body equipo: Equipo):  Response<Unit>

    @POST("equipos/crear")
    suspend fun crear(@Header("Authorization") authHeader: String, @Body equipo: Equipo) : Response<Unit>

    @DELETE("equipos/inactivar")
    suspend fun eliminar(@Header("Authorization") authHeader: String, @Query("id") id: Int) : Response<Unit>
}
