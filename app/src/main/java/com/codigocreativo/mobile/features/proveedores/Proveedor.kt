package com.codigocreativo.mobile.features.proveedores

import com.codigocreativo.mobile.features.marca.Marca
import com.codigocreativo.mobile.utils.Estado
import com.google.gson.annotations.SerializedName

data class Proveedor(
    @SerializedName("ID_PROVEEDOR")
    val idProveedor: Int,
    @SerializedName("NOMBRE")
    val nombre: String,
    @SerializedName("ESTADO")
    var estado: Estado,
    @SerializedName("ID_PAIS")
    val pais: Pais
)
