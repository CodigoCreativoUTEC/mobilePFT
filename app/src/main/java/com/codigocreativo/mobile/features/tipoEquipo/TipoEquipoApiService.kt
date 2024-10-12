package com.codigocreativo.mobile.features.tipoEquipo

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// Retrofit interface para la API
interface TipoEquipoApiService {
    @GET("tipoEquipos/listar")
    suspend fun listarTipoEquipos(
        @Header("Authorization") token: String,
        @Query("nombre") nombre: String? = null,
        @Query("estado") estado: String? = null
    ): Response<List<TipoEquipo>>
}
