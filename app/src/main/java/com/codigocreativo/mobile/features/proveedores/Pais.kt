package com.codigocreativo.mobile.features.proveedores

import com.google.gson.annotations.SerializedName

data class Pais(
    @SerializedName("ID_PAIS")
    val idPais: Int,
    @SerializedName("NOMBRE")
    val nombre: String
)
