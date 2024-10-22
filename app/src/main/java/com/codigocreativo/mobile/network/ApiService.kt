package com.codigocreativo.mobile.network

import com.codigocreativo.mobile.utils.Estado
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("usuarios/google-login")
    suspend fun googleLogin(@Body token: TokenRequest): Response<JwtResponse>

    @POST("usuarios/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<JwtResponse>

    // Método para registrar un nuevo usuario
    @POST("usuarios/crear")
    suspend fun registrarUsuario(@Body usuario: User): Response<Unit>
}
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

// Clases para los requests/responses
data class TokenRequest(val idToken: String)
data class JwtResponse(val token: String, val userNeedsAdditionalInfo: Boolean, val user : User)
data class User(
    @SerializedName("usuariosTelefonos")
    val usuariosTelefonos: List<UsuariosTelefono>,
    @SerializedName("id")
    val id: Int,
    @SerializedName("cedula")
    val cedula: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("contrasenia")
    val contrasenia: String,
    @SerializedName("fechaNacimiento")
    val fechaNacimiento: String,
    @SerializedName("estado")
    val estado: Estado,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellido")
    val apellido: String,
    @SerializedName("nombreUsuario")
    val nombreUsuario: String,
    @SerializedName("idInstitucion")
    val idInstitucion: Institucion,
    @SerializedName("idPerfil")
    val idPerfil: Perfil
)

data class UsuariosTelefono(
    @SerializedName("id")
    val id: Int,
    @SerializedName("numero")
    val numero: String
)

data class Institucion(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombre")
    val nombre: String
)

data class Perfil(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombrePerfil")
    val nombrePerfil: String,
    @SerializedName("estado")
    val estado: Estado,
    @SerializedName("funcionalidades")
    val funcionalidades: List<Any?>
)