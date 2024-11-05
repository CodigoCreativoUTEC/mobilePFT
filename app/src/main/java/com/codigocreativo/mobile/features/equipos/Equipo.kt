package com.codigocreativo.mobile.features.equipos

import com.codigocreativo.mobile.features.modelo.Modelo
import com.codigocreativo.mobile.features.paises.Pais
import com.codigocreativo.mobile.features.proveedores.Proveedor
import com.codigocreativo.mobile.features.tipoEquipo.TipoEquipo
import com.codigocreativo.mobile.utils.Estado

data class Equipo(
    val equiposUbicaciones: List<Any>,
    val estado: Estado,
    val fechaAdquisicion: String,
    val garantia: String,
    val id: Int,
    val idInterno: String,
    val idModelo: Modelo,
    val idPais: Pais,
    val idProveedor: Proveedor,
    val idTipo: TipoEquipo,
    val ubicacion: Ubicacion,
    val imagen: String,
    val nombre: String,
    val nroSerie: String
)