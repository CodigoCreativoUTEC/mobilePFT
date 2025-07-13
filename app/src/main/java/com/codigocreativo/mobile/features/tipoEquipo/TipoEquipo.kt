package com.codigocreativo.mobile.features.tipoEquipo

import com.codigocreativo.mobile.utils.Estado
import com.google.gson.annotations.SerializedName

data class TipoEquipo(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("nombreTipo")
    val nombreTipo: String,
    @SerializedName("estado")
    var estado: Estado
)