package com.codigocreativo.mobile.features.ubicacion

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
interface UbicacionesApiService {


    @GET("ubicaciones/listar")
    suspend fun listar(
        @Header("Authorization") token: String
    ): Response<List<Ubicacion>>

}
