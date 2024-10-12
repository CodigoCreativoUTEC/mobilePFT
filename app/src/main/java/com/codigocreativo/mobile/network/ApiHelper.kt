package com.codigocreativo.mobile.network

import retrofit2.Response
import retrofit2.HttpException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

suspend fun <T> obtenerDatos(
    scope: CoroutineScope,
    call: suspend () -> Response<T>,
    onSuccess: (T) -> Unit,
    onFailure: (String) -> Unit
) {
    scope.launch {
        try {
            val response = call()
            if (response.isSuccessful) {
                response.body()?.let { onSuccess(it) } ?: onFailure("Respuesta nula")
            } else {
                onFailure("Error: Código de respuesta ${response.code()} - ${response.message()}")
            }
        } catch (e: HttpException) {
            onFailure("Excepción HTTP: ${e.message}")
        } catch (e: Exception) {
            onFailure("Error inesperado: ${e.message}")
        }
    }
}


class ApiHelper {


}