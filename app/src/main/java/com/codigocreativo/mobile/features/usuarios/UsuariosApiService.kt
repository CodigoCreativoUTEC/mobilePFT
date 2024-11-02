package com.codigocreativo.mobile.features.usuarios

import com.codigocreativo.mobile.features.proveedores.Proveedor
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UsuariosApiService {
    @GET("usuarios/buscar")
    suspend fun buscarUsuarios(
        @Header("Authorization") token: String,
        @Query("nombre") nombre: String? = null,
        @Query("estado") estado: String? = null
    ): Response<List<Usuario>>

    @GET("usuarios/listar")
    suspend fun listarUsuarios(
        @Header("Authorization") token: String
    ): Response<List<Usuario>>

    @PUT("usuarios/modificar")
    suspend fun actualizar(@Header("Authorization") authHeader: String, @Body usuario: Usuario): Response<Unit>

    @POST("usuarios/crear")
    suspend fun crearUsuario(@Header("Authorization") authHeader: String, @Body usuario: Usuario) : Response<Unit>

    @DELETE("usuarios/inactivar")
    suspend fun eliminarUsuario(@Header("Authorization") authHeader: String, @Query("id") id: Int) : Response<Unit>
}
