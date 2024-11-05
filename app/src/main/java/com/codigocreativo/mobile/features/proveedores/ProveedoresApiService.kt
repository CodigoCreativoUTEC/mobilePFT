package com.codigocreativo.mobile.features.proveedores

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

// Retrofit interface para la API
interface ProveedoresApiService {
    @GET("proveedores/buscar")
    suspend fun buscarProveedores(
        @Header("Authorization") token: String,
        @Query("nombre") nombre: String? = null,
        @Query("estado") estado: String? = null
    ): Response<List<Proveedor>>

    @GET("proveedores/listar")
    suspend fun listarProveedores(
        @Header("Authorization") token: String
    ): Response<List<Proveedor>>

    @PUT("proveedores/modificar")
    suspend fun actualizar(@Header("Authorization") authHeader: String, @Body proveedor: Proveedor):  Response<Unit>

    @POST("proveedores/crear")
    suspend fun crearProveedor(@Header("Authorization") authHeader: String, @Body proveedor: Proveedor) : Response<Unit>

    @DELETE("proveedores/inactivar")
    suspend fun eliminarProveedor(@Header("Authorization") authHeader: String, @Body id: Int) : Response<Unit>
}
