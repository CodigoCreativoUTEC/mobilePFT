package com.codigocreativo.mobile.features.proveedores

import com.codigocreativo.mobile.features.paises.Pais
import com.codigocreativo.mobile.utils.Estado
import com.google.gson.annotations.SerializedName

data class Proveedor(
    @SerializedName("id")
    val idProveedor: Int?,
    @SerializedName("nombre")
    var nombre: String,
    @SerializedName("estado")
    var estado: Estado,
    @SerializedName("pais")
    var pais: Pais?,
    @SerializedName("telefono")
    var telefono: String? = null,
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("direccion")
    var direccion: String? = null
)
