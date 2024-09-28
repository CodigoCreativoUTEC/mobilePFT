package com.codigocreativo.mobile

data class Marca(val id: Int, val nombre: String, val estado: Estado)

enum class Estado {
    ACTIVO, INACTIVO
}
