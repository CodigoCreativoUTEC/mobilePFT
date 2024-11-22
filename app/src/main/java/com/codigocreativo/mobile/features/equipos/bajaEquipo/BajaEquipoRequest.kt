package com.codigocreativo.mobile.features.equipos.bajaEquipo

data class BajaEquipoRequest(
    val razonBaja: String,
    val fechaBaja: String,
    val comentarios: String
)