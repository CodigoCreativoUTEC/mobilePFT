package com.codigocreativo.mobile.features.perfiles

import com.codigocreativo.mobile.utils.Estado
import com.google.gson.annotations.SerializedName

data class Perfil(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombrePerfil")
    val nombrePerfil: String,
    @SerializedName("estado")
    val estado: Estado
)

// Clase Perfil con estado como string para evitar problemas de serialización
data class PerfilSinId(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombrePerfil")
    val nombrePerfil: String,
    @SerializedName("estado")
    val estado: String
)
