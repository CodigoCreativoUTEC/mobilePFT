package com.codigocreativo.mobile.features.modelo

import com.codigocreativo.mobile.features.marca.Marca
import com.codigocreativo.mobile.utils.Estado
import com.google.gson.annotations.SerializedName

data class Modelo(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("idMarca")
    val idMarca: Marca,
    @SerializedName("estado")
    var estado: Estado
)
