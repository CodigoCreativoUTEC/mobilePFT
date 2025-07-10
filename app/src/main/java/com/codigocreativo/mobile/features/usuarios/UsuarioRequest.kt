package com.codigocreativo.mobile.features.usuarios

import com.codigocreativo.mobile.features.institucion.Institucion
import com.codigocreativo.mobile.features.perfiles.Perfil
import com.codigocreativo.mobile.features.perfiles.PerfilSinId
import com.codigocreativo.mobile.utils.Estado
import com.google.gson.annotations.SerializedName

data class UsuarioRequest(
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
    val idPerfil: Perfil,
    @SerializedName("usuariosTelefonos")
    val usuariosTelefonos: List<Telefono>
)

// Clase para crear nuevos usuarios sin el campo id
data class UsuarioRequestSinId(
    @SerializedName("cedula")
    val cedula: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("contrasenia")
    val contrasenia: String,
    @SerializedName("fechaNacimiento")
    val fechaNacimiento: String,
    @SerializedName("estado")
    val estado: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellido")
    val apellido: String,
    @SerializedName("nombreUsuario")
    val nombreUsuario: String,
    @SerializedName("idInstitucion")
    val idInstitucion: InstitucionSinId,
    @SerializedName("idPerfil")
    val idPerfil: PerfilSinId,
    @SerializedName("usuariosTelefonos")
    val usuariosTelefonos: List<TelefonoSinId>
)

// Clase simplificada que envía solo IDs en lugar de objetos completos
data class UsuarioRequestSimple(
    @SerializedName("cedula")
    val cedula: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("contrasenia")
    val contrasenia: String,
    @SerializedName("fechaNacimiento")
    val fechaNacimiento: String,
    @SerializedName("estado")
    val estado: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellido")
    val apellido: String,
    @SerializedName("nombreUsuario")
    val nombreUsuario: String,
    @SerializedName("idInstitucion")
    val idInstitucion: Int,
    @SerializedName("idPerfil")
    val idPerfil: Int,
    @SerializedName("usuariosTelefonos")
    val usuariosTelefonos: List<TelefonoSinId>
)

// Clase simplificada que envía solo IDs en lugar de objetos completos (con teléfonos que incluyen ID)
data class UsuarioRequestSimpleConTelefonos(
    @SerializedName("cedula")
    val cedula: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("contrasenia")
    val contrasenia: String,
    @SerializedName("fechaNacimiento")
    val fechaNacimiento: String,
    @SerializedName("estado")
    val estado: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellido")
    val apellido: String,
    @SerializedName("nombreUsuario")
    val nombreUsuario: String,
    @SerializedName("idInstitucion")
    val idInstitucion: Int,
    @SerializedName("idPerfil")
    val idPerfil: Int,
    @SerializedName("usuariosTelefonos")
    val usuariosTelefonos: List<TelefonoConId>
)

// Clase Institucion con estado como string para registros
data class InstitucionSinId(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("estado")
    val estado: String
)

// Clase Telefono sin id para nuevos registros
data class TelefonoSinId(
    @SerializedName("numero")
    val numero: String
)

// Clase Telefono con id para compatibilidad
data class TelefonoConId(
    @SerializedName("id")
    val id: Int,
    @SerializedName("numero")
    val numero: String
) 