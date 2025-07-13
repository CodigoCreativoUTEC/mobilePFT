package com.codigocreativo.mobile.features.ubicacion

import com.codigocreativo.mobile.features.equipos.Institucion

data class Ubicacion(
    val cama: Any,
    val estado: String,
    val id: Int,
    val institucion: Institucion,
    val nombre: String,
    val numero: Int,
    val piso: Int,
    val sector: String
)