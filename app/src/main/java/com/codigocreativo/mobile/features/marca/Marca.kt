package com.codigocreativo.mobile.features.marca

import com.codigocreativo.mobile.utils.Estado
import com.google.gson.annotations.SerializedName

data class Marca(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("nombre")
    var nombre: String,
    @SerializedName("estado")
    var estado: Estado
)


