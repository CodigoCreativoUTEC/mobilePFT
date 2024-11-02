package com.codigocreativo.mobile.features.usuarios

import com.google.gson.annotations.SerializedName

data class Telefono(
    @SerializedName("id")
    val id: Int,
    @SerializedName("numero")
    val numero: String
)
