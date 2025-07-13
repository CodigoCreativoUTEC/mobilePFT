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

// Clase correcta basada en la estructura del servidor
data class UsuarioRequestCorrecto(
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
    val idInstitucion: InstitucionSimple,
    @SerializedName("idPerfil")
    val idPerfil: PerfilSimple,
    @SerializedName("usuariosTelefonos")
    val usuariosTelefonos: List<TelefonoConIdUsuario>
)

// Clase Institucion simple para el request
data class InstitucionSimple(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombre")
    val nombre: String
)

// Clase Perfil simple para el request
data class PerfilSimple(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombrePerfil")
    val nombrePerfil: String,
    @SerializedName("estado")
    val estado: String
)

// Clase Telefono con idUsuario para el request
data class TelefonoConIdUsuario(
    @SerializedName("id")
    val id: Int,
    @SerializedName("numero")
    val numero: String,
    @SerializedName("idUsuario")
    val idUsuario: String
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

// Clase final que coincide exactamente con el JSON esperado por el backend
data class UsuarioRequestFinal(
    @SerializedName("cedula")
    val cedula: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("contrasenia")
    val contrasenia: String,
    @SerializedName("fechaNacimiento")
    val fechaNacimiento: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellido")
    val apellido: String,
    @SerializedName("nombreUsuario")
    val nombreUsuario: String,
    @SerializedName("idPerfil")
    val idPerfil: PerfilFinal,
    @SerializedName("usuariosTelefonos")
    val usuariosTelefonos: List<TelefonoFinal>
)

// Clase Perfil final que coincide con el JSON esperado
data class PerfilFinal(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombrePerfil")
    val nombrePerfil: String,
    @SerializedName("estado")
    val estado: String
)

// Clase Telefono final que coincide con el JSON esperado
data class TelefonoFinal(
    @SerializedName("numero")
    val numero: String
) 