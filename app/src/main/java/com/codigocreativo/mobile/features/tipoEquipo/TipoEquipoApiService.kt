package com.codigocreativo.mobile.features.tipoEquipo


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

// Retrofit interface para la API
interface TipoEquipoApiService {
    @GET("tipoEquipos/listar")
    suspend fun listarTipoEquipos(
        @Header("Authorization") token: String,
        @Query("nombre") nombreTipo: String? = null,
        @Query("estado") estado: String? = null
    ): Response<List<TipoEquipo>>


    @POST("tipoEquipos/crear")
    suspend fun crearTipoEquipo(@Header("Authorization") token: String, @Body nuevoTipoEquipo: TipoEquipo): Response<Unit>

    @PUT("tipoEquipos/modificar")
    suspend fun editarTipoEquipo(@Header("Authorization") token: String, @Body nuevoTipoEquipo: TipoEquipo): Response<Unit>

    @DELETE("tipoEquipos/inactivar")
    suspend fun eliminarTipoEquipo(@Header("Authorization") token: String, @Query("id") id: Int) : Response<Unit>
}
