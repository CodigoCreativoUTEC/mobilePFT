package com.codigocreativo.mobile.features.equipos

import com.google.gson.annotations.SerializedName

data class Institucion(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombre")
    val nombre: String
)