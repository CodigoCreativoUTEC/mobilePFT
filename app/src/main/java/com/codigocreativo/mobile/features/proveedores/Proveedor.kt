package com.codigocreativo.mobile.features.proveedores

import com.codigocreativo.mobile.features.marca.Marca
import com.codigocreativo.mobile.utils.Estado
import com.google.gson.annotations.SerializedName

data class Proveedor(
    @SerializedName("id")
    val idProveedor: Int,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("estado")
    var estado: Estado,
    @SerializedName("pais")
    val pais: Pais
)
