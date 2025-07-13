package com.codigocreativo.mobile.features.equipos.bajaEquipo
import com.codigocreativo.mobile.features.equipos.Equipo
import com.codigocreativo.mobile.features.usuarios.Usuario
import com.codigocreativo.mobile.utils.Estado
import com.google.gson.annotations.SerializedName


data class BajaEquipoRequest(
    @SerializedName("razon")
    val razon: String,
    @SerializedName("fecha")
    val fecha: String,
    @SerializedName("idUsuario")
    val idUsuario: Usuario,
    @SerializedName("idEquipo")
    val idEquipo: Equipo,
    @SerializedName("estado")
    val estado: Estado,
    @SerializedName("comentarios")
    val comentarios: String
)