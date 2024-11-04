package com.codigocreativo.mobile.features.usuarios

import com.codigocreativo.mobile.features.institucion.Institucion
import com.codigocreativo.mobile.features.perfiles.Perfil
import com.codigocreativo.mobile.utils.Estado
import com.google.gson.annotations.SerializedName

data class Usuario(
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
    @SerializedName("usuariosDirecciones")
    val usuariosTelefonos: List<Telefono>
    )
