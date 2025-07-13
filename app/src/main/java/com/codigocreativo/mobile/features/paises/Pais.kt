package com.codigocreativo.mobile.features.paises

import com.google.gson.annotations.SerializedName
import com.codigocreativo.mobile.utils.Estado

data class Pais(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("estado")
    var estado: Estado
)
