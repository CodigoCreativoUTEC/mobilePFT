package com.codigocreativo.mobile.features.equipos

import com.codigocreativo.mobile.features.modelo.Modelo
import com.codigocreativo.mobile.features.paises.Pais
import com.codigocreativo.mobile.features.proveedores.Proveedor
import com.codigocreativo.mobile.features.tipoEquipo.TipoEquipo
import com.codigocreativo.mobile.features.ubicacion.Ubicacion
import com.codigocreativo.mobile.utils.Estado
import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class Equipo(

    @SerializedName("equiposUbicaciones")
    val equiposUbicaciones: List<Any>,
    @SerializedName("estado")
    val estado: Estado,
    @SerializedName("fechaAdquisicion")
    val fechaAdquisicion: String,
    @SerializedName("garantia")
    val garantia: String,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("idInterno")
    val idInterno: String,
    @SerializedName("idModelo")
    val idModelo: Modelo,
    @SerializedName("idPais")
    val idPais: Pais,
    @SerializedName("idProveedor")
    val idProveedor: Proveedor,
    @SerializedName("idTipo")
    val idTipo: TipoEquipo,
    @SerializedName("idUbicacion")
    val ubicacion: Ubicacion,
    @SerializedName("imagen")
    val imagen: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("nroSerie")
    val nroSerie: String
)