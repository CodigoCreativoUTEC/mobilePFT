package com.codigocreativo.mobile.objetos

data class Marca(val id: Int, val nombre: String, var estado: Estado)

enum class Estado {
    ACTIVO, INACTIVO
}
