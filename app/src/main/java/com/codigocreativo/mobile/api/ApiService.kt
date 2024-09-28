package com.codigocreativo.mobile.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Data classes for request and response
data class LoginRequest(val usuario: String, val password: String)
data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val cedula: String,
    val email: String,
    val contrasenia: String?,
    val fechaNacimiento: List<Int>,
    val estado: String,
    val nombre: String,
    val apellido: String,
    val nombreUsuario: String,
    val idInstitucion: Institucion,
    val idPerfil: Perfil,
    val usuariosTelefonos: List<Telefono>
)

data class Institucion(
    val id: Int,
    val nombre: String
)

data class Perfil(
    val id: Int,
    val nombrePerfil: String,
    val estado: String
)

data class Telefono(
    val id: Int,
    val numero: String
)


// Retrofit interface
interface ApiService {
    @POST("usuarios/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}
