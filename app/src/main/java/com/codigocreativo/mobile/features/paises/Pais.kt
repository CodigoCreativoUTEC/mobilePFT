package com.codigocreativo.mobile.features.paises

import com.google.gson.annotations.SerializedName

data class Pais(
    @SerializedName("id")
    val idPais: Int,
    @SerializedName("nombre")
    val nombre: String
)
