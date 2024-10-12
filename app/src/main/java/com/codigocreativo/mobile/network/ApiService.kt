package com.codigocreativo.mobile.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


data class LoginResponse(
    val token: String,
    val user: User
)

data class GoogleLoginRequest(val idToken: String)

data class User(
    val id: Int,
    val cedula: String,
    val email: String,
    val contrasenia: String?,
    val fechaNacimiento: Int,
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

interface ApiService {
    @POST("usuarios/google-login")
    suspend fun googleLogin(@Body token: TokenRequest): Response<JwtResponse>

    @POST("usuarios/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<JwtResponse>

    // Método para registrar un nuevo usuario
    @POST("usuarios/crear")
    suspend fun registrarUsuario(@Body usuario: User): Response<User>
}
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

// Clases para los requests/responses
data class TokenRequest(val idToken: String)
data class JwtResponse(val token: String, val userNeedsAdditionalInfo: Boolean, val user : User)
